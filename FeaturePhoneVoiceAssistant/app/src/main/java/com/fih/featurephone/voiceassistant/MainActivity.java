package com.fih.featurephone.voiceassistant;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.baidu.unit.SettingActivity;
import com.fih.featurephone.voiceassistant.baidu.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.speechaction.FixBaseAction;
import com.fih.featurephone.voiceassistant.speechaction.TranslateFixAction;
import com.fih.featurephone.voiceassistant.baidu.tts.BaiduTTSAI;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.ui.Msg;
import com.fih.featurephone.voiceassistant.ui.MsgAdapter;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener{
    public final static String LOCAL_BROADCAST_LAUNCH_CAMERA = "LOCAL_BROADCAST_LAUNCH_CAMERA";
    public final static String LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM = "LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM";

    private final int SETTING_REQUEST_CODE = 1;
    private final int OCR_CAMERA_REQUEST_CODE = 2;
    private final int OCR_IMAGE_REQUEST_CODE = 3;

    private final int MAIN_UI_LEVER = 0x1;
    private final int TRANSLATE_SUB_UI_LEVER = 0x2;
    private final int POEM_SUB_UI_LEVER = 0x4;
    private final int COUPLET_SUB_UI_LEVER = 0x8;
    private final int TEXT_INPUT_SUB_UI_LEVER = 0x10;

    private BaiduSpeechAI mBaiduSpeechAI;
    private BaiduUnitAI mBaiduUnitAI;
    private BaiduTTSAI mBaiduTTSAI;
    private BaiduOcrAI mBaiduOcrAI;
    private boolean mSpeechEnable = false;
    private SettingActivity.SettingResult mSettingResult;
    private Handler mActionHandler;

    private List<Msg> mResultMsgList = new ArrayList<Msg>();
    private ListView mResultMsgListView;
    private boolean mSupportTouch = false;
    private int mCurUILever = MAIN_UI_LEVER;
    private int mLastItemOptionFunction = 0;
    private String mLastTranslateLanguage = TranslateFixAction.DEFAULT_TRANSLATE_TARGET_LANGUAGE;
    private int mLastOCROptionMode = OCR_CAMERA_REQUEST_CODE;// OCR_CAMERA_REQUEST_CODE: camera, OCR_IMAGE_REQUEST_CODE: photo album
    private String mLastOCRLanguage = BaiduOcrAI.OCR_DEFAULT_LANGUAGE;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);
        mSettingResult = SettingActivity.getSavedSettingResults(this);
        mActionHandler = new Handler();

        initView();
        initAssistant();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                        new IntentFilter(LOCAL_BROADCAST_LAUNCH_CAMERA));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                        new IntentFilter(LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAssistant();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadCastReceiver);
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
        mResultMsgListView.setLayoutParams(mResultMsgListView.getLayoutParams());//及时生效
        mResultMsgListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initView() {
        mResultMsgListView = (ListView)findViewById(R.id.msg_list_view);
        mResultMsgListView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (!mSettingResult.mDebug) {
            findViewById(R.id.tv_debug_info).setVisibility(View.GONE);
        } else {
            //和属性android:scrollbars="vertical"配合使用为了滚动
            ((TextView)findViewById(R.id.tv_debug_info)).setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        MsgAdapter resultMsgAdapter = new MsgAdapter(MainActivity.this, R.layout.floatresult_msgitem, mResultMsgList);
        resultMsgAdapter.setHideHeadPic(true); //设置头像
        mResultMsgListView.setAdapter(resultMsgAdapter);
//        mResultMsgListView.setSelector(R.color.transparent);//设置条目没有选中背景@android:color/transparent
        mResultMsgListView.setSelector(R.color.item_selected);//设置条目选中背景颜色，不设置默认为黄色
        initMsg();
        initProgressDialog();
        if (mSupportTouch) {
            initTouchScreenView();
        }
    }

    private void initTouchScreenView() {
        findViewById(R.id.manage_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.manage_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowManageDialog();
            }
        });

        findViewById(R.id.assistant_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchVoiceAssistant();
            }
        });

        findViewById(R.id.left_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTranslateLanguage(true);
            }
        });

        findViewById(R.id.right_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTranslateLanguage(false);
            }
        });

        mResultMsgListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onShowItemOptionDialog(view, position);
                return true;
            }
        });

        findViewById(R.id.input_text_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInputTextOver();
            }
        });
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
    }

    private void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    private void initAssistant() {
        if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT == mSettingResult.mUnitType
                || BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT == mSettingResult.mUnitType) {
            mBaiduSpeechAI = new BaiduSpeechAI(this, mSpeechListener);
            mBaiduSpeechAI.initBaiduSpeech();
            mBaiduSpeechAI.initBaiduSpeechSettings(mSettingResult.mSpeechType);
        }

        mBaiduUnitAI = new BaiduUnitAI(this, mUnitListener,
                mSettingResult.mUnitType, mSettingResult.mRobotType, mSettingResult.mBotTypeList);
        mBaiduUnitAI.initBaiduUnit();

        mBaiduTTSAI = new BaiduTTSAI(this, mTTSListener);
        mBaiduTTSAI.initBaiduTTS();

        mBaiduOcrAI = new BaiduOcrAI(this, mOCRListener);
        mBaiduOcrAI.initBaiduOCR();
    }

    private void releaseAssistant() {
        if (null != mBaiduSpeechAI) mBaiduSpeechAI.releaseBaiduSpeech();
        if (null != mBaiduUnitAI) mBaiduUnitAI.releaseBaiduUnit();
        if (null != mBaiduTTSAI) mBaiduTTSAI.releaseBaiduTTS();
        if (null != mBaiduOcrAI) mBaiduOcrAI.releaseBaiduOCR();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                if (Activity.RESULT_OK == resultCode) {
                    relaunchApp();
                }
                break;
            case OCR_CAMERA_REQUEST_CODE:
            case OCR_IMAGE_REQUEST_CODE:
                if (Activity.RESULT_OK == resultCode) {
                    final boolean ocrQuestion = data.getBooleanExtra("OCR_QUESTION", false);
                    final String ocrImagePath = OCR_CAMERA_REQUEST_CODE == requestCode?
                                    data.getStringExtra("OCR_FILEPATH") : getImagePath(data);
                    String language = data.getStringExtra("OCR_LANGUAGE");
                    final String ocrLanguage = TextUtils.isEmpty(language)? mLastOCRLanguage : language;

                    if (!TextUtils.isEmpty(ocrImagePath)) {
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        mBaiduOcrAI.setLanguageType(ocrLanguage);
                        mBaiduOcrAI.setDetectDirection(true);
                        mBaiduOcrAI.baiduOCRText(ocrImagePath, ocrQuestion);
                    }
                }
                break;
        }
    }

    private void triggerQuery(String result) {
        if (null == mBaiduUnitAI) return;

        showProgressDialog(getResources().getString(R.string.baidu_unit_working));

        final String filterResult;
        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
            String replace = result.replaceAll("\n", " ");
            if (mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.REVERSE_TRANSLATE) {
                replace = CommonUtil.toLowerCase(replace);
            }
            filterResult = replace;
        } else {
            filterResult = result;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                mBaiduUnitAI.getBaiduKeyboardUnit(filterResult);
            }
        }).start();
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
            if (!mBaiduTTSAI.isTTSRunning()) {
                ((ImageView) findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
            }
        }

        @Override
        public void onFinalResult(final String result) {
            triggerQuery(result);
        }

        @Override
        public void onError(final String msg) {
            if (msg.contains("Network is not available")) {
                showErrorMsg(getString(R.string.network_error));
            } else {
                if (mSettingResult.mDebug) showErrorMsg(msg);
            }
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
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
        }

        @Override
        public void onFinalResult(final String question, final String answer, final String hint) {
            hideProgressDialog();
            if (TextUtils.isEmpty(answer)) return;
            //为了翻译显示更合理
            final boolean exchange = ((TRANSLATE_SUB_UI_LEVER & mCurUILever) > 0 &&
                    mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.REVERSE_TRANSLATE);
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showFinalResponse(question, answer, exchange);
                showHint(hint);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFinalResponse(question, answer, exchange);
                        showHint(hint);
                    }
                });
            }
