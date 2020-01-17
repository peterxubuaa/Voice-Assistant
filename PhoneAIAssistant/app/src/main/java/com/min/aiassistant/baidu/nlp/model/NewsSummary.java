package com.min.aiassistant.baidu.nlp.model;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduTextBaseModel;
import com.min.aiassistant.baidu.nlp.BaiduNLPAI;
import com.min.aiassistant.baidu.nlp.parsejson.ParseNewsSummaryJson;

import java.util.HashMap;
import java.util.Map;

public class NewsSummary extends BaiduTextBaseModel<ParseNewsSummaryJson.NewsSummary> {

    public NewsSummary(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mHostURL = "https://aip.baidubce.com/rpc/2.0/nlp/v1/news_summary";
    }

    protected ParseNewsSummaryJson.NewsSummary parseJson(String json) {
        return ParseNewsSummaryJson.getInstance().parse(json);
    }

    protected void handleResult(ParseNewsSummaryJson.NewsSummary newsSummary) {
        if (TextUtils.isEmpty(newsSummary.mSummary)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_nlp_news_summary_fail));
        } else {
            mBaiduBaseListener.onFinalResult(newsSummary.mSummary, BaiduNLPAI.NEWS_SUMMARY_ACTION);
        }
    }

    public void request(String text) {
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("content", text); //字符串（限3000字符数以内）
        requestParamMap.put("max_summary_len", 200); //此数值将作为摘要结果的最大长度, 推荐最优区间：200-500字

        request(requestParamMap);
    }
}
