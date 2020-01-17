package com.min.aiassistant.baidu.tts.control;

import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.util.Map;

/**
 * 合成引擎的初始化参数
 * <p>
 * Created by fujiayi on 2017/9/13.
 */

public class InitConfig {
    /**
     * mAppId mAppKey 和 mSecretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    private String mAppId;

    private String mAppKey;

    private String mSecretKey;

    /**
     * 纯在线或者离在线融合
     */
    private TtsMode mTTSMode;


    /**
     * 初始化的其它参数，用于setParam
     */
    private Map<String, String> mParams;

    /**
     * 合成引擎的回调
     */
    private SpeechSynthesizerListener mListener;

    public InitConfig(String appId, String appKey, String secretKey, TtsMode ttsMode,
                      Map<String, String> params, SpeechSynthesizerListener listener) {
        mAppId = appId;
        mAppKey = appKey;
        mSecretKey = secretKey;
        mTTSMode = ttsMode;
        mParams = params;
        mListener = listener;
    }

    public SpeechSynthesizerListener getListener() {
        return mListener;
    }

    Map<String, String> getParams() {
        return mParams;
    }

    String getAppId() {
        return mAppId;
    }

    String getAppKey() {
        return mAppKey;
    }

    String getSecretKey() {
        return mSecretKey;
    }

    TtsMode getTtsMode() {
        return mTTSMode;
    }
}
