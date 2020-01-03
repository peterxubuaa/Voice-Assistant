package com.fih.featurephone.voiceassistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.contentapprove.BaiduContentApproveAI;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.BaiduFaceOfflineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceIdentify;
import com.fih.featurephone.voiceassistant.baidu.humanbody.BaiduHumanBodyAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.baidu.tts.BaiduTTSAI;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.speechaction.FixBaseAction;
import com.fih.featurephone.voiceassistant.speechaction.TranslateFixAction;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;

import java.io.File;
import java.util.ArrayList;

abstract class BaiduAIActivity extends Activity {

    protected final int OCR_TEXT_CAMERA_REQUEST_CODE = 1;
    protected final int OCR_TEXT_IMAGE_REQUEST_CODE = 2;
    protected final int FACE_IDENTIFY_CAMERA_REQUEST_CODE = 3;
    protected final int FACE_IDENTIFY_IMAGE_REQUEST_CODE = 4;
    protected final int FACE_DETECT_CAMERA_REQUEST_CODE = 5;
    protected final int FACE_DETECT_IMAGE_REQUEST_CODE = 6;
    protected final int FACE_AUTHENTICATE_CAMERA_REQUEST_CODE = 7;
    protected final int FACE_AUTHENTICATE_IMAGE_REQUEST_CODE = 8;
    protected final int CLASSIFY_CAMERA_REQUEST_CODE = 9;
    protected final int CLASSIFY_IMAGE_REQUEST_CODE = 10;
    protected final int HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE = 11;
    protected final int HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE = 12;
    protected final int HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE = 13;
    protected final int HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE = 14;
    protected final int OCR_QRCODE_CAMERA_REQUEST_CODE = 15;
    protected final int OCR_QRCODE_IMAGE_REQUEST_CODE = 16;

    protected final int IMAGE_SELECT_REQUEST_CODE = 99;

    protected final int MAIN_UI_LEVER = 0x1;
    protected final int TRANSLATE_SUB_UI_LEVER = 0x2;
    protected final int POEM_SUB_UI_LEVER = 0x4;
    protected final int COUPLET_SUB_UI_LEVER = 0x8;

    private BaiduSpeechAI mBaiduSpeechAI;
    private BaiduUnitAI mBaiduUnitAI;
    private BaiduTTSAI mBaiduTTSAI;
    private BaiduOcrAI mBaiduOcrAI;
    private BaiduFaceOfflineAI mBaiduFaceOfflineAI;
    private BaiduFaceOnlineAI mBaiduFaceOnlineAI;
    private BaiduClassifyImageAI mBaiduClassifyImageAI;
    private BaiduHumanBodyAI mBaiduHumanBodyAI;
    private BaiduContentApproveAI mBaiduContentApproveAI;

    protected boolean mSpeechEnable = false;
    protected SettingActivity.SettingResult mSettingResult;
    protected Handler mActionHandler;

