package com.fih.featurephone.voiceassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
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

import com.fih.featurephone.voiceassistant.baidu.face.activity.FaceAuthActivity;
import com.fih.featurephone.voiceassistant.baidu.face.activity.FaceRGBIdentifyActivity;
import com.fih.featurephone.voiceassistant.baidu.face.activity.FaceRGBRegisterActivity;
import com.fih.featurephone.voiceassistant.baidu.face.activity.FaceUserManagerActivity;
import com.fih.featurephone.voiceassistant.baidu.face.listener.SdkInitListener;
import com.fih.featurephone.voiceassistant.baidu.face.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.face.utils.ConfigUtils;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.baidu.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.baidu.tts.BaiduTTSAI;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.speechaction.FixBaseAction;
import com.fih.featurephone.voiceassistant.speechaction.TranslateFixAction;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.ui.Msg;
import com.fih.featurephone.voiceassistant.ui.MsgAdapter;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener{
    public final static String LOCAL_BROADCAST_LAUNCH_CAMERA = "LOCAL_BROADCAST_LAUNCH_CAMERA";
    public final static String LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM = "LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM";

    private final int SETTING_REQUEST_CODE = 1;
    private final int OCR_CAMERA_REQUEST_CODE = 2;
    private final int OCR_IMAGE_REQUEST_CODE = 3;
    private final int FACE_REQUEST_CODE = 4;
    private final int REQUEST_MULTIPLE_PERMISSION = 100;

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

    private List<Msg> mResultMsgList = new ArrayList<>();
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

        applyApkPermissions();

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);
        mSettingResult = SettingActivity.getSavedSettingResults(this);
        mActionHandler = new Handler();

        initView();
        initAssistant();
        initFace();

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
        TextView debugTextView = findViewById(R.id.tv_debug_info);
        int displayHeight = CommonUtil.getDisplaySize(this).y - CommonUtil.getStatusBarHeight(this);
        int imageViewButtonHeight = findViewById(R.id.microphone_image_view).getLayoutParams().height;
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
        mResultMsgListView = findViewById(R.id.msg_list_view);
        mResultMsgListView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (!mSettingResult.mDebug) {
            findViewById(R.id.tv_debug_info).setVisibility(View.GONE);
        } else {
            //和属性android:scrollbars="vertical"配合使用为了滚动
            ((TextView)findViewById(R.id.tv_debug_info)).setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        MsgAdapter resultMsgAdapter = new MsgAdapter(MainActivity.this, R.layout.result_msglist_item, mResultMsgList);
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
        findViewById(R.id.assistant_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.assistant_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowAssistantDialog();
            }
        });

        findViewById(R.id.manage_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.manage_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowManageDialog();
            }
        });

        findViewById(R.id.microphone_image_view).setOnClickListener(new View.OnClickListener() {
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
        if (Activity.RESULT_OK != resultCode) return;

        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                relaunchApp();
                break;
            case OCR_CAMERA_REQUEST_CODE:
            case OCR_IMAGE_REQUEST_CODE:
                final boolean ocrQuestion = data.getBooleanExtra("OCR_QUESTION", false);
                final String ocrImagePath = OCR_CAMERA_REQUEST_CODE == requestCode?
                                data.getStringExtra("OCR_FILEPATH") :
                                SystemUtil.getAlbumImagePath(this, data.getData());
                String language = data.getStringExtra("OCR_LANGUAGE");
                final String ocrLanguage = TextUtils.isEmpty(language)? mLastOCRLanguage : language;

                if (!TextUtils.isEmpty(ocrImagePath)) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    mBaiduOcrAI.setLanguageType(ocrLanguage);
                    mBaiduOcrAI.setDetectDirection(true);
                    mBaiduOcrAI.baiduOCRText(ocrImagePath, ocrQuestion);
                }
                break;
            case FACE_REQUEST_CODE:
                String faceUserName = data.getStringExtra("FACE_USER_NAME");
                triggerQuery("谁是" + faceUserName);
                break;
        }
    }

    // 请求权限
    public void applyApkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] requiredPermissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS};
            ArrayList<String> denyPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                denyPermissions.add(permission);
            }
            if (denyPermissions.size() > 0) {
                requestPermissions(denyPermissions.toArray(new String[0]), REQUEST_MULTIPLE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        if (REQUEST_MULTIPLE_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    CommonUtil.toast(this, "必须允许所有权限，否则功能会异常");
                    SystemClock.sleep(2000);
                    applyApkPermissions();
                    return;
                }
            }
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
                ((ImageView) findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
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
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
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
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_speaking_white_48dp);
        }

        @Override
        public void onEnd() {
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
        }

        @Override
        public void onError(String msg) {
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
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
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
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
        ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(
                mSpeechEnable? R.drawable.baseline_mic_black_48dp : R.drawable.baseline_mic_none_white_48dp);
    }

