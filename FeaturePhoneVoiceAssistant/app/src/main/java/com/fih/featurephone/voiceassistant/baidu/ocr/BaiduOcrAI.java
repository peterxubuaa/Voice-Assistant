package com.fih.featurephone.voiceassistant.baidu.ocr;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.fih.featurephone.voiceassistant.R;
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

public class BaiduOcrAI {
    public static final Map<String, String> LANGUAGE_MAP = new HashMap<String, String>();
    public static final String OCR_DEFAULT_LANGUAGE = "中英文";
    private boolean mBaiduOCR_HasToken = false;
    private boolean mBaiduOCR_DetectDirection = true;
    private boolean mBaiduOCR_DetectLanguage = false;
    private String mBaiduOCR_LanguageType = GeneralBasicParams.CHINESE_ENGLISH;
    private Context mContext;
    private onOCRListener mListener;

    public interface onOCRListener {
        void onError(String msg);
        void onFinalResult(String result, boolean question);
    }

    public BaiduOcrAI(Context context, onOCRListener listener) {
        mContext = context;
        mListener = listener;
        initMapValues();
    }

    public void initBaiduOCR() {
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
        }, mContext, mContext.getString(R.string.API_KEY), mContext.getString(R.string.SECRET_KEY));
    }

    public void releaseBaiduOCR() {
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
//            Toast.makeText(mContext, mContext.getResources().getString(R.string.baidu_unit_ocr_token_error), Toast.LENGTH_LONG).show();
            if (null != mListener) {
                mListener.onError(mContext.getResources().getString(R.string.baidu_unit_ocr_token_error));
            }
        }
        return mBaiduOCR_HasToken;
    }

    public void baiduOCRText(String imagePath, final boolean question) {
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
        param.setDetectLanguage(mBaiduOCR_DetectLanguage);
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
                    mListener.onFinalResult(result, question);
                }
            }
        });
    }
}
