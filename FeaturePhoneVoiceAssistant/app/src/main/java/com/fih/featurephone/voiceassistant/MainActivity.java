package com.fih.featurephone.voiceassistant;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.tts.BaiduTTSAI;
import com.fih.featurephone.voiceassistant.ui.Msg;
import com.fih.featurephone.voiceassistant.ui.MsgAdapter;
import com.fih.featurephone.voiceassistant.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.unit.SettingActivity;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener{
    private final int SETTING_REQUEST_CODE = 0;

    private BaiduSpeechAI mBaiduSpeechAI;
    private BaiduUnitAI mBaiduUnitAI;
    private BaiduTTSAI mBaiduTTSAI;
    private boolean mSpeechEnable = false;
    private SettingActivity.SettingResult mSettingResult;
    private Handler mActionHandler;

    private List<Msg> mResultMsgList = new ArrayList<Msg>();
    private MsgAdapter mResultMsgAdapter;
    private ListView mResultMsgListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettingResult = SettingActivity.getSavedSettingResults(this);
        mActionHandler = new Handler();
        initAssistant();

        initView();
    }

    @Override
    public void onGlobalLayout() {
        TextView debugTextView = (TextView)findViewById(R.id.tv_debug_info);
        int displayHeight = CommonUtil.getDisplaySize(this).y - CommonUtil.getStatusBarHeight(this);
        int imageViewButtonHeight = findViewById(R.id.assistant_image_view).getLayoutParams().height;
        if (mSettingResult.mDebug) {
            debugTextView.getLayoutParams().height = displayHeight / 4;
            mResultMsgListView.getLayoutParams().height = displayHeight * 3/4 - imageViewButtonHeight;
        } else {
            mResultMsgListView.getLayoutParams().height = displayHeight - imageViewButtonHeight;
        }

        mResultMsgListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initView() {
        mResultMsgListView = (ListView)findViewById(R.id.msg_list_view);
        mResultMsgListView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (!mSettingResult.mDebug) {
            findViewById(R.id.debug_scrollview).setVisibility(View.GONE);
        }

        mResultMsgAdapter = new MsgAdapter(MainActivity.this, R.layout.floatresult_msgitem, mResultMsgList);
        mResultMsgListView.setAdapter(mResultMsgAdapter);
        initMsg();

        if (CommonUtil.isSupportMultiTouch(this)) {
            findViewById(R.id.assistant_image_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStartVoiceAssistant(v);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAssistant();
    }

    private void initAssistant() {
        if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT == mSettingResult.mUnitType
                || BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT == mSettingResult.mUnitType) {
            mBaiduSpeechAI = new BaiduSpeechAI(this, mSpeechListener);
            mBaiduSpeechAI.initBaiduSpeech();
        }

        mBaiduUnitAI = new BaiduUnitAI(this, mUnitListener,
                mSettingResult.mUnitType, mSettingResult.mRobotType, mSettingResult.mBotTypeList);
        mBaiduUnitAI.initBaiduUnit();

        mBaiduTTSAI = new BaiduTTSAI(this, mTTSListener);
        mBaiduTTSAI.initBaiduTTS();
    }

    private void releaseAssistant() {
        if (null != mBaiduSpeechAI) mBaiduSpeechAI.releaseBaiduSpeech();
        if (null != mBaiduUnitAI) mBaiduUnitAI.releaseBaiduUnit();
        if (null != mBaiduTTSAI) mBaiduTTSAI.releaseBaiduTTS();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (SETTING_REQUEST_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                relaunchApp();
            }
        }
    }

    private void relaunchApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        }
        finish();
    }

    private BaiduSpeechAI.onSpeechListener mSpeechListener = new BaiduSpeechAI.onSpeechListener() {
        @Override
        public void onShowDebugInfo(final String info) {
            if (!mSettingResult.mDebug) {
                return;
            }
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showDebugInfo(info, false);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDebugInfo(info, false);
                    }
                });
            }
        }

        @Override
        public void onExit() {
            mSpeechEnable = false;
        }

        @Override
        public void onFinalResult(final String result) {
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            new Thread(new Runnable(){
                @Override
                public void run() {
                    mBaiduUnitAI.getBaiduKeyboardUnit(result);
                }
            }).start();
        }
    };

    private BaiduUnitAI.onUnitListener mUnitListener = new BaiduUnitAI.onUnitListener() {
        @Override
        public void onShowDebugInfo(final String info, final boolean reset) {
            if (!mSettingResult.mDebug) {
                return;
            }
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showDebugInfo(info, reset);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showDebugInfo(info, reset);
                    }
                });
            }
        }

        @Override
        public void onExit() {
            mSpeechEnable = false;
        }

        @Override
        public void onFinalResult(final String question, final String answer, final String hint) {
            if (TextUtils.isEmpty(answer)) return;
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showFinalResponse(question, answer, hint);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFinalResponse(question, answer, hint);
                    }
                });
            }
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            startTTS(answer);
        }
    };

    private void startTTS(String text) {
        final int MAX_TTS_LENGTH = 1024 / 2; //chines character is two bytes
        int startPos = 0;
        while (true) {
            if ((startPos + MAX_TTS_LENGTH) > text.length()) {
                mBaiduTTSAI.startTTSSpeak(text.substring(startPos));
                break;
            } else {
                mBaiduTTSAI.startTTSSpeak(text.substring(startPos, MAX_TTS_LENGTH));
                startPos += MAX_TTS_LENGTH;
            }
        }
    }

    private BaiduTTSAI.onTTSListener mTTSListener = new BaiduTTSAI.onTTSListener() {
        @Override
        public void start() {
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_speaking);
        }

        @Override
        public void end() {
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
        }

    };

    public void onStartVoiceAssistant(View v) {
        if (mBaiduTTSAI.isTTSRunning()) {
            mBaiduTTSAI.stopTTSSpeak();
            mSpeechEnable = false;
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
            return;
        }
        if (mSpeechEnable) {
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.stopBaiduSpeechRecognize();
            if (null != mBaiduUnitAI) mBaiduUnitAI.stopBaiduASRUnit();
        } else {
            if (null != mBaiduSpeechAI) {
                mActionHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBaiduSpeechAI.startBaiduSpeechRecognize();
                    }
                });
            }
            if (null != mBaiduUnitAI) {
                mActionHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBaiduUnitAI.startBaiduASRUnit();
                    }
                });
            }
        }
        mSpeechEnable = !mSpeechEnable;
        ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(
                mSpeechEnable? R.drawable.audio_record_enable : R.drawable.audio_record_disable);
    }

