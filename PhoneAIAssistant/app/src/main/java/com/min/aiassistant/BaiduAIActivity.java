package com.min.aiassistant;

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

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.contentapprove.BaiduContentApproveAI;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.model.FaceIdentify;
import com.min.aiassistant.baidu.humanbody.BaiduHumanBodyAI;
import com.min.aiassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.min.aiassistant.baidu.imageprocess.BaiduImageProcessAI;
import com.min.aiassistant.baidu.nlp.BaiduNLPAI;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.speech.BaiduSpeechAI;
import com.min.aiassistant.baidu.tts.BaiduTTSAI;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.picture.CameraCaptureActivity;
import com.min.aiassistant.speechaction.FixBaseAction;
import com.min.aiassistant.speechaction.TranslateFixAction;
import com.min.aiassistant.utils.BitmapUtils;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.GlobalValue;

import java.io.File;
import java.util.ArrayList;

abstract class BaiduAIActivity extends Activity {
    //XXX_CAMERA_REQUEST_CODE必须是单数， XXX_IMAGE_REQUEST_CODE必须是双数！
    protected final int OCR_TEXT_CAMERA_REQUEST_CODE = 1;
    protected final int OCR_TEXT_IMAGE_REQUEST_CODE = 2;
    protected final int OCR_QRCODE_CAMERA_REQUEST_CODE = 3;
    protected final int OCR_QRCODE_IMAGE_REQUEST_CODE = 4;
    protected final int OCR_HANDWRITING_CAMERA_REQUEST_CODE = 5;
    protected final int OCR_HANDWRITING_IMAGE_REQUEST_CODE = 6;
    protected final int FACE_IDENTIFY_CAMERA_REQUEST_CODE = 11;
    protected final int FACE_IDENTIFY_IMAGE_REQUEST_CODE = 12;
    protected final int FACE_DETECT_CAMERA_REQUEST_CODE = 13;
    protected final int FACE_DETECT_IMAGE_REQUEST_CODE = 14;
    protected final int FACE_AUTHENTICATE_CAMERA_REQUEST_CODE = 15;
    protected final int FACE_AUTHENTICATE_IMAGE_REQUEST_CODE = 16;
    protected final int CLASSIFY_CAMERA_REQUEST_CODE = 21;
    protected final int CLASSIFY_IMAGE_REQUEST_CODE = 22;
    protected final int HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE = 31;
    protected final int HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE = 32;
    protected final int HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE = 33;
    protected final int HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE = 34;
    protected final int HUMAN_BODY_SEGMENT_CAMERA_REQUEST_CODE = 35;
    protected final int HUMAN_BODY_SEGMENT_IMAGE_REQUEST_CODE = 36;
    protected final int IMAGE_PROCESS_SELFIE_ANIME_CAMERA_REQUEST_CODE = 41;
    protected final int IMAGE_PROCESS_SELFIE_ANIME_IMAGE_REQUEST_CODE = 42;
    protected final int IMAGE_PROCESS_COLOURIZE_CAMERA_REQUEST_CODE = 43;
    protected final int IMAGE_PROCESS_COLOURIZE_IMAGE_REQUEST_CODE = 44;
    protected final int IMAGE_PROCESS_STYLE_TRANS_CAMERA_REQUEST_CODE = 45;
    protected final int IMAGE_PROCESS_STYLE_TRANS_IMAGE_REQUEST_CODE = 46;

    protected final int IMAGE_SELECT_REQUEST_CODE = 99;

    protected final int MAIN_UI_LEVER = 0x1;
    protected final int TRANSLATE_SUB_UI_LEVER = 0x2;
    protected final int POEM_SUB_UI_LEVER = 0x4;
    protected final int COUPLET_SUB_UI_LEVER = 0x8;
    protected final int TEXT_INPUT_SUB_UI_LEVER = 0x10;

