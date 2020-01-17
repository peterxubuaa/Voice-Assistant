package com.min.aiassistant.baidu.ocr.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.ocr.parsejson.ParseBasicTextJson;
import com.min.aiassistant.utils.CommonUtil;

public class RecognizeText extends BaiduImageBaseModel<ParseBasicTextJson.OCRText> {
    private final String BASE_REQUEST_PARAM = "&detect_direction=true" + "&paragraph=true" + "&probability=true";
    private String mLanguageType = "CHN_ENG";

    public RecognizeText(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic";
        mURLRequestParamString = BASE_REQUEST_PARAM + "&language_type=" + mLanguageType;
    }

    public void setLanguageType(String languageType) {
        mLanguageType = languageType;//保存原始语言翻译设置
        if ("CHN".equals(languageType) || "ENG".equals(languageType)) {
            //保证中英文都识别出来，避免乱码
            languageType = "CHN_ENG";
        }

        mURLRequestParamString = BASE_REQUEST_PARAM + "&language_type=" + languageType;
    }

    protected ParseBasicTextJson.OCRText parseJson(String json) {
        return ParseBasicTextJson.getInstance().parse(json);
    }

    protected void handleResult(ParseBasicTextJson.OCRText ocrText) {
        if (null == ocrText.mWordsResultList) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_ocr_text_fail));
            return;
        }

        StringBuilder result = new StringBuilder();
        for (ParseBasicTextJson.WordsResult wordsResult : ocrText.mWordsResultList) {
            String words = wordsResult.mWords;
            if ("CHN".equals(mLanguageType)) {
                words = CommonUtil.filterEnglishCharacter(wordsResult.mWords);
            } else if ("ENG".equals(mLanguageType)) {
                words = CommonUtil.filterChineseCharacter(wordsResult.mWords);
            }
            if (result.length() != 0) result.append("\n");
            result.append(words);
        }

        mBaiduBaseListener.onFinalResult(result.toString(), mQuestion?
                BaiduOcrAI.OCR_TEXT_QUESTION_ACTION : BaiduOcrAI.OCR_TEXT_ACTION);
    }
}
