package com.fih.featurephone.voiceassistant.baidu.ocr;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduUtil;
import com.fih.featurephone.voiceassistant.baidu.ocr.model.RecognizeFormula;
import com.fih.featurephone.voiceassistant.baidu.ocr.model.RecognizeQRCode;
import com.fih.featurephone.voiceassistant.baidu.ocr.model.RecognizeText;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//OCR可识别的语言
//"CHN_ENG";
//"CHN"; 汉语
//"ENG";英文
//"POR";葡萄牙语
//"FRE";法语
//"GER";德语
//"ITA";意大利语
//"SPA";西班牙语
//"RUS";俄语
//"JAP";日语
//"KOR";韩语

public class BaiduOcrAI extends BaiduBaseAI {
    public static final int OCR_TEXT_ACTION = 1;
    public static final int OCR_TEXT_QUESTION_ACTION = 2;
    public static final int OCR_FORMULA_ACTION = 3;
    public static final int OCR_QRCODE_ACTION = 5;

    public static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    public static final String OCR_DEFAULT_LANGUAGE = "中英文";
    private boolean mBaiduOCR_DetectDirection = true;

    private boolean mBaiduOCR_HasToken = false;
//    private boolean mBaiduOCR_DetectLanguage = false;
    private String mBaiduOCR_LanguageType = GeneralBasicParams.CHINESE_ENGLISH;
    private Context mContext;
    private BaiduBaseAI.IBaiduBaseListener mListener;

    private RecognizeText mRecognizeText;
    private RecognizeFormula mRecognizeFormula;
    private RecognizeQRCode mRecognizeQRCode;

    public BaiduOcrAI(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mContext = context;
        mListener = listener;
        initMapValues();

        mRecognizeText = new RecognizeText(context, listener);
        mRecognizeFormula = new RecognizeFormula(context, listener);
        mRecognizeQRCode = new RecognizeQRCode(context, listener);
    }

    public void init() {
//        用明文ak，sk初始化
        OCR.getInstance(mContext).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
//                String token = result.getAccessToken();
                mBaiduOCR_HasToken = true;
            }

            @Override
            public void onError(OCRError error) {
                mBaiduOCR_HasToken = false;
                error.printStackTrace();
                if (null != mListener) mListener.onError(error.getMessage());
            }
        }, mContext, BaiduUtil.OCRTTS_API_KEY, BaiduUtil.OCRTTS_SECRET_KEY);
    }

    public void release() {
        OCR.getInstance(mContext).release();
    }

    public void setLanguageType(String languageType) {
        if (TextUtils.isEmpty(languageType)) languageType = OCR_DEFAULT_LANGUAGE;

        String ocrLanguage = LANGUAGE_MAP.get(languageType);
        if (TextUtils.isEmpty(ocrLanguage)) {
            mBaiduOCR_LanguageType = languageType;
        } else {
            mBaiduOCR_LanguageType = ocrLanguage;
        }
    }

    public void setDetectDirection(boolean detectDirection) {
        mBaiduOCR_DetectDirection = detectDirection;
    }

    private void initMapValues() {
        //OCR可识别的语言
        LANGUAGE_MAP.put("汉英语", "CHN_ENG");  LANGUAGE_MAP.put("中英文", "CHN_ENG");
        LANGUAGE_MAP.put("汉语", "CHN");        LANGUAGE_MAP.put("中文", "CHN");
        LANGUAGE_MAP.put("英语", "ENG");        LANGUAGE_MAP.put("英文", "ENG");
        LANGUAGE_MAP.put("法语", "FRE");        LANGUAGE_MAP.put("法文", "FRE");
        LANGUAGE_MAP.put("德语", "GER");        LANGUAGE_MAP.put("德文", "GER");
        LANGUAGE_MAP.put("俄语", "RUS");        LANGUAGE_MAP.put("俄文", "RUS");
        LANGUAGE_MAP.put("日语", "JAP");        LANGUAGE_MAP.put("日文", "JAP");
        LANGUAGE_MAP.put("韩语", "KOR");        LANGUAGE_MAP.put("韩文", "KOR");
        LANGUAGE_MAP.put("意大利语", "ITA");
        LANGUAGE_MAP.put("西班牙语", "SPA");
        LANGUAGE_MAP.put("葡萄牙语", "POR");
    }

    private boolean checkTokenStatus() {
        if (!mBaiduOCR_HasToken) {
            if (null != mListener) {
                mListener.onError(mContext.getResources().getString(R.string.baidu_unit_ocr_token_error));
            }
        }
        return mBaiduOCR_HasToken;
    }

    public void onBaiduOCRText(String imagePath, final boolean question) {
        if (!checkTokenStatus() || TextUtils.isEmpty(imagePath)) return;

        GeneralBasicParams param = new GeneralBasicParams();
        if ("CHN".equals(mBaiduOCR_LanguageType) || "ENG".equals(mBaiduOCR_LanguageType)) {
            //保证中英文都识别出来，避免乱码
            param.setLanguageType(GeneralBasicParams.CHINESE_ENGLISH);
        } else {
            //其他语言
            param.setLanguageType(mBaiduOCR_LanguageType);
        }
        param.setDetectDirection(mBaiduOCR_DetectDirection);
        param.setDetectLanguage(false);
        param.setImageFile(new File(imagePath));
//            RecognizeService.recGeneralEnhanced(mContext, param, listener);
        RecognizeService.recGeneralBasic(mContext, param, new RecognizeService.ServiceListener() {
            @Override
            public void onResult(String result) {
                if (null != mListener) {
                    if ("CHN".equals(mBaiduOCR_LanguageType)) {
                        result = CommonUtil.filterEnglishCharacter(result);
                    } else if ("ENG".equals(mBaiduOCR_LanguageType)) {
                        result = CommonUtil.filterChineseCharacter(result);
                    }
                    mListener.onFinalResult(result, question? OCR_TEXT_QUESTION_ACTION : OCR_TEXT_ACTION);
                }
            }
        });
    }

/*    以下是不用百度的OCR SDK*/
    public void onOCRText(final String imagePath, final boolean question, final String languageType) {
        new Thread() {
            @Override
            public void run() {
                String ocrLanguage = LANGUAGE_MAP.get(languageType);
                if (TextUtils.isEmpty(ocrLanguage)) {
                    mBaiduOCR_LanguageType = "CHN_ENG";
                } else {
                    mBaiduOCR_LanguageType = ocrLanguage;
                }
                mRecognizeText.setLanguageType(ocrLanguage);
                mRecognizeText.request(imagePath, question);
            }
        }.start();
    }

    public void onOCRFormula(final String imagePath, final boolean question) {
        new Thread() {
            @Override
            public void run() {
                mRecognizeFormula.request(imagePath, question);
            }
        }.start();
    }

    public void onOCRQRCode(final String imagePath, final boolean question) {
        new Thread() {
            @Override
            public void run() {
                mRecognizeQRCode.request(imagePath, question);
            }
        }.start();
    }
}