//    public void onExitVoiceAssistant(View v) {
//        releaseAssistant();
//        finish();
//    }

    public void onStartSettingActivity() {
        startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), SETTING_REQUEST_CODE);
    }

    public void onStartFaceDetectActivity() {
        showFinalResponse(getString(R.string.baidu_face_identify_start), false);
        startActivityForResult(new Intent(MainActivity.this, FaceRGBIdentifyActivity.class), FACE_REQUEST_CODE);
    }

    public void onStartFaceManagerActivity() {
        startActivity(new Intent(MainActivity.this, FaceUserManagerActivity.class));
    }

    private void showDebugInfo(String info, boolean reset) {
        if (findViewById(R.id.tv_debug_info).getVisibility() != View.VISIBLE) {
            findViewById(R.id.tv_debug_info).setVisibility(View.VISIBLE);
        }

        TextView tv = findViewById(R.id.tv_debug_info);
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
                startActivity(new Intent(MainActivity.this, HelpInfoActivity.class));
//                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help), Msg.TYPE_SEND));
//                if (!mSupportTouch) {
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_keyboard_input), Msg.TYPE_SEND));
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_dialogue_translate_mode), Msg.TYPE_SEND));
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_face_mode), Msg.TYPE_SEND));
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_ocr_mode), Msg.TYPE_SEND));
//                } else {
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_dialogue_translate_mode_support_touch), Msg.TYPE_SEND));
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_face_mode_support_touch), Msg.TYPE_SEND));
//                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_question_help_ocr_mode_support_touch), Msg.TYPE_SEND));
//                }
                break;
            case TRANSLATE_SUB_UI_LEVER:
                if (!mSupportTouch) {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help), Msg.TYPE_SEND));
                } else {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help_support_touch), Msg.TYPE_SEND));
                }
                break;
            case POEM_SUB_UI_LEVER:
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_poem_help), Msg.TYPE_SEND));
                break;
            case COUPLET_SUB_UI_LEVER:
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_couplet_help), Msg.TYPE_SEND));
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
                case KeyEvent.KEYCODE_2:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_5:
                    event.startTracking();
                    if(event.getRepeatCount() == 0){
                        mShortPress = true;
                    }
                    return true;