    protected BaiduSpeechAI mBaiduSpeechAI;
    protected BaiduUnitAI mBaiduUnitAI;
    private BaiduTTSAI mBaiduTTSAI;
    private BaiduOcrAI mBaiduOcrAI;
    private BaiduFaceOnlineAI mBaiduFaceOnlineAI;
    private BaiduClassifyImageAI mBaiduClassifyImageAI;
    private BaiduHumanBodyAI mBaiduHumanBodyAI;
    private BaiduContentApproveAI mBaiduContentApproveAI;
    private BaiduImageProcessAI mBaiduImageProcessAI;
    protected BaiduNLPAI mBaiduNLPAI;

    protected boolean mSpeechEnable = false;
    protected SettingActivity.SettingResult mSettingResult;
    protected Handler mActionHandler;

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
        mBaiduSpeechAI = new BaiduSpeechAI(this, mSpeechListener);
        mBaiduSpeechAI.initBaiduSpeech();
        mBaiduSpeechAI.setLanguageType(mSettingResult.mSpeechType);

        mBaiduTTSAI = new BaiduTTSAI(this, mTTSListener);
        mBaiduTTSAI.initBaiduTTS();
        mBaiduTTSAI.setTTSVoiceType(mSettingResult.mTTSVoiceType);

        mBaiduOcrAI = new BaiduOcrAI(this, mOCRListener);
        mBaiduOcrAI.init();

        mBaiduUnitAI = new BaiduUnitAI(this, mUnitListener, mSettingResult.mBotTypeList);
        mBaiduUnitAI.init();

        mBaiduFaceOnlineAI = new BaiduFaceOnlineAI(this, mFaceOnlineListener);
        mBaiduFaceOnlineAI.init();

        mBaiduClassifyImageAI = new BaiduClassifyImageAI(this, mClassifyImageListener);
        mBaiduClassifyImageAI.init();

        mBaiduHumanBodyAI = new BaiduHumanBodyAI(this, mHumanBodyListener);
        mBaiduHumanBodyAI.init();

        mBaiduContentApproveAI = new BaiduContentApproveAI(this, mContentApproveListener);
        mBaiduContentApproveAI.init();

        mBaiduImageProcessAI = new BaiduImageProcessAI(this, mImageProcessListener);
        mBaiduImageProcessAI.init();

