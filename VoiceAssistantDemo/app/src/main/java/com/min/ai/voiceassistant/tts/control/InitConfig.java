package com.min.ai.voiceassistant.tts.control;

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
    private SpeechSynthesizerListener listener;

    public InitConfig(String mAppId, String mAppKey, String mSecretKey, TtsMode mTTSMode,
                      Map<String, String> mParams, SpeechSynthesizerListener listener) {
        this.mAppId = mAppId;
        this.mAppKey = mAppKey;
        this.mSecretKey = mSecretKey;
        this.mTTSMode = mTTSMode;
        this.mParams = mParams;
        this.listener = listener;
    }

    public SpeechSynthesizerListener getListener() {
        return listener;
    }

    public Map<String, String> getParams() {
        return mParams;
    }

    public String getAppId() {
        return mAppId;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public String getSecretKey() {
        return mSecretKey;
    }

    public TtsMode getTtsMode() {
        return mTTSMode;
    }
}