//    public void onExitVoiceAssistant(View v) {
//        releaseAssistant();
//        finish();
//    }

    public void onSetVoiceAssistant(View v) {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivityForResult(intent, SETTING_REQUEST_CODE);
    }

    private void showDebugInfo(String info, boolean reset) {
        if (findViewById(R.id.debug_scrollview).getVisibility() != View.VISIBLE) {
            findViewById(R.id.debug_scrollview).setVisibility(View.VISIBLE);
        }

        TextView tv = (TextView)findViewById(R.id.tv_debug_info);
        if (reset) tv.setText("");
        tv.append(info);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ScrollView scrollView = (ScrollView)findViewById(R.id.debug_scrollview);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void showFinalResponse(String question, String answer, String hint) {
        mResultMsgList.add(new Msg(question, Msg.TYPE_SEND));
        mResultMsgList.add(new Msg(answer, Msg.TYPE_RECEIVED));
        if (!TextUtils.isEmpty(hint)) mResultMsgList.add(new Msg(hint, Msg.TYPE_RECEIVED));

        mResultMsgAdapter.notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgList.size());

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mResultMsgListView.setSelection(mResultMsgAdapter.getCount() - 1);
            }
        });
    }

    private void initMsg() {
        final String welcome = getString(R.string.baidu_unit_welcome);
        Msg msgWelcome = new Msg(welcome, Msg.TYPE_RECEIVED);
        mResultMsgList.add(msgWelcome);
        Msg msgHelp = new Msg(getString(R.string.baidu_unit_question_help), Msg.TYPE_RECEIVED);
        mResultMsgList.add(msgHelp);

        mActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTTS(welcome);
            }
        }, 3000);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    onStartVoiceAssistant((findViewById(R.id.assistant_image_view)));
                    return false;
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    focusListView();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    focusDebugView();
                    break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                onSetVoiceAssistant(null);
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ((ScrollView)findViewById(R.id.debug_scrollview)).pageScroll(ScrollView.FOCUS_UP);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ((ScrollView)findViewById(R.id.debug_scrollview)).pageScroll(ScrollView.FOCUS_DOWN);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void focusListView() {
        mResultMsgListView.setFocusable(true);
        mResultMsgListView.requestFocus();
    }

    private void focusDebugView(){
        if (mSettingResult.mDebug) {
            findViewById(R.id.tv_debug_info).setFocusable(true);
            findViewById(R.id.tv_debug_info).requestFocus();
        }
    }
}