        mBaiduNLPAI = new BaiduNLPAI(this, mNLPListener);
        mBaiduNLPAI.init();
    }

    protected void releaseAssistant() {
        mBaiduSpeechAI.releaseBaiduSpeech();
        mBaiduUnitAI.release();
        mBaiduTTSAI.releaseBaiduTTS();
        mBaiduOcrAI.release();
        mBaiduFaceOnlineAI.release();
        mBaiduClassifyImageAI.release();
        mBaiduHumanBodyAI.release();
        mBaiduContentApproveAI.release();
        mBaiduImageProcessAI.release();
        mBaiduNLPAI.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode || null == data) return;

        final boolean question = data.getBooleanExtra(GlobalValue.INTENT_UNIT_QUESTION, false);
        final String filePath;
        if (requestCode % 2 == 0) { //crop image
            filePath = data.getStringExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH);
        } else { //camera shot
            filePath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
        }
        if (!FileUtils.isFileExist(filePath)) return;
        if (mSettingResult.mRequestImage) {
            showFinalImageResponse(filePath, false);
        }
        showProgressDialog(getResources().getString(R.string.baidu_unit_working));

        switch (requestCode) {
            case OCR_TEXT_CAMERA_REQUEST_CODE:
            case OCR_TEXT_IMAGE_REQUEST_CODE:
                String language = data.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE);
                  mBaiduOcrAI.onOCRText(filePath, question, language);
                break;
            case OCR_QRCODE_CAMERA_REQUEST_CODE:
            case OCR_QRCODE_IMAGE_REQUEST_CODE:
                mBaiduOcrAI.action(BaiduOcrAI.QRCODE_TYPE, filePath);
                break;
            case OCR_HANDWRITING_CAMERA_REQUEST_CODE:
            case OCR_HANDWRITING_IMAGE_REQUEST_CODE:
                mBaiduOcrAI.action(BaiduOcrAI.HANDWRITING_TYPE, filePath);
                break;

            case FACE_IDENTIFY_CAMERA_REQUEST_CODE:
            case FACE_IDENTIFY_IMAGE_REQUEST_CODE:
                if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == mSettingResult.mFaceIdentifyMode) {
                    mBaiduContentApproveAI.action(filePath, question);
                } else {
                    mBaiduFaceOnlineAI.onIdentifyThread(filePath, question,
                            mSettingResult.mFaceIdentifyMode == GlobalValue.FACE_IDENTIFY_ONLINE_MN ?
                                    FaceIdentify.FACE_IDENTIFY_M_FROM_N : FaceIdentify.FACE_IDENTIFY_1_FROM_N);
                }
                break;
            case FACE_DETECT_CAMERA_REQUEST_CODE:
            case FACE_DETECT_IMAGE_REQUEST_CODE:
                mBaiduFaceOnlineAI.onDetectThread(filePath);
                break;
            case FACE_AUTHENTICATE_CAMERA_REQUEST_CODE:
            case FACE_AUTHENTICATE_IMAGE_REQUEST_CODE:
                final String faceName = data.getStringExtra(GlobalValue.INTENT_FACE_NAME);
                final String faceIdCardNum = data.getStringExtra(GlobalValue.INTENT_FACE_ID_CARD_NUM);
                mBaiduFaceOnlineAI.onAuthenticateThread(filePath, faceName, faceIdCardNum);
                break;

            case CLASSIFY_CAMERA_REQUEST_CODE:
            case CLASSIFY_IMAGE_REQUEST_CODE:
                int classifyType = data.getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, BaiduClassifyImageAI.CLASSIFY_TYPE_DEFAULT);
                mBaiduClassifyImageAI.action(classifyType, filePath, question);
                break;

            case HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE:
            case HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE:
                mBaiduHumanBodyAI.action(BaiduHumanBodyAI.GESTURE_TYPE, filePath);
                break;
            case HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE:
            case HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE:
                mBaiduHumanBodyAI.action(BaiduHumanBodyAI.HEADCOUNT_TYPE, filePath);
                break;
            case HUMAN_BODY_SEGMENT_CAMERA_REQUEST_CODE:
            case HUMAN_BODY_SEGMENT_IMAGE_REQUEST_CODE:
                mBaiduHumanBodyAI.action(BaiduHumanBodyAI.BODY_SEGMENT_TYPE, filePath);
                break;

            case IMAGE_PROCESS_SELFIE_ANIME_CAMERA_REQUEST_CODE:
            case IMAGE_PROCESS_SELFIE_ANIME_IMAGE_REQUEST_CODE:
                mBaiduImageProcessAI.action(BaiduImageProcessAI.SELFIE_ANIME_TYPE, filePath);
                break;
            case IMAGE_PROCESS_COLOURIZE_CAMERA_REQUEST_CODE:
            case IMAGE_PROCESS_COLOURIZE_IMAGE_REQUEST_CODE:
                mBaiduImageProcessAI.action(BaiduImageProcessAI.COLOURIZE_TYPE, filePath);
                break;
            case IMAGE_PROCESS_STYLE_TRANS_CAMERA_REQUEST_CODE:
            case IMAGE_PROCESS_STYLE_TRANS_IMAGE_REQUEST_CODE:
                mBaiduImageProcessAI.action(BaiduImageProcessAI.STYLE_TRANS_TYPE, filePath);
                break;
        }
    }

    /*功能模块监听回调*/
    private BaiduSpeechAI.OnSpeechListener mSpeechListener = new BaiduSpeechAI.OnSpeechListener() {
        @Override
        public void onExit() {
            mSpeechEnable = false;
            if (!mBaiduTTSAI.isTTSRunning()) {
                ((ImageView) findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
            }
        }

        @Override
        public void onFinalResult(String result, boolean query) {
            if (query) {
                triggerQuery(result);
            } else {
                showFinalTextResponse(result, false);
            }
        }

        @Override
        public void onError(final String msg) {
            if (msg.contains("Network is not available")) {
                showErrorMsg(getString(R.string.network_error));
            } else  {
                showErrorMsg(msg);
            }
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mUnitListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(String msg) {}

        @Override
        public void onFinalResult(Object result, int resultType) {
            switch (resultType) {
                case BaiduUnitAI.UNIT_NOTIFY:
                    Object[] objects = (Object[])result;
                    String botID = (String)objects[0];
                    int type = (int)objects[1];
                    int viewStatus = -1;
                    if (FixBaseAction.STOP_FIX_ACTION == type) {
                        mCurUILever = MAIN_UI_LEVER;
                        if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE.equals(botID)) {
                            viewStatus = View.GONE;
                            mBaiduSpeechAI.setLanguageType(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
                        }
                    } else {
                        if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE.equals(botID)) {
                            mCurUILever = TRANSLATE_SUB_UI_LEVER;
                            viewStatus = View.VISIBLE;
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
                    break;
                case BaiduUnitAI.UNIT_RESULT:
                    String[] results = (String[])result;
                    final String question = results[0];
                    final String answer = results[1];
                    final String hint = results[2];
                    if (TextUtils.isEmpty(answer)) return;
                    //为了翻译显示更合理
                    final boolean exchange = ((TRANSLATE_SUB_UI_LEVER & mCurUILever) > 0 &&
                            mBaiduUnitAI.getTranslateFixAction().getTranslateType() == TranslateFixAction.REVERSE_TRANSLATE);
                    showFinalTextResponse(question, answer, exchange);
                    showHint(hint);
                    startTTS(answer);
                    break;
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
                        showFinalTextResponse((String)result, true);
                        startTTS((String)result);
                    }
                } else if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (BaiduOcrAI.OCR_TEXT_QUESTION_ACTION == type) {
                        triggerQuery((String)result);
                    } else {
                        showFinalTextResponse((String)result, (mBaiduUnitAI.getTranslateFixAction().getTranslateType() != TranslateFixAction.TRANSLATE));
                        startTTS((String)result);
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
                showFinalTextResponse((String)result, true);
                startTTS((String)result);
            }
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
                        showFinalTextResponse(getString(R.string.baidu_face_identify_fail), true);
                        startTTS(getString(R.string.baidu_face_identify_fail));
                    } else {
                        if (BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION == resultType) {
                            triggerQuery(responseIdentify);
                        } else {
                            if (BaiduFaceOnlineAI.FACE_IDENTIFY_IMAGE_ACTION == resultType) {
                                showFinalImageResponse(responseIdentify, true);
                            } else {
                                showFinalTextResponse(responseIdentify, true);
                                startTTS(responseIdentify);
                            }
                        }
                    }
                    break;
                case BaiduFaceOnlineAI.FACE_DETECT_ACTION:
                case BaiduFaceOnlineAI.FACE_DETECT_IMAGE_ACTION:
                    final String responseDetect = (String)result;
                    if (BaiduFaceOnlineAI.FACE_DETECT_IMAGE_ACTION == resultType) {
                        showFinalImageResponse(responseDetect, true);
                    } else {
                        showFinalTextResponse(responseDetect, true);
                        startTTS(responseDetect);
                    }
                    break;
                case BaiduFaceOnlineAI.FACE_AUTHENTICATE_ACTION:
                case BaiduFaceOnlineAI.FACE_AUTHENTICATE_IMAGE_ACTION:
                    final String responseAuthenticate = (String)result;
                    if (BaiduFaceOnlineAI.FACE_AUTHENTICATE_IMAGE_ACTION == resultType) {
                        showFinalImageResponse(responseAuthenticate, true);
                    } else {
                        showFinalTextResponse(responseAuthenticate, true);
                        startTTS(responseAuthenticate);
                    }
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
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalTextResponse(getString(R.string.baidu_human_body_gesture_fail), true);
                        startTTS(getString(R.string.baidu_human_body_gesture_fail));
                    } else {
                        if (BaiduHumanBodyAI.GESTURE_ACTION == resultType) {
                            showFinalTextResponse((String)result, true);
                            startTTS((String)result);
                        } else {
                            showFinalImageResponse((String)result, true);
                        }
                    }
                    break;
                case BaiduHumanBodyAI.HEAD_COUNT_ACTION:
                case BaiduHumanBodyAI.HEAD_COUNT_IMAGE_ACTION:
                    if (BaiduHumanBodyAI.HEAD_COUNT_ACTION == resultType) {
                        showFinalTextResponse((String)result, true);
                        startTTS((String)result);
                    } else {
                        showFinalImageResponse((String)result, true);//显示图片
                    }
                    break;
                case BaiduHumanBodyAI.BODY_SEGMENT_IMAGE_ACTION:
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalTextResponse(getString(R.string.baidu_human_body_segment_fail), true);
                        startTTS(getString(R.string.baidu_human_body_segment_fail));
                    } else {
                        showFinalImageResponse((String)result, true);//显示图片
                    }
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
                    runOnUiThread(new Runnable() { //very important!!!
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

    private BaiduBaseAI.IBaiduBaseListener mImageProcessListener = new BaiduBaseAI.IBaiduBaseListener()  {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduImageProcessAI.SELFIE_ANIME_ACTION:
                case BaiduImageProcessAI.COLOURIZE_ACTION:
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalImageResponse(getString(R.string.baidu_image_process_fail), true);
                    } else {
                        showFinalImageResponse((String)result, true);
                    }
                    break;
            }
        }
    };

    private BaiduBaseAI.IBaiduBaseListener mNLPListener = new BaiduBaseAI.IBaiduBaseListener()  {
        @Override
        public void onError(final String msg) {
            showErrorMsg(msg);
        }

        @Override
        public void onFinalResult(final Object result, final int resultType) {
            switch (resultType) {
                case BaiduNLPAI.CORRECT_TEXT_ACTION:
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalImageResponse(getString(R.string.baidu_nlp_correct_text_fail), true);
                    } else {
                        showFinalTextResponse((String)result, true);
                    }
                    break;
                case BaiduNLPAI.NEWS_SUMMARY_ACTION:
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalImageResponse(getString(R.string.baidu_nlp_news_summary_fail), true);
                    } else {
                        showFinalTextResponse((String)result, true);
                    }
                    break;
                case BaiduNLPAI.DNN_SENTENCE_ACTION:
                    if (TextUtils.isEmpty((String)result)) {
                        showFinalImageResponse(getString(R.string.baidu_nlp_dnn_sentence_fail), true);
                    } else {
                        showFinalTextResponse((String)result, true);
                    }
                    break;
            }
        }
    };

    protected void triggerQuery(final String result) {
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

        mBaiduUnitAI.action(filterResult);
    }

    protected void triggerQuery(final Bitmap bitmap) {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_ocr));
        itemList.add(getString(R.string.option_image_classify));
        itemList.add(getString(R.string.option_face_identify));
        if (mSettingResult.mEnableExtraFun) {
            itemList.add(getString(R.string.option_face_detect));

            itemList.add(getString(R.string.option_human_body_gesture));
            itemList.add(getString(R.string.option_human_body_headcount));
            itemList.add(getString(R.string.option_human_body_segment));

            itemList.add(getString(R.string.option_image_process_selfie_anime));
            itemList.add(getString(R.string.option_image_process_colourize));
            itemList.add(getString(R.string.option_image_process_style_trans));

            itemList.add(getString(R.string.option_ocr_qrcode));
            itemList.add(getString(R.string.option_ocr_handwriting));
        }
        final String[] queryItems = itemList.toArray(new String[0]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(BaiduAIActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(queryItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String ITEM_IMAGE_FILE_PATH =
                        FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "item_main.jpg";
                if (FileUtils.isFileExist(ITEM_IMAGE_FILE_PATH))
                    FileUtils.deleteFile(ITEM_IMAGE_FILE_PATH);
                BitmapUtils.saveBitmapToJpeg(bitmap, ITEM_IMAGE_FILE_PATH);
                // which 下标从0开始
                switch (which) {
                    case 0: // OCR文字识别
                        final String[] ocrItems = getResources().getStringArray(R.array.ocr_language_item);
                        onShowOCRLanguageDialog(ocrItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // which 下标从0开始
                                if (mSettingResult.mRequestImage) {
                                    showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                                }
                                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                                mBaiduOcrAI.onOCRText(ITEM_IMAGE_FILE_PATH, false, ocrItems[which]);
                            }
                        });
                        break;
                    case 1: // 物体识别
                        onShowClassifyTypeDialog(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // which 下标从0开始, 需要转换为classify type
                                int classifyType = mSettingResult.mClassifyImageTypeList.get(which);
                                if (mSettingResult.mRequestImage) {
                                    showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                                }
                                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                                mBaiduClassifyImageAI.action(classifyType, ITEM_IMAGE_FILE_PATH, false);
                            }
                        });
                        break;
                }

                if (which >= 2) {
                    if (mSettingResult.mRequestImage) {
                        showFinalImageResponse(ITEM_IMAGE_FILE_PATH, false);
                    }
                    showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                    switch (which) {
                        case 2: // 人脸识别
                            if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == mSettingResult.mFaceIdentifyMode) {
                                mBaiduContentApproveAI.action(ITEM_IMAGE_FILE_PATH, false);
                            } else {
                                mBaiduFaceOnlineAI.onIdentifyThread(ITEM_IMAGE_FILE_PATH, false,
                                        mSettingResult.mFaceIdentifyMode == GlobalValue.FACE_IDENTIFY_ONLINE_MN ?
                                                FaceIdentify.FACE_IDENTIFY_M_FROM_N : FaceIdentify.FACE_IDENTIFY_1_FROM_N);
                            }
                            break;
                        case 3: // 人脸检测
                            mBaiduFaceOnlineAI.onDetectThread(ITEM_IMAGE_FILE_PATH);
                            break;

                        case 4: // 手势识别
                            mBaiduHumanBodyAI.action(BaiduHumanBodyAI.GESTURE_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                        case 5: // 清点人头
                            mBaiduHumanBodyAI.action(BaiduHumanBodyAI.HEADCOUNT_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                        case 6: // 清点人头
                            mBaiduHumanBodyAI.action(BaiduHumanBodyAI.BODY_SEGMENT_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;

                        case 7: //额外功能，人像动漫化
                            mBaiduImageProcessAI.action(BaiduImageProcessAI.SELFIE_ANIME_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                        case 8: //额外功能，黑白图像上色
                            mBaiduImageProcessAI.action(BaiduImageProcessAI.COLOURIZE_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                        case 9: //额外功能，图像风格转换
                            mBaiduImageProcessAI.action(BaiduImageProcessAI.STYLE_TRANS_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;

                        case 10: // 二维码识别
                            mBaiduOcrAI.action(BaiduOcrAI.QRCODE_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                        case 11: //手写文本识别
                            mBaiduOcrAI.action(BaiduOcrAI.HANDWRITING_TYPE, ITEM_IMAGE_FILE_PATH);
                            break;
                    }
                }
            }
        });
        listDialog.show();
    }

    protected void startTTS(String text) {
        if (!mSettingResult.mTTS || TextUtils.isEmpty(text)) return;

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
    protected void onSwitchVoiceAssistant(final boolean query) {
        if (mBaiduTTSAI.isTTSRunning()) {
            mBaiduTTSAI.stopTTSSpeak();
            mSpeechEnable = false;
            ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(R.drawable.baseline_mic_none_white_48dp);
            return;
        }

        if (mSpeechEnable) {
            mBaiduSpeechAI.cancelBaiduSpeechRecognize();
        } else {
            mActionHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBaiduSpeechAI.startBaiduSpeechRecognize(query);
                }
            });
        }
        mSpeechEnable = !mSpeechEnable;
        ((ImageView)findViewById(R.id.microphone_image_view)).setImageResource(
                mSpeechEnable? R.drawable.baseline_mic_black_48dp : R.drawable.baseline_mic_none_white_48dp);
    }

    protected void onSwitchTranslateLanguage(boolean chineseToForeign) {
        if (TRANSLATE_SUB_UI_LEVER != mCurUILever) return;

        if (chineseToForeign) {
            findViewById(R.id.left_arrow_image_view).setVisibility(View.INVISIBLE);
            findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
            mBaiduSpeechAI.setLanguageType(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);
            showFinalTextResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getOriginalLanguage()), false);
        } else {
            findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
            findViewById(R.id.right_arrow_image_view).setVisibility(View.INVISIBLE);
            mBaiduSpeechAI.setLanguageType(BaiduSpeechAI.BAIDU_SPEECH_ENGLISH);
            mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.REVERSE_TRANSLATE);
            showFinalTextResponse(String.format(getString(R.string.baidu_unit_fix_translate_input),
                    mBaiduUnitAI.getTranslateFixAction().getTargetLanguage()), true);
        }
    }

    protected void onSwitchTranslateLanguage(String language) {
        mBaiduSpeechAI.setLanguageType(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
        mBaiduUnitAI.getTranslateFixAction().setTranslateType(TranslateFixAction.TRANSLATE);

        if (TRANSLATE_SUB_UI_LEVER != mCurUILever) {
            mBaiduUnitAI.getTranslateFixAction().forceAction(true);
            mBaiduUnitAI.getTranslateFixAction().setTargetLanguage(language);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_start), language);
            showFinalTextResponse(msg,false);
            startTTS(msg);

            mCurUILever = TRANSLATE_SUB_UI_LEVER;
            findViewById(R.id.left_arrow_image_view).setVisibility(View.VISIBLE);
            findViewById(R.id.right_arrow_image_view).setVisibility(View.VISIBLE);
        } else {
            mBaiduUnitAI.getTranslateFixAction().forceAction(false);
            String msg = String.format(getString(R.string.baidu_unit_fix_translate_stop), language);
            showFinalTextResponse(msg, false);
            startTTS(msg);

            mCurUILever = MAIN_UI_LEVER;
            findViewById(R.id.left_arrow_image_view).setVisibility(View.GONE);
            findViewById(R.id.right_arrow_image_view).setVisibility(View.GONE);
        }
    }

    protected void onShowFaceMerge() {
        mBaiduFaceOnlineAI.onFaceMergeActivity();
    }

    protected void onShowFaceCompare() {
        mBaiduFaceOnlineAI.onFaceCompareActivity();
    }

    protected void onShowFaceManager() {
        mBaiduFaceOnlineAI.onFaceManagerActivity();
    }

    protected String getAndShowMatchedOCRLanguage(String language) {
        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
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
    abstract protected void showHint(String hint);
    abstract protected void showProgressDialog(String msg);
    abstract protected void hideProgressDialog();
    abstract protected void onShowClassifyTypeDialog(DialogInterface.OnClickListener listener);
    abstract protected void onShowOCRLanguageDialog(String[] items, DialogInterface.OnClickListener listener);
}
