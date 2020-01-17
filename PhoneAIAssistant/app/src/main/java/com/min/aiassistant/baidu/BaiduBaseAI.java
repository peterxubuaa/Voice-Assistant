package com.min.aiassistant.baidu;

public class BaiduBaseAI {

    public interface IBaiduBaseListener {
        void onError(String msg);
        void onFinalResult(Object result, int resultType);
    }

    public void init() {}

    public void release() {}
}
