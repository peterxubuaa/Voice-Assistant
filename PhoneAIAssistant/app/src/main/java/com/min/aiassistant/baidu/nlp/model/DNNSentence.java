package com.min.aiassistant.baidu.nlp.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduTextBaseModel;
import com.min.aiassistant.baidu.nlp.BaiduNLPAI;
import com.min.aiassistant.baidu.nlp.parsejson.ParseCorrectTextJson;
import com.min.aiassistant.baidu.nlp.parsejson.ParseDNNSentenceJson;

import java.util.HashMap;
import java.util.Map;

public class DNNSentence extends BaiduTextBaseModel<ParseDNNSentenceJson.DNNSentence> {

    public DNNSentence(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mHostURL = "https://aip.baidubce.com/rpc/2.0/nlp/v2/dnnlm_cn";
    }

    protected ParseDNNSentenceJson.DNNSentence parseJson(String json) {
        return ParseDNNSentenceJson.getInstance().parse(json);
    }

    protected void handleResult(ParseDNNSentenceJson.DNNSentence dnnSentence) {
        mBaiduBaseListener.onFinalResult(String.valueOf(dnnSentence.mPPL), BaiduNLPAI.DNN_SENTENCE_ACTION);
    }

    public void request(String text) {
        Map<String, Object> requestParamMap = new HashMap<>();
        requestParamMap.put("text", text); //文本内容，最大256字节，不需要切词
        request(requestParamMap);
    }
}
