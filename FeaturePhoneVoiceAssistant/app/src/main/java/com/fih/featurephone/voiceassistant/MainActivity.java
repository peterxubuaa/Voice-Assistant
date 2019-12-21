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

import com.fih.featurephone.voiceassistant.baidu.faceoffline.BaiduFaceOfflineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.baidu.tts.BaiduTTSAI;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.speechaction.FixBaseAction;
import com.fih.featurephone.voiceassistant.speechaction.TranslateFixAction;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.ui.Msg;
import com.fih.featurephone.voiceassistant.ui.MsgAdapter;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener{
    private final int SETTING_REQUEST_CODE = 1;
    private final int OCR_CAMERA_REQUEST_CODE = 2;
    private final int OCR_IMAGE_REQUEST_CODE = 3;
    private final int FACE_IDENTIFY_CAMERA_REQUEST_CODE = 4;
    private final int FACE_IDENTIFY_IMAGE_REQUEST_CODE = 5;
    private final int FACE_DETECT_CAMERA_REQUEST_CODE = 44;
    private final int FACE_DETECT_IMAGE_REQUEST_CODE = 55;
    private final int CLASSIFY_CAMERA_REQUEST_CODE = 6;
    private final int CLASSIFY_IMAGE_REQUEST_CODE = 7;
    private final int IMAGE_SELECT_REQUEST_CODE = 99;
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
    private BaiduFaceOfflineAI mBaiduFaceOfflineAI;
    private BaiduFaceOnlineAI mBaiduFaceOnlineAI;
    private BaiduClassifyImageAI mBaiduClassifyImageAI;

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
    private int mLastClassifyImageType = BaiduClassifyImageAI.CLASSIFY_TYPE_ADVANCED_GENERAL;
    private ProgressDialog mProgressDialog;
    private int mCropImageRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);
        mSettingResult = SettingActivity.getSavedSettingResults(this);
        mActionHandler = new Handler();

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM));

        if (applyApkPermissions()) {
            initView();
            mActionHandler.post(new Runnable() {
                @Override
                public void run() {
                    initAssistant();//比较耗时，所以异步执行
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            findViewById(R.id.tv_debug_info).setVisibility(View.VISIBLE);
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
                onSwitchTranslateLanguage(true);
            }
        });

        findViewById(R.id.right_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchTranslateLanguage(false);
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

        if (mSettingResult.mOfflineFace) {
            mBaiduFaceOfflineAI = new BaiduFaceOfflineAI(this, mFaceOfflineListener);
            mBaiduFaceOfflineAI.initBaiduFace();
        } else {
            mBaiduFaceOnlineAI = new BaiduFaceOnlineAI(this, mFaceOnlineListener);
            mBaiduFaceOnlineAI.initBaiduFace();
        }

        mBaiduClassifyImageAI = new BaiduClassifyImageAI(this, mClassifyImageListener);
        mBaiduClassifyImageAI.initBaiduClassifyImage();
    }

    private void releaseAssistant() {
        if (null != mBaiduSpeechAI) mBaiduSpeechAI.releaseBaiduSpeech();
        if (null != mBaiduUnitAI) mBaiduUnitAI.releaseBaiduUnit();
        if (null != mBaiduTTSAI) mBaiduTTSAI.releaseBaiduTTS();
        if (null != mBaiduOcrAI) mBaiduOcrAI.releaseBaiduOCR();
        if (null != mBaiduFaceOfflineAI) mBaiduFaceOfflineAI.releaseBaiduFace();
        if (null != mBaiduFaceOnlineAI) mBaiduFaceOnlineAI.releaseBaiduFace();
        if (null != mBaiduClassifyImageAI) mBaiduClassifyImageAI.releaseBaiduClassifyImage();
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
/*
        mActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSpeechEnable) startTTS(welcome);
            }
        }, 3000);
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final String CROP_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_main.jpg";
        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                relaunchApp();
                break;
            case OCR_CAMERA_REQUEST_CODE:
                final boolean ocrQuestion = data.getBooleanExtra(GlobalValue.INTENT_UNIT_QUESTION, false);
                final String ocrCameraPath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
                String language = data.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE);
                final String ocrLanguage = TextUtils.isEmpty(language)? mLastOCRLanguage : language;

                if (!TextUtils.isEmpty(ocrCameraPath)) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    mBaiduOcrAI.setLanguageType(ocrLanguage);
                    mBaiduOcrAI.setDetectDirection(true);
                    mBaiduOcrAI.baiduOCRText(ocrCameraPath, ocrQuestion);
                }
                break;
            case OCR_IMAGE_REQUEST_CODE:
                if (!TextUtils.isEmpty(CROP_IMAGE_FILE_PATH)) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    mBaiduOcrAI.setLanguageType(mLastOCRLanguage);
                    mBaiduOcrAI.setDetectDirection(true);
                    mBaiduOcrAI.baiduOCRText(CROP_IMAGE_FILE_PATH, false);
                }
                break;
            case FACE_IDENTIFY_CAMERA_REQUEST_CODE:
            case FACE_DETECT_CAMERA_REQUEST_CODE:
                if (null != mBaiduFaceOnlineAI) {
                    final boolean faceQuestion = data.getBooleanExtra(GlobalValue.INTENT_UNIT_QUESTION, false);
                    final String faceCameraPath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
                    final String faceDetect = data.getStringExtra(GlobalValue.INTENT_EXTRA_FUN);
                    if (!TextUtils.isEmpty(faceCameraPath)) {
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        if (!TextUtils.isEmpty(faceDetect) || FACE_DETECT_CAMERA_REQUEST_CODE == requestCode) {
                            mBaiduFaceOnlineAI.onDetect(faceCameraPath);
                        } else {
                            mBaiduFaceOnlineAI.onIdentify(faceCameraPath, faceQuestion);
                        }
                    }
                } else if (null != mBaiduFaceOfflineAI) {
                    String faceCameraUserName = data.getStringExtra("FACE_USER_NAME");
                    triggerQuery("谁是" + faceCameraUserName);
                }
                break;
            case FACE_IDENTIFY_IMAGE_REQUEST_CODE:
            case FACE_DETECT_IMAGE_REQUEST_CODE:
                if (null != mBaiduFaceOnlineAI) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    if (FACE_IDENTIFY_IMAGE_REQUEST_CODE == requestCode) {
                        mBaiduFaceOnlineAI.onIdentify(CROP_IMAGE_FILE_PATH, false);
                    } else {
                        mBaiduFaceOnlineAI.onDetect(CROP_IMAGE_FILE_PATH);
                    }
                } else if (null != mBaiduFaceOfflineAI) {
                    mBaiduFaceOfflineAI.identifyUser(CROP_IMAGE_FILE_PATH);
                }
                break;
            case CLASSIFY_CAMERA_REQUEST_CODE:
                final boolean classifyCameraQuestion = data.getBooleanExtra(GlobalValue.INTENT_UNIT_QUESTION, false);
                final String classifyCameraPath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
                int type = data.getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, -1);
                final int classifyImageType = type >= 0? type : BaiduClassifyImageAI.CLASSIFY_TYPE_ADVANCED_GENERAL;
                if (!TextUtils.isEmpty(classifyCameraPath)) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    mBaiduClassifyImageAI.classifyImageThread(classifyImageType,
                            classifyCameraPath, classifyCameraQuestion);
                }
                break;
            case CLASSIFY_IMAGE_REQUEST_CODE:
                if (!TextUtils.isEmpty(CROP_IMAGE_FILE_PATH)) {
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    mBaiduClassifyImageAI.classifyImageThread(mLastClassifyImageType,
                            CROP_IMAGE_FILE_PATH, false);
                }
                break;
            case IMAGE_SELECT_REQUEST_CODE:
                SystemUtil.cropSelectImage(this, data.getData(), mCropImageRequestCode, CROP_IMAGE_FILE_PATH);
                break;
        }
    }

    // 请求权限
    public boolean applyApkPermissions() {
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
                return false;
            }
        }
        return true;
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
            relaunchApp();
        }
    }

    /*功能模块监听回调*/
    private BaiduSpeechAI.OnSpeechListener mSpeechListener = new BaiduSpeechAI.OnSpeechListener() {
        @Override
        public void onShowDebugInfo(final String info) {
            if (mSettingResult.mDebug) {
                showDebugInfo(info, false);
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

    private BaiduUnitAI.OnUnitListener mUnitListener = new BaiduUnitAI.OnUnitListener() {
        @Override
        public void onShowDebugInfo(final String info, final boolean reset) {
            if (mSettingResult.mDebug) {
                showDebugInfo(info, reset);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFinalResponse(question, answer, exchange);
                    showHint(hint);
                }
            });
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

    private BaiduTTSAI.OnTTSListener mTTSListener = new BaiduTTSAI.OnTTSListener() {
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

    private BaiduOcrAI.OnOCRListener mOCRListener = new BaiduOcrAI.OnOCRListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
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
                    }
                } else if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (question) {
                        triggerQuery(result);
                    } else {
                        showFinalResponse(result, (mBaiduUnitAI.getTranslateFixAction().getTranslateType() != TranslateFixAction.TRANSLATE));
                    }
                }
            }
        }
    };

    private BaiduClassifyImageAI.OnClassifyImageListener mClassifyImageListener = new BaiduClassifyImageAI.OnClassifyImageListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final String result, final String description, boolean question) {
            if (question && !TextUtils.isEmpty(result)) {
                triggerQuery(result);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFinalResponse(result, description, false);
                    }
                });
                startTTS(result);
            }
        }
    };

    private BaiduFaceOfflineAI.OnFaceOfflineListener mFaceOfflineListener = new BaiduFaceOfflineAI.OnFaceOfflineListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String faceName = (String)result;
                    if (TextUtils.isEmpty(faceName)) {
                        showFinalResponse("不认识此人", false);
                        startTTS("不认识此人");
                    } else {
                        showFinalResponse(faceName, false);
                        startTTS(faceName);
                    }
                }
            });
        }
    };

    private BaiduFaceOnlineAI.OnFaceOnlineListener mFaceOnlineListener = new BaiduFaceOnlineAI.OnFaceOnlineListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION:
                case BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION:
                    final String userInfo = (String)result;
                    if (TextUtils.isEmpty(userInfo)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFinalResponse("不认识此人", false);
                            }
                        });
                        startTTS("不认识此人");
                    } else {
                        if (BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION == resultType) {
                            triggerQuery(userInfo);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showFinalResponse(userInfo, false);
                                }
                            });
                            startTTS(userInfo);
                        }
                    }
                    break;
                case BaiduFaceOnlineAI.FACE_DETECT_ACTION:
                    final String detailInfo = (String)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFinalResponse(detailInfo, false);
                        }
                    });
                    startTTS(detailInfo);
                    break;
            }
        }
    };

    private void relaunchApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        }
        finish();
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

        mBaiduUnitAI.getBaiduKeyboardUnitThread(filterResult);
    }

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

    /*主界面下方按钮触发功能*/
    private void onSwitchVoiceAssistant() {
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

    private void onSwitchTranslateLanguage(boolean chineseToForeign) {
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

    /*菜单功能选择*/
    private void onShowManageDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_translate_start));
        itemList.add(getString(R.string.option_ocr));
        itemList.add(getString(R.string.option_face_identify));
        itemList.add(getString(R.string.option_image_classify));
        itemList.add(getString(R.string.option_text_input));
        itemList.add(getString(R.string.option_clear_all_items));

        if (mSettingResult.mEnableExtraFun) {
            itemList.add(getString(R.string.option_face_detect));
            itemList.add(getString(R.string.option_face_merge));
            itemList.add(getString(R.string.option_face_compare));
        }
        String[] items = itemList.toArray(new String[0]);

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
                            onSwitchTranslateLanguage("");//exit translate
                        } else {
                            onShowTranslateLanguageDialog();
                        }
                        break;
                    case 1: //快捷OCR
                        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                            onShowOCRInputDialog(mLastOCRLanguage);
                        } else {
                            onShowOCRLanguageDialog();
                        }
                        break;
                    case 2: //人脸识别
                        onShowFaceIdentifyInputDialog();
                        break;
                    case 3: //图像物体识别
                        onShowClassifyTypeDialog();
                        break;
                    case 4: //输入文本框
                        onSwitchInputText();
                        break;
                    case 5: //清除列表中所有显示
                        onClearAllItems();
                        break;
                    case 6: //额外功能，人脸检测
                        onShowFaceDetectInputDialog();
                        break;
                    case 7: //额外功能，人脸融合
                        onShowFaceMerge();
                        break;
                    case 8: //额外功能，人脸比较
                        onShowFaceCompare();
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
                onSwitchTranslateLanguage(items[which]);
            }
        });
        listDialog.show();
    }

    private void onSwitchTranslateLanguage(String language) {
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

    private void onShowOCRLanguageDialog() {
        final String[] items = getResources().getStringArray(R.array.ocr_language_item);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                onShowOCRInputDialog(items[which]);
            }
        });
        listDialog.show();
    }

    private void onShowOCRInputDialog(final String language) {
        final String[] items = {
                getString(R.string.option_ocr_camera),
                getString(R.string.option_ocr_image),
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
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);
                        mCropImageRequestCode = OCR_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onOCRCameraActivity(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.OCR_TYPE);
        intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, mLastOCRLanguage);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_ocr.jpg");
        startActivityForResult(intent, OCR_CAMERA_REQUEST_CODE);
    }

    private void onShowFaceIdentifyInputDialog() {
        final String[] items = {
                getString(R.string.option_face_camera),
                getString(R.string.option_face_image),
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
                        onFaceIdentifyCamera();
                        break;
                    case 1:
                        mCropImageRequestCode = FACE_IDENTIFY_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onFaceIdentifyCamera() {
        if (null != mBaiduFaceOnlineAI) {
            Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.IDENTIFY_FACE_TYPE);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                    getFilesDir().getAbsolutePath() + File.separator + "camera_face_identify.jpg");
            startActivityForResult(intent, FACE_IDENTIFY_CAMERA_REQUEST_CODE);
        } else if (null != mBaiduFaceOfflineAI) {
            showFinalResponse(getString(R.string.baidu_face_identify_start), false);
            mBaiduFaceOfflineAI.onFaceDetect(FACE_IDENTIFY_CAMERA_REQUEST_CODE);
        }
    }

    void onShowFaceDetectInputDialog() {
        final String[] items = {
                getString(R.string.option_face_camera),
                getString(R.string.option_face_image),
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
                        onFaceDetectCamera();
                        break;
                    case 1:
                        mCropImageRequestCode = FACE_DETECT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onFaceDetectCamera() {
        if (null != mBaiduFaceOnlineAI) {
            Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.DETECT_FACE_TYPE);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                    getFilesDir().getAbsolutePath() + File.separator + "camera_face_detect.jpg");
            startActivityForResult(intent, FACE_DETECT_CAMERA_REQUEST_CODE);
        } /*else if (null != mBaiduFaceOfflineAI) {
        }*/
    }

    private void onShowFaceMerge() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceMerge();
        } /*else if(null != mBaiduFaceOfflineAI) {
        }*/
    }

    private void onShowFaceCompare() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceCompare();
        } /*else if(null != mBaiduFaceOfflineAI) {
        }*/
    }

    private void onShowClassifyTypeDialog() {
        final String[] items = getResources().getStringArray(R.array.classify_image_type_item);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                onShowClassifyImageInputDialog(which);
            }
        });
        listDialog.show();
    }

    private void onShowClassifyImageInputDialog(final int classifyType) {
        final String[] items = {
                getString(R.string.option_image_classify_camera),
                getString(R.string.option_image_classify_image),
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
                        onClassifyImageCameraActivity(classifyType);
                        break;
                    case 1:
                        mLastClassifyImageType = classifyType;
                        mCropImageRequestCode = CLASSIFY_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onClassifyImageCameraActivity(int classifyType) {
        mLastClassifyImageType = classifyType;

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, classifyType);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.CLASSIFY_IMAGE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_classify_image.jpg");
        startActivityForResult(intent, CLASSIFY_CAMERA_REQUEST_CODE);
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

    /*系统帮助选项*/
    private void onShowAssistantDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_show_setting));
        itemList.add(getString(R.string.option_show_help));
        if (mSettingResult.mEnableExtraFun) {
            itemList.add(getString(R.string.option_show_face_manger));
        }
        final String[] items = itemList.toArray(new String[0]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: // 系统设置
                        onShowSettings();
                        break;
                    case 1: // 显示帮助
                        onShowHelp();
                        break;
                    case 2: // 人脸库管理
                        onShowFaceManager();
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowFaceManager() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceManager();
        } else if(null != mBaiduFaceOfflineAI) {
            mBaiduFaceOfflineAI.onFaceManager();
        }
    }

    private void onShowHelp() {
        switch (mCurUILever) {
            case MAIN_UI_LEVER:
                startActivity(new Intent(MainActivity.this, HelpInfoActivity.class));
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

    private void onShowSettings() {
        startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), SETTING_REQUEST_CODE);
    }

    /*选中列表任一条目的功能选项*/
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

                doListItemOption(which, view, position);
            }
        });
        listDialog.show();
    }

    private void doListItemOption(int option, View view, int position) {
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
                WebSearchAction.getInstance(this).searchAction(text);
                break;
        }
    }


    private void focusListView() {
        mResultMsgListView.requestFocus();
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
            if (GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA.equals(action)) {
                onOCRCameraActivity(intent.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE));
            }
        }
    };

    /*在UI界面上显示信息*/
    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

    private void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    private void showDebugInfo(final String info, final boolean reset) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            subShowDebugInfo(info, reset);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subShowDebugInfo(info, reset);
                }
            });
        }
    }

    private void subShowDebugInfo(String info, boolean reset) {
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
        hideProgressDialog();

        if (reverse) {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_RECEIVED));
        } else {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_SEND));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    private void showFinalResponse(String first, String second, boolean reverse) {
        hideProgressDialog();

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

    private void showErrorMsg(final String msg) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            hideProgressDialog();
            mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED));
            ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
            mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED));
                    ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
                    mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
                }
            });
        }
    }

    /*以下支持按键功能机*/
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
                    onSwitchTranslateLanguage(true);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onSwitchTranslateLanguage(false);
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
                    onShowSettings();
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
                if(mShortPress) doListItemOption(mLastItemOptionFunction, null, -1);
                //Don't handle long press here, because the user will have to get his finger back up first
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_3:
                if(mShortPress) {
                    if (OCR_CAMERA_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRCameraActivity(mLastOCRLanguage);
                    } else if (OCR_IMAGE_REQUEST_CODE == mLastOCROptionMode) {
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(mLastOCRLanguage);
                        mCropImageRequestCode = OCR_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(this, IMAGE_SELECT_REQUEST_CODE);
                    }
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_2:
                if(mShortPress) {
                    onFaceIdentifyCamera();
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_1:
                if(mShortPress) onSwitchTranslateLanguage(mLastTranslateLanguage);
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
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(mLastOCRLanguage);
                        mCropImageRequestCode = OCR_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(this, IMAGE_SELECT_REQUEST_CODE);
                    }
                } else {
                    onShowOCRLanguageDialog();
                }
                return true;
            case KeyEvent.KEYCODE_2:
                mShortPress = false;
                onShowFaceManager();
                return true;
            case KeyEvent.KEYCODE_1:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    onSwitchTranslateLanguage(mLastTranslateLanguage);
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
}