    protected boolean mSupportTouch = false;
    protected int mCurUILever = MAIN_UI_LEVER;
    protected String mLastOCRLanguage = BaiduOcrAI.OCR_DEFAULT_LANGUAGE;
    protected int mLastClassifyImageType = BaiduClassifyImageAI.CLASSIFY_TYPE_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionHandler = new Handler();
        mSettingResult = SettingActivity.getSavedSettingResults(this);
    }

    protected void initAssistant() {
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
        mBaiduOcrAI.init();

        if (GlobalValue.FACE_IDENTIFY_OFFLINE == mSettingResult.mFaceIdentifyMode) {
            mBaiduFaceOfflineAI = new BaiduFaceOfflineAI(this, mFaceOfflineListener);
            mBaiduFaceOfflineAI.initBaiduFace();
        } else {
            mBaiduFaceOnlineAI = new BaiduFaceOnlineAI(this, mFaceOnlineListener);
            mBaiduFaceOnlineAI.initBaiduFace();
        }

        mBaiduClassifyImageAI = new BaiduClassifyImageAI(this, mClassifyImageListener);
        mBaiduClassifyImageAI.init();

        mBaiduHumanBodyAI = new BaiduHumanBodyAI(this, mHumanBodyListener);
        mBaiduHumanBodyAI.init();

        mBaiduContentApproveAI = new BaiduContentApproveAI(this, mContentApproveListener);
        mBaiduContentApproveAI.init();
    }

    protected void releaseAssistant() {
        if (null != mBaiduSpeechAI) mBaiduSpeechAI.releaseBaiduSpeech();
        if (null != mBaiduUnitAI) mBaiduUnitAI.releaseBaiduUnit();
        if (null != mBaiduTTSAI) mBaiduTTSAI.releaseBaiduTTS();
        if (null != mBaiduOcrAI) mBaiduOcrAI.release();
        if (null != mBaiduFaceOfflineAI) mBaiduFaceOfflineAI.releaseBaiduFace();
        if (null != mBaiduFaceOnlineAI) mBaiduFaceOnlineAI.releaseBaiduFace();
        if (null != mBaiduClassifyImageAI) mBaiduClassifyImageAI.release();
        if (null != mBaiduHumanBodyAI) mBaiduHumanBodyAI.release();
        if (null != mBaiduContentApproveAI) mBaiduContentApproveAI.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final boolean question = data.getBooleanExtra(GlobalValue.INTENT_UNIT_QUESTION, false);
        final String filePath;
        if (requestCode % 2 == 0) { //crop image
            filePath = data.getStringExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH);
        } else { //camera shot
            filePath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
        }
        if (!FileUtils.isFileExist(filePath)) return;
        if (mSettingResult.mRequestImage) showFinalImageResponse(filePath, false);
        showProgressDialog(getResources().getString(R.string.baidu_unit_working));

        switch (requestCode) {
            case OCR_TEXT_CAMERA_REQUEST_CODE:
            case OCR_TEXT_IMAGE_REQUEST_CODE:
                String language = data.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE);
                mBaiduOcrAI.setLanguageType(language);
                mBaiduOcrAI.setDetectDirection(true);
                mBaiduOcrAI.onBaiduOCRText(filePath, question);