//            Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            startTTS(answer);
        }

        @Override
        public void onNotify(String botID, int type) {
            int viewStatus = -1;
            if (FixBaseAction.STOP_FIX_ACTION == type) {
                mCurUILever = MAIN_UI_LEVER;
                if (mSupportTouch && BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE.equals(botID)) {
                    viewStatus = View.GONE;
                }
            } else {
                if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE.equals(botID)) {
                    mCurUILever = TRANSLATE_SUB_UI_LEVER;
                    if (mSupportTouch) viewStatus = View.VISIBLE;
                } else if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_POEM.equals(botID)) {
                    mCurUILever = POEM_SUB_UI_LEVER;
                } else if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_COUPLET.equals(botID)) {
                    mCurUILever = COUPLET_SUB_UI_LEVER;
                }
            }

            if (viewStatus >= 0) {
                if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                    findViewById(R.id.left_arrow_image_view).setVisibility(viewStatus);
                    findViewById(R.id.right_arrow_image_view).setVisibility(viewStatus);
                } else {
                    final int viewStatusThread = viewStatus;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.left_arrow_image_view).setVisibility(viewStatusThread);
                            findViewById(R.id.right_arrow_image_view).setVisibility(viewStatusThread);
                        }
                    });
                }
            }
        }
    };

    private BaiduTTSAI.onTTSListener mTTSListener = new BaiduTTSAI.onTTSListener() {
        @Override
        public void onStart() {
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_speaking);
        }

        @Override
        public void onEnd() {
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
        }

        @Override
        public void onError(String msg) {
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
            if (msg.contains("TimeoutException")) {
                showErrorMsg(getString(R.string.network_error));
            } else {
                if (mSettingResult.mDebug) showErrorMsg(msg);
            }
        }
    };

    private BaiduOcrAI.onOCRListener mOCRListener = new BaiduOcrAI.onOCRListener() {
        @Override
        public void onError(final String msg) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                showErrorMsg(msg);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorMsg(msg);
                    }
                });
            }
        }

        @Override
        public void onFinalResult(String result, boolean question) {
            if (TextUtils.isEmpty(result)) {
                showFinalResponse(getString(R.string.baidu_unit_ocr_fail),false);
            } else {
                if (MAIN_UI_LEVER == mCurUILever) {
                    if (question) {
                        triggerQuery(result);
                    } else {
                        showFinalResponse(result, false);
                        hideProgressDialog();
                    }
                } else if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (question) {
                        triggerQuery(result);
                    } else {
                        showFinalResponse(result, (mBaiduUnitAI.getTranslateFixAction().getTranslateType() != TranslateFixAction.TRANSLATE));
                        hideProgressDialog();
                    }
                }
            }
        }
    };

    private void startTTS(String text) {
        if (!mSettingResult.mTTS || null == mBaiduTTSAI || TextUtils.isEmpty(text)) return;

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

    public void onSwitchVoiceAssistant() {
        if (null != mBaiduTTSAI && mBaiduTTSAI.isTTSRunning()) {
            mBaiduTTSAI.stopTTSSpeak();
            mSpeechEnable = false;
            ((ImageView)findViewById(R.id.assistant_image_view)).setImageResource(R.drawable.audio_record_disable);
            return;
        }

        if (mSpeechEnable) {
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.cancelBaiduSpeechRecognize();
            if (null != mBaiduUnitAI) mBaiduUnitAI.cancelBaiduASRUnit();
        } else {
            if (null != mBaiduSpeechAI) {
                mActionHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        mBaiduSpeechAI.initBaiduSpeechSettings(mSettingResult.mSpeechType);
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

    public void onStartSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivityForResult(intent, SETTING_REQUEST_CODE);
    }

    private void showDebugInfo(String info, boolean reset) {
        if (findViewById(R.id.tv_debug_info).getVisibility() != View.VISIBLE) {
            findViewById(R.id.tv_debug_info).setVisibility(View.VISIBLE);
        }

        TextView tv = (TextView)findViewById(R.id.tv_debug_info);
        if (reset) tv.setText("");
        tv.append(info);

        //滚动到底部
        int offset = tv.getLineCount() * tv.getLineHeight();
        if(offset > tv.getHeight()){
            tv.scrollTo(0,offset - tv.getLayoutParams().height);
        }
    }

    private void showFinalResponse(String first, boolean reverse) {
        if (reverse) {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_RECEIVED));
        } else {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_SEND));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    private void showFinalResponse(String first, String second, boolean reverse) {
        if (reverse) {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_RECEIVED));
            if (!TextUtils.isEmpty(second)) mResultMsgList.add(new Msg(second, Msg.TYPE_SEND));
        } else {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_SEND));
            if (!TextUtils.isEmpty(second)) mResultMsgList.add(new Msg(second, Msg.TYPE_RECEIVED));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    private void showHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            mResultMsgList.add(new Msg(hint, Msg.TYPE_RECEIVED));
            ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
            mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
        }
    }

    private void initMsg() {
        final String welcome;
        if (!mSupportTouch) {
            welcome = getString(R.string.baidu_unit_welcome);
        } else {
            welcome = getString(R.string.baidu_unit_welcome_support_touch);
        }
        Msg msgWelcome = new Msg(welcome, Msg.TYPE_RECEIVED);
        mResultMsgList.add(msgWelcome);

        mActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSpeechEnable) startTTS(welcome);
            }
        }, 3000);
    }

    private void onShowHelp() {
        switch (mCurUILever) {
            case MAIN_UI_LEVER:
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help), Msg.TYPE_SEND));
                if (!mSupportTouch) {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_keyboard_input), Msg.TYPE_SEND));
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_dialogue_translate_mode), Msg.TYPE_SEND));
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_ocr_mode), Msg.TYPE_SEND));
                } else {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_dialogue_translate_mode_support_touch), Msg.TYPE_SEND));
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_ocr_mode_support_touch), Msg.TYPE_SEND));
                }
                break;
            case TRANSLATE_SUB_UI_LEVER:
                if (!mSupportTouch) {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help), Msg.TYPE_SEND));
                } else {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help_support_touch), Msg.TYPE_SEND));
                }
                break;
            case POEM_SUB_UI_LEVER:
            case COUPLET_SUB_UI_LEVER:
                break;
        }
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    private void showErrorMsg(String msg) {
        mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED));

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) > 0) {
                            onInputTextOver();
                            return false;
                        } else {
                            onSwitchVoiceAssistant();
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) == 0) {
                            focusListView();
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) > 0) {
            onSwitchInputText();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    onSwitchInputText();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    switchTranslateLanguage(true);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    switchTranslateLanguage(false);
                    break;
                case KeyEvent.KEYCODE_1:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_5:
                    event.startTracking();
                    if(event.getRepeatCount() == 0){
                        mShortPress = true;
                    }
                    return true;
                case KeyEvent.KEYCODE_2:
                    scrollDebugView(true);
                    break;
                case KeyEvent.KEYCODE_8:
                    scrollDebugView(false);
                    break;
                case KeyEvent.KEYCODE_STAR:
                    onShowHelp();
                    break;
                case KeyEvent.KEYCODE_POUND:
                    onStartSettingActivity();
                    break;
            }
        } else {
            if (KeyEvent.KEYCODE_MENU == keyCode) {
                onSwitchInputText();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_5:
                if(mShortPress) doOption(mLastItemOptionFunction, null, -1);
                //Don't handle long press here, because the user will have to get his finger back up first
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_3:
                if(mShortPress) {
                    if (OCR_CAMERA_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRCameraActivity(mLastOCRLanguage);
                    } else if (OCR_IMAGE_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRSelectImage(mLastOCRLanguage);
                    }
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_1:
                if(mShortPress) switchTranslateShortcut(mLastTranslateLanguage);
                mShortPress = false;
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean mShortPress = false;
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_5:
                mShortPress = false;
                onShowItemOptionDialog(null, -1);
                return true;
            case KeyEvent.KEYCODE_3:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (OCR_CAMERA_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRCameraActivity(mLastOCRLanguage);
                    } else if (OCR_IMAGE_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRSelectImage(mLastOCRLanguage);
                    }
                } else {
                    onShowOCRLanguageDialog();
                }
                return true;
            case KeyEvent.KEYCODE_1:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    switchTranslateShortcut(mLastTranslateLanguage);
                } else {
                    onShowTranslateLanguageDialog();
                }
                return true;
            default:
                break;
        }
        //Just return false because the super call does always the same (returning false)
        return false;
    }

    private void switchTranslateShortcut(String language) {
        if (null == mBaiduUnitAI) return;

        if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduUnitAI.BAIDU_UNIT_SPEECH_CHINESE);
        mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);

        if (TRANSLATE_SUB_UI_LEVER != mCurUILever) {
            mBaiduUnitAI.getTranslateFixAction().forceAction(true);
            mBaiduUnitAI.getTranslateFixAction().setTargetLanguage(language);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_start), language);
            showFinalResponse(msg,false);
            startTTS(msg);

            mCurUILever = TRANSLATE_SUB_UI_LEVER;
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
            }
        } else {
            mBaiduUnitAI.getTranslateFixAction().forceAction(false);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_stop), language);
            showFinalResponse(msg, false);
            startTTS(msg);

            mCurUILever = MAIN_UI_LEVER;
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.GONE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.GONE);
            }
        }
    }

    private void switchTranslateLanguage(boolean chineseToForeign) {
        if (null == mBaiduUnitAI || TRANSLATE_SUB_UI_LEVER != mCurUILever) return;

        if (chineseToForeign) {
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
            }
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduUnitAI.BAIDU_UNIT_SPEECH_CHINESE);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);
            showFinalResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage()), false);
        } else {
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.INVISIBLE);
            }
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduUnitAI.BAIDU_UNIT_SPEECH_ENGLISH);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.REVERSE_TRANSLATE);
            showFinalResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getTargetLanguage()), true);
        }
    }

    private void focusListView() {
//        mResultMsgListView.setFocusable(true);
        mResultMsgListView.requestFocus();
    }

    private void scrollDebugView(boolean up) {
        TextView tv = (TextView)findViewById(R.id.tv_debug_info);
        if (tv.getVisibility() != View.VISIBLE) return;

        int offset = tv.getLineHeight() * 5;
        if (up){
            if (tv.getScrollY() > 0) tv.scrollBy(0, -offset);
        } else {
            if (tv.getScrollY() + tv.getHeight() < tv.getLineCount() * tv.getLineHeight()) tv.scrollBy(0, offset);
        }
    }

    private void onOCRCameraActivity(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra("OCR_LANGUAGE", mLastOCRLanguage);
        startActivityForResult(intent, OCR_CAMERA_REQUEST_CODE);
    }

    private void onOCRSelectImage(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        //通过intent打开相册，使用startactivityForResult方法启动actvity，会返回到onActivityResult方法，所以我们还得复写onActivityResult方法
        Intent intent = new Intent(Intent.ACTION_PICK);//"android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, OCR_IMAGE_REQUEST_CODE);
    }

    private String getAndShowMatchedOCRLanguage(String language) {
        if (TRANSLATE_SUB_UI_LEVER == mCurUILever && null != mBaiduUnitAI) {
            if (mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.TRANSLATE) {
                language = mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage();
                showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
            } else {
                language = mBaiduUnitAI.getTranslateFixAction().getTargetLanguage();
                showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), true);
            }
        } else {
            showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
        }

        return language;
    }

    private void onSwitchInputText() {
        EditText inputText = (EditText)findViewById(R.id.input_text);
        if (inputText.getVisibility() == View.VISIBLE) {
            findViewById(R.id.input_text).setVisibility(View.GONE);
            if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.GONE);
            mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;
        } else {
            findViewById(R.id.input_text).setVisibility(View.VISIBLE);
            if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
            inputText.requestFocus();
            mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
        }
    }

    private void onInputTextOver() {
        EditText inputText = (EditText)findViewById(R.id.input_text);
        final String input = inputText.getText().toString();
        if (!TextUtils.isEmpty(input)) {
            triggerQuery(input);
        }
        findViewById(R.id.input_text).setVisibility(View.GONE);
        if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.GONE);

        mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;
        if (mSupportTouch) CommonUtil.hideSoftKeyboard(this);
    }

    private void onShowItemOptionDialog(final View view, final int position) {
        final String[] items = {
                getString(R.string.option_voice_output),
                getString(R.string.option_query_again),
                getString(R.string.option_copy_edit),
                getString(R.string.option_clear_item),
                getString(R.string.option_web_query)
        };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                mLastItemOptionFunction = which;

                doOption(which, view, position);
            }
        });
        listDialog.show();
    }

    private void doOption(int option, View view, int position) {
        String text = "";

        if (mSupportTouch) {
            if (null != view) text = ((TextView)view).getText().toString();
        } else {
            Msg msg = (Msg)mResultMsgListView.getSelectedItem();
            if (null != msg) text = msg.getContent();
        }

        if (TextUtils.isEmpty(text)) return;
        //remove botid name, xxx (xxx)
        if (text.lastIndexOf(")") == text.length() - 1) {
            int pos = text.lastIndexOf("(");
            if (pos >= 0) {
                text = text.substring(0, pos);
            }
        }

        switch (option) {
            case 0://语音播报
                if (!mSpeechEnable && (null != mBaiduTTSAI && !mBaiduTTSAI.isTTSRunning())) {
                    startTTS(text);
                }
                break;
            case 1://重新提问
                triggerQuery(text);
                break;
            case 2://拷贝编辑
                copyAndEditMsgItemText(text);
                break;
            case 3://清除显示
                if (mSupportTouch) {
                    if (position >= 0) mResultMsgList.remove(position);
                } else {
                    mResultMsgList.remove(mResultMsgListView.getSelectedItemPosition());
                }
                ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
                break;
            case 4://上网搜索
                new WebSearchAction(this).searchAction(text);
                break;
        }
    }

    private void onShowManageDialog() {
        final String[] items = {
                getString(R.string.option_shortcut_translate_start),
                getString(R.string.option_shortcut_ocr),
                getString(R.string.option_text_input),
                getString(R.string.option_clear_all_items),
                getString(R.string.option_show_setting),
                getString(R.string.option_show_help),
        };

        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
            items[0] = getString(R.string.option_shortcut_translate_stop);
        }

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: //快捷翻译
                        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                            switchTranslateShortcut("");//exit translate
                        } else {
                            onShowTranslateLanguageDialog();
                        }
                        break;
                    case 1: //快捷OCR
                        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                            onShowOCRItemsDialog(mLastOCRLanguage);
                        } else {
                            onShowOCRLanguageDialog();
                        }
                        break;
                    case 2: //输入文本框
                        onSwitchInputText();
                        break;
                    case 3: //清除列表中所有显示
                        mResultMsgList.clear();
                        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
                        break;
                    case 4: //显示系统设置
                        onStartSettingActivity();
                        break;
                    case 5: //显示帮助
                        onShowHelp();
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowTranslateLanguageDialog() {
        final String[] allItems = getResources().getStringArray(R.array.translate_action_language_keyword);
        final String[] items = Arrays.copyOfRange(allItems, 3, allItems.length);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                mLastTranslateLanguage = items[which];
                switchTranslateShortcut(items[which]);
            }
        });
        listDialog.show();
    }

    private void onShowOCRItemsDialog(final String language) {
        final String[] items = {
                getString(R.string.option_shortcut_ocr_camera),
                getString(R.string.option_shortcut_ocr_image),
        };

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        mLastOCROptionMode = OCR_CAMERA_REQUEST_CODE;
                        onOCRCameraActivity(language);
                        break;
                    case 1:
                        mLastOCROptionMode = OCR_IMAGE_REQUEST_CODE;
                        onOCRSelectImage(language);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowOCRLanguageDialog() {
        final String[] items = getResources().getStringArray(R.array.ocr_language_item);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                onShowOCRItemsDialog(items[which]);
            }
        });
        listDialog.show();
    }

    private void copyAndEditMsgItemText(String text) {
        ClipboardManager clipboardManager =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (null != clipboardManager) {
            ClipData clipData = ClipData.newPlainText(null, text);
            clipboardManager.setPrimaryClip(clipData);
        }

        EditText inputText = (EditText)findViewById(R.id.input_text);
        inputText.setVisibility(View.VISIBLE);
        findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
        inputText.setText(text);
        inputText.requestFocus();

        mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
    }

    private String getImagePath(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (null == uri) return null;

        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }

        return imagePath;
    }

    //获得图片路径
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);   //内容提供器
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));   //获取路径
            }
            cursor.close();
        }
        return path;
    }

    private BroadcastReceiver mLocalBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LOCAL_BROADCAST_LAUNCH_CAMERA.equals(action)) {
                onOCRCameraActivity(intent.getStringExtra("OCR_LANGUAGE"));
            }
        }
    };


