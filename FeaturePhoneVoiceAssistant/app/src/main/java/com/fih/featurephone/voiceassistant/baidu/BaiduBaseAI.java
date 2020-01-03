package com.fih.featurephone.voiceassistant.baidu;

import android.content.Context;

public class BaiduBaseAI {

    public interface IBaiduBaseListener {
        void onError(String msg);
        void onFinalResult(Object result, int resultType);
    }

    public BaiduBaseAI(Context context, IBaiduBaseListener listener) {
    }

    public void init() {}

    public void release() {}
}