//              mBaiduOcrAI.onOCRText(ocrCameraPath, ocrQuestion, ocrLanguage);
                break;
            case OCR_QRCODE_CAMERA_REQUEST_CODE:
            case OCR_QRCODE_IMAGE_REQUEST_CODE:
                mBaiduOcrAI.onOCRQRCode(filePath, question);
                break;
            case FACE_IDENTIFY_CAMERA_REQUEST_CODE:
            case FACE_DETECT_CAMERA_REQUEST_CODE:
                if (null != mBaiduFaceOnlineAI) {
                    if (FACE_DETECT_CAMERA_REQUEST_CODE == requestCode) {
                        mBaiduFaceOnlineAI.onDetectThread(filePath);
                    } else {
                        if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == mSettingResult.mFaceIdentifyMode) {
                            mBaiduContentApproveAI.onPublicFigureThread(filePath, question);
                        } else {
                            mBaiduFaceOnlineAI.onIdentifyThread(filePath, question,
                                    mSettingResult.mFaceIdentifyMode == GlobalValue.FACE_IDENTIFY_ONLINE_MN ?
                                            FaceIdentify.FACE_IDENTIFY_M_FROM_N : FaceIdentify.FACE_IDENTIFY_1_FROM_N);
                        }
                    }
                } else if (null != mBaiduFaceOfflineAI) {
                    String faceName = data.getStringExtra(GlobalValue.INTENT_FACE_USER_NAME);
                    showFinalTextResponse(faceName, true);
                    startTTS(faceName);
                }
                break;
            case FACE_IDENTIFY_IMAGE_REQUEST_CODE:
            case FACE_DETECT_IMAGE_REQUEST_CODE:
                if (null != mBaiduFaceOnlineAI) {
                    if (FACE_IDENTIFY_IMAGE_REQUEST_CODE == requestCode) {
                        if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == mSettingResult.mFaceIdentifyMode) {
                            mBaiduContentApproveAI.onPublicFigureThread(filePath, question);
                        } else {
                            mBaiduFaceOnlineAI.onIdentifyThread(filePath, question,
                                    mSettingResult.mFaceIdentifyMode == GlobalValue.FACE_IDENTIFY_ONLINE_MN ?
                                            FaceIdentify.FACE_IDENTIFY_M_FROM_N : FaceIdentify.FACE_IDENTIFY_1_FROM_N);
                        }
                    } else {
                        mBaiduFaceOnlineAI.onDetectThread(filePath);
                    }
                } else if (null != mBaiduFaceOfflineAI) {
                    mBaiduFaceOfflineAI.identifyUser(filePath);
                }
                break;
            case FACE_AUTHENTICATE_CAMERA_REQUEST_CODE:
            case FACE_AUTHENTICATE_IMAGE_REQUEST_CODE:
                if (null != mBaiduFaceOnlineAI) {
                    final String faceName = data.getStringExtra(GlobalValue.INTENT_FACE_NAME);
                    final String faceIdCardNum = data.getStringExtra(GlobalValue.INTENT_FACE_ID_CARD_NUM);
                    mBaiduFaceOnlineAI.onAuthenticateThread(filePath, faceName, faceIdCardNum);
                }
                break;
            case CLASSIFY_CAMERA_REQUEST_CODE:
            case CLASSIFY_IMAGE_REQUEST_CODE:
                int classifyType = data.getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, BaiduClassifyImageAI.CLASSIFY_TYPE_DEFAULT);
                mBaiduClassifyImageAI.onClassifyImageThread(classifyType,
                        filePath, question);
                break;
            case HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE:
            case HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE:
                mBaiduHumanBodyAI.onRecognizeGestureThread(filePath);
                break;
            case HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE:
            case HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE:
                mBaiduHumanBodyAI.onHeadCountThread(filePath);
                break;
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
                    showFinalTextResponse(question, answer, exchange);
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

    private BaiduBaseAI.IBaiduBaseListener mOCRListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, int type) {
            if (TextUtils.isEmpty((String)result)) {
                showFinalTextResponse(getString(R.string.baidu_unit_ocr_fail),true);
            } else {
                if (MAIN_UI_LEVER == mCurUILever) {
                    if (BaiduOcrAI.OCR_TEXT_QUESTION_ACTION == type) {
                        triggerQuery((String)result);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFinalTextResponse((String)result, true);
                                startTTS((String)result);
                            }
                        });
                    }
                } else if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (BaiduOcrAI.OCR_TEXT_QUESTION_ACTION == type) {
                        triggerQuery((String)result);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFinalTextResponse((String)result, (mBaiduUnitAI.getTranslateFixAction().getTranslateType() != TranslateFixAction.TRANSLATE));
                                startTTS((String)result);
                            }
                        });
                    }
                }
            }
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mClassifyImageListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, int resultType) {
            if (BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION == resultType) {
                triggerQuery((String)result);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showFinalTextResponse((String)result, true);
                    }
                });
                startTTS((String)result);
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
                        showFinalTextResponse(getString(R.string.baidu_face_identify_fail), true);
                        startTTS(getString(R.string.baidu_face_identify_fail));
                    } else {
                        showFinalTextResponse(faceName, true);
                        startTTS(faceName);
                    }
                }
            });
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mFaceOnlineListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION:
                case BaiduFaceOnlineAI.FACE_IDENTIFY_IMAGE_ACTION:
                case BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION:
                    final String responseIdentify = (String)result;
                    if (TextUtils.isEmpty(responseIdentify)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showFinalTextResponse(getString(R.string.baidu_face_identify_fail), true);
                            }
                        });
                        startTTS(getString(R.string.baidu_face_identify_fail));
                    } else {
                        if (BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION == resultType) {
                            triggerQuery(responseIdentify);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (BaiduFaceOnlineAI.FACE_IDENTIFY_IMAGE_ACTION == resultType) {
                                        showFinalImageResponse(responseIdentify, true);
                                    } else {
                                        showFinalTextResponse(responseIdentify, true);
                                        startTTS(responseIdentify);
                                    }
                                }
                            });
                        }
                    }
                    break;
                case BaiduFaceOnlineAI.FACE_DETECT_ACTION:
                case BaiduFaceOnlineAI.FACE_DETECT_IMAGE_ACTION:
                    final String responseDetect = (String)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (BaiduFaceOnlineAI.FACE_DETECT_IMAGE_ACTION == resultType) {
                                showFinalImageResponse(responseDetect, true);
                            } else {
                                showFinalTextResponse(responseDetect, true);
                                startTTS(responseDetect);
                            }
                        }
                    });
                    break;
                case BaiduFaceOnlineAI.FACE_AUTHENTICATE_ACTION:
                case BaiduFaceOnlineAI.FACE_AUTHENTICATE_IMAGE_ACTION:
                    final String responseAuthenticate = (String)result;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (BaiduFaceOnlineAI.FACE_AUTHENTICATE_IMAGE_ACTION == resultType) {
                                showFinalImageResponse(responseAuthenticate, true);
                            } else {
                                showFinalTextResponse(responseAuthenticate, true);
                                startTTS(responseAuthenticate);
                            }
                        }
                    });
                    break;
            }
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mHumanBodyListener = new BaiduBaseAI.IBaiduBaseListener()  {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduHumanBodyAI.GESTURE_ACTION:
                case BaiduHumanBodyAI.GESTURE_IMAGE_ACTION:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String response = (String)result;
                            if (TextUtils.isEmpty(response)) {
                                showFinalTextResponse(getString(R.string.baidu_human_body_gesture_fail), true);
                                startTTS(getString(R.string.baidu_human_body_gesture_fail));
                            } else {
                                if (BaiduHumanBodyAI.GESTURE_ACTION == resultType) {
                                    showFinalTextResponse(response, true);
                                    startTTS(response);
                                } else {
                                    showFinalImageResponse(response, true);
                                }
                            }
                        }
                    });
                    break;
                case BaiduHumanBodyAI.HEAD_COUNT_ACTION:
                case BaiduHumanBodyAI.HEAD_COUNT_IMAGE_ACTION:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String response = (String)result;
                            if (BaiduHumanBodyAI.HEAD_COUNT_ACTION == resultType) {
                                showFinalTextResponse(response, true);
                                startTTS(response);
                            } else {
                                showFinalImageResponse(response, true);//显示图片
                            }
                        }
                    });
                    break;
            }
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mContentApproveListener = new BaiduBaseAI.IBaiduBaseListener()  {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduContentApproveAI.PUBLIC_FIGURE_ACTION:
                case BaiduContentApproveAI.PUBLIC_FIGURE_QUESTION_ACTION:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<String> nameList = (ArrayList<String>)result;
                            if (null == nameList || nameList.size() == 0) {
                                showFinalTextResponse(getString(R.string.baidu_face_identify_fail), true);
                                startTTS(getString(R.string.baidu_face_identify_fail));
                            } else {
                                for (String name : nameList) {
                                    if (BaiduContentApproveAI.PUBLIC_FIGURE_QUESTION_ACTION == resultType) {
                                        triggerQuery(name);
                                    } else {
                                        showFinalTextResponse(name, true);
                                        startTTS(name);
                                    }
                                }
                            }
                        }
                    });
                    break;
            }
        }
    };

    protected void triggerQuery(String result) {
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

    protected void triggerQuery(final Bitmap bitmap) {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_ocr));
        itemList.add(getString(R.string.option_face_identify));
        itemList.add(getString(R.string.option_image_classify));
        if (mSettingResult.mEnableExtraFun) {
            itemList.add(getString(R.string.option_face_detect));
            itemList.add(getString(R.string.option_human_body_gesture));
            itemList.add(getString(R.string.option_human_body_headcount));
            itemList.add(getString(R.string.option_ocr_qrcode));
        }
        final String[] items = itemList.toArray(new String[0]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(BaiduAIActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String ITEM_IMAGE_FILE_PATH =
                        FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "item_main.jpg";
                if (FileUtils.isFileExist(ITEM_IMAGE_FILE_PATH)) FileUtils.deleteFile(ITEM_IMAGE_FILE_PATH);
                BitmapUtils.saveBitmapToJpeg(bitmap, ITEM_IMAGE_FILE_PATH);
                // which 下标从0开始
                switch (which) {
                    case 0: // OCR文字识别
                        final String[] items = getResources().getStringArray(R.array.ocr_language_item);
                        AlertDialog.Builder listDialog =
                                new AlertDialog.Builder(BaiduAIActivity.this);
                        listDialog.setTitle(getString(R.string.option_dialog_title));
                        listDialog.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // which 下标从0开始
                                if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                                mBaiduOcrAI.setLanguageType(items[which]);//mLastOCRLanguage);
                                mBaiduOcrAI.setDetectDirection(true);
                                mBaiduOcrAI.onBaiduOCRText(ITEM_IMAGE_FILE_PATH, false);
                            }
                        });
                        listDialog.show();
                        break;
                    case 1: // 人脸识别
                        if (null != mBaiduFaceOnlineAI) {
                            if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                            showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                            if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == mSettingResult.mFaceIdentifyMode) {
                                mBaiduContentApproveAI.onPublicFigureThread(ITEM_IMAGE_FILE_PATH, false);
                            } else {
                                mBaiduFaceOnlineAI.onIdentifyThread(ITEM_IMAGE_FILE_PATH, false,
                                        mSettingResult.mFaceIdentifyMode == GlobalValue.FACE_IDENTIFY_ONLINE_MN ?
                                                FaceIdentify.FACE_IDENTIFY_M_FROM_N : FaceIdentify.FACE_IDENTIFY_1_FROM_N);
                            }
                        } else if (null != mBaiduFaceOfflineAI) {
                            mBaiduFaceOfflineAI.identifyUser(ITEM_IMAGE_FILE_PATH);
                        }
                        break;
                    case 2: // 物体识别
                        if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        mBaiduClassifyImageAI.onClassifyImageThread(mLastClassifyImageType,
                                ITEM_IMAGE_FILE_PATH, false);
                        break;
                    case 3: // 人脸检测
                        if (null != mBaiduFaceOnlineAI) {
                            if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                            showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                            mBaiduFaceOnlineAI.onDetectThread(ITEM_IMAGE_FILE_PATH);
                        } else if (null != mBaiduFaceOfflineAI) {
                            mBaiduFaceOfflineAI.identifyUser(ITEM_IMAGE_FILE_PATH);
                        }
                        break;
                    case 4: // 手势识别
                        if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        mBaiduHumanBodyAI.onRecognizeGestureThread(ITEM_IMAGE_FILE_PATH);
                        break;
                    case 5: // 清点人头
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        mBaiduHumanBodyAI.onHeadCountThread(ITEM_IMAGE_FILE_PATH);
                        break;
                    case 6: // 二维码识别
                        if (mSettingResult.mRequestImage) showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                        showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                        mBaiduOcrAI.onOCRQRCode(ITEM_IMAGE_FILE_PATH, false);
                        break;
                }
            }
        });
        listDialog.show();
    }

    protected void startTTS(String text) {
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
    protected void onSwitchVoiceAssistant() {
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

    protected void onSwitchTranslateLanguage(boolean chineseToForeign) {
        if (null == mBaiduUnitAI || TRANSLATE_SUB_UI_LEVER != mCurUILever) return;

        if (chineseToForeign) {
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.INVISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
            }
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);
            showFinalTextResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage()), false);
        } else {
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.INVISIBLE);
            }
            if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduSpeechAI.BAIDU_SPEECH_ENGLISH);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.REVERSE_TRANSLATE);
            showFinalTextResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getTargetLanguage()), true);
        }
    }

    protected void onSwitchTranslateLanguage(String language) {
        if (null == mBaiduUnitAI) return;

        if (null != mBaiduSpeechAI) mBaiduSpeechAI.initBaiduSpeechSettings(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
        mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);

        if (TRANSLATE_SUB_UI_LEVER != mCurUILever) {
            mBaiduUnitAI.getTranslateFixAction().forceAction(true);
            mBaiduUnitAI.getTranslateFixAction().setTargetLanguage(language);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_start), language);
            showFinalTextResponse(msg,false);
            startTTS(msg);

            mCurUILever = TRANSLATE_SUB_UI_LEVER;
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
            }
        } else {
            mBaiduUnitAI.getTranslateFixAction().forceAction(false);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_stop), language);
            showFinalTextResponse(msg, false);
            startTTS(msg);

            mCurUILever = MAIN_UI_LEVER;
            if (mSupportTouch) {
                findViewById(R.id.left_arrow_image_view).setVisibility(View.GONE);
                findViewById(R.id.right_arrow_image_view).setVisibility(View.GONE);
            }
        }
    }

    protected void onFaceIdentifyCamera() {
        if (null != mBaiduFaceOnlineAI) {
            Intent intent = new Intent(BaiduAIActivity.this, CameraCaptureActivity.class);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.IDENTIFY_FACE_TYPE);
            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                    getFilesDir().getAbsolutePath() + File.separator + "camera_face_identify.jpg");
            startActivityForResult(intent, FACE_IDENTIFY_CAMERA_REQUEST_CODE);
        } else if (null != mBaiduFaceOfflineAI) {
            showFinalTextResponse(getString(R.string.baidu_face_identify_start), false);
            mBaiduFaceOfflineAI.onFaceDetect(FACE_IDENTIFY_CAMERA_REQUEST_CODE);
        }
    }

    protected void onShowFaceMerge() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceMergeActivity();
        }
    }

    protected void onShowFaceCompare() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceCompareActivity();
        }
    }

    protected void onShowFaceManager() {
        if (null != mBaiduFaceOnlineAI) {
            mBaiduFaceOnlineAI.onFaceManagerActivity();
        } else if(null != mBaiduFaceOfflineAI) {
            mBaiduFaceOfflineAI.onFaceManager();
        }
    }

    protected String getAndShowMatchedOCRLanguage(String language) {
        if (TRANSLATE_SUB_UI_LEVER == mCurUILever && null != mBaiduUnitAI) {
            if (mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.TRANSLATE) {
                language = mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage();
                showFinalTextResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
            } else {
                language = mBaiduUnitAI.getTranslateFixAction().getTargetLanguage();
                showFinalTextResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), true);
            }
        } else {
            showFinalTextResponse(String.format(getString(R.string.baidu_unit_ocr_start), language), false);
        }

        return language;
    }

    abstract protected void showFinalTextResponse(String first, boolean reverse);
    abstract protected void showFinalTextResponse(String first, String second, boolean reverse);
    abstract protected void showFinalImageResponse(String imageFilePath, boolean reverse);
    abstract protected void showErrorMsg(final String msg);
    abstract protected void showDebugInfo(final String info, final boolean reset);
    abstract protected void showHint(String hint);
    abstract protected void showProgressDialog(String msg);
    abstract protected void hideProgressDialog();
}
