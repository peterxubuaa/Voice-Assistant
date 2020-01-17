package com.min.aiassistant.baidu.nlp.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduTextBaseModel;
import com.min.aiassistant.baidu.nlp.BaiduNLPAI;
import com.min.aiassistant.baidu.nlp.parsejson.ParseCorrectTextJson;

import java.util.HashMap;
import java.util.Map;

public class CorrectText extends BaiduTextBaseModel<ParseCorrectTextJson.CorrectText> {

    public CorrectText(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mHostURL = "https://aip.baidubce.com/rpc/2.0/nlp/v1/ecnet";
    }

    protected ParseCorrectTextJson.CorrectText parseJson(String json) {
        return ParseCorrectTextJson.getInstance().parse(json);
    }

    protected void handleResult(ParseCorrectTextJson.CorrectText correctText) {
        if (null == correctText.mItem) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_nlp_correct_text_fail));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (ParseCorrectTextJson.VecFragment vecFragment : correctText.mItem.mVecFragmentList) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(vecFragment.mOriFrag).append(" -> ").append(vecFragment.mCorrectFrag);
        }
        if (sb.length() > 0) {
            mBaiduBaseListener.onFinalResult(sb.toString(), BaiduNLPAI.CORRECT_TEXT_ACTION);
        }
        mBaiduBaseListener.onFinalResult(correctText.mItem.mCorrectQuery, BaiduNLPAI.CORRECT_TEXT_ACTION);
    }

    public void request(String text) {
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("text", text);
        request(requestParamMap);
    }
}
