package com.min.aiassistant.baidu.ocr.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.ocr.parsejson.ParseHandwritingJson;

public class RecognizeHandwriting extends BaiduImageBaseModel<ParseHandwritingJson.Handwriting> {

    public RecognizeHandwriting(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/handwriting";
        //recognize_granularity: 是否定位单字符位置，big：不定位单字符位置，默认值；small：定位单字符位置
        //words_type: words_type=number:手写数字识别；无此参数或传其它值 默认手写通用识别（目前支持汉字和英文）
        mURLRequestParamString = "&recognize_granularity=big"; //+ "&words_type=number";
    }

    protected ParseHandwritingJson.Handwriting parseJson(String json) {
        return ParseHandwritingJson.getInstance().parse(json);
    }

    protected void handleResult(ParseHandwritingJson.Handwriting handwriting) {
        if (null == handwriting.mWordsResultList) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_ocr_text_fail));
            return;
        }

        StringBuilder result = new StringBuilder();
        for (ParseHandwritingJson.WordsResult wordsResult : handwriting.mWordsResultList) {
            String words = wordsResult.mWords;
            if (result.length() != 0) result.append("\n");
            result.append(words);
        }

        mBaiduBaseListener.onFinalResult(result.toString(), mQuestion?
                BaiduOcrAI.OCR_TEXT_QUESTION_ACTION : BaiduOcrAI.OCR_TEXT_ACTION);
    }
}