//    private String getOCRLanguageType(String language) {
//        if (TextUtils.isEmpty(language)) language = BaiduOcrAI.OCR_DEFAULT_LANGUAGE;
//        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
//            if (mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.TRANSLATE) {
//                language = mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage();
//                showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
//            } else {
//                language = mBaiduUnitAI.getTranslateFixAction().getTargetLanguage();
//                showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), true);
//            }
//        } else {
//            showFinalResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
//        }
//        return language;
//    }


/*
    private void focusDebugView(){
        if (mSettingResult.mDebug) {
//            findViewById(R.id.tv_debug_info).setFocusable(true);
            findViewById(R.id.tv_debug_info).requestFocus();
        }
    }


    private void selListViewItem(boolean up) {
        int pos = mResultMsgListView.getSelectedItemPosition();
        if (up) {
//            int pos = mResultMsgListView.getFirstVisiblePosition();
            mResultMsgListView.setSelection(pos - 1);
        } else {
//            int pos = mResultMsgListView.getLastVisiblePosition();
            mResultMsgListView.setSelection(pos + 1);
        }
    }

    private void ttsListViewSelItem() {
        if (mSpeechEnable || mBaiduTTSAI.isTTSRunning()) return;

        Msg msg = (Msg)mResultMsgListView.getSelectedItem();
        if (null != msg) {
            String text = msg.getContent();
            startTTS(text);
        }
    }
*/
}