//                case KeyEvent.KEYCODE_2:
//                    scrollDebugView(true);
//                    break;
//                case KeyEvent.KEYCODE_8:
//                    scrollDebugView(false);
//                    break;
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
            case KeyEvent.KEYCODE_2:
                if(mShortPress) {
                    onStartFaceDetectActivity();
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_1:
                if(mShortPress) onSwitchTranslateShortcut(mLastTranslateLanguage);
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
            case KeyEvent.KEYCODE_2:
                mShortPress = false;
                onStartFaceManagerActivity();
                return true;
            case KeyEvent.KEYCODE_1:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    onSwitchTranslateShortcut(mLastTranslateLanguage);
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

    private void onSwitchTranslateShortcut(String language) {
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

/*    private void scrollDebugView(boolean up) {
        TextView tv = findViewById(R.id.tv_debug_info);
        if (tv.getVisibility() != View.VISIBLE) return;

        int offset = tv.getLineHeight() * 5;
        if (up){
            if (tv.getScrollY() > 0) tv.scrollBy(0, -offset);
        } else {
            if (tv.getScrollY() + tv.getHeight() < tv.getLineCount() * tv.getLineHeight()) tv.scrollBy(0, offset);
        }
    }*/

    private void onOCRCameraActivity(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra("OCR_LANGUAGE", mLastOCRLanguage);
        startActivityForResult(intent, OCR_CAMERA_REQUEST_CODE);
    }

    private void onOCRSelectImage(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        SystemUtil.startSysAlbumActivity(this, OCR_IMAGE_REQUEST_CODE);
//
//        //通过intent打开相册，使用startactivityForResult方法启动actvity，会返回到onActivityResult方法，所以我们还得复写onActivityResult方法
//        Intent intent = new Intent(Intent.ACTION_PICK);//"android.intent.action.GET_CONTENT");
//        intent.setType("image/*");
//        startActivityForResult(intent, OCR_IMAGE_REQUEST_CODE);
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
        EditText inputText = findViewById(R.id.input_text);
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

    private void onClearAllItems() {
        mResultMsgList.clear();
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
    }

    private void onInputTextOver() {
        EditText inputText = findViewById(R.id.input_text);
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

    private void onShowAssistantDialog() {
        final String[] items = {
                getString(R.string.option_show_face_manger),
                getString(R.string.option_show_setting),
                getString(R.string.option_show_help),
        };

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: // 人脸库管理
                        onStartFaceManagerActivity();
                        break;
                    case 1: // 系统设置
                        onStartSettingActivity();
                        break;
                    case 2: // 显示帮助
                        onShowHelp();
                        break;
                    case 3: //人脸注册
                        startActivity(new Intent(MainActivity.this, FaceRGBRegisterActivity.class));
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowManageDialog() {
        final String[] items = {
                getString(R.string.option_translate_start),
                getString(R.string.option_ocr),
                getString(R.string.option_show_face_detect),
                getString(R.string.option_text_input),
                getString(R.string.option_clear_all_items),
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
                            onSwitchTranslateShortcut("");//exit translate
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
                    case 2: //人脸识别
                        onStartFaceDetectActivity();
                        break;
                    case 3: //输入文本框
                        onSwitchInputText();
                        break;
                    case 4: //清除列表中所有显示
                        onClearAllItems();
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
                onSwitchTranslateShortcut(items[which]);
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

        EditText inputText = findViewById(R.id.input_text);
        inputText.setVisibility(View.VISIBLE);
        findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
        inputText.setText(text);
        inputText.requestFocus();

        mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
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

    //Face engine
    private void initFace() {
        final String configFilePath = getFilesDir() + File.separator + "faceConfig.txt";
        boolean isConfigExit = ConfigUtils.isConfigExit(configFilePath);
        boolean isInitConfig = ConfigUtils.initConfig(configFilePath);
        if (isInitConfig && isConfigExit) {
            CommonUtil.toast(this, getString(R.string.baidu_face_config_success));
        } else {
            CommonUtil.toast(this, getString(R.string.baidu_face_config_fail));
            ConfigUtils.modifyJson(configFilePath);
        }
        initFaceLicense();
    }
    /**
     * 启动应用程序，如果之前初始过，自动初始化鉴权和模型（可以添加到Application 中）
     */
    private void initFaceLicense() {
        if (FaceSDKManager.sInitStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(this, new SdkInitListener() {
                @Override
                public void initStart() {}
                @Override
                public void initLicenseSuccess() {
                    CommonUtil.toast(MainActivity.this,
                            getString(R.string.baidu_face_auth_success));
                }
                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    CommonUtil.toast(MainActivity.this,
                            getString(R.string.baidu_face_auth_fail) + msg + " (" + errorCode + ")");
                    startActivity(new Intent(MainActivity.this, FaceAuthActivity.class));
                }
                @Override
                public void initModelSuccess() {
                    CommonUtil.toast(MainActivity.this, getString(R.string.baidu_face_model_success));
                }
                @Override
                public void initModelFail(int errorCode, String msg) {
                    CommonUtil.toast(MainActivity.this,
                            getString(R.string.baidu_face_model_fail) + msg + " (" + errorCode + ")");
                }
            });
        }
    }
}
