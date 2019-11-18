package com.fih.featurephone.voiceassistant.baidu.tts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.tts.control.InitConfig;
import com.fih.featurephone.voiceassistant.baidu.tts.control.MySyntherizer;
import com.fih.featurephone.voiceassistant.baidu.tts.control.NonBlockSyntherizer;
import com.fih.featurephone.voiceassistant.baidu.tts.listener.UiMessageListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


public class BaiduTTSAI {
    private static final String TAG = BaiduTTSAI.class.getSimpleName();
    private static final int TTS_INIT_EXIT_MASK = 0x100;
    private static final int TTS_START_STOP_MASK = 0x200;

    // 主控制类，所有合成控制方法从这个类开始
    private MySyntherizer mBaiduTTS_Synthesizer = null;
    private Context mContext;
    private int mWorkState = 0;
    private onTTSListener mListener;

    public interface onTTSListener {
        void onStart();
        void onEnd();
        void onError(String msg);
    }

    static class BaiduTTSHandler extends Handler {
        WeakReference<BaiduTTSAI> mTheBaiduTTSAI;

        BaiduTTSHandler(BaiduTTSAI baiduTTSAI) {
            mTheBaiduTTSAI = new WeakReference<BaiduTTSAI>(baiduTTSAI);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what) {
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.PRINT:
                    if (msg.arg1 != 0) {//error message
                        mTheBaiduTTSAI.get().stopTTSSpeak();
                        mTheBaiduTTSAI.get().mListener.onError((String)msg.obj);
                    }
                    Log.d(TAG, "PRINT " + msg.obj);
                    break;
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.UI_CHANGE_INPUT_TEXT_SELECTION:
                    Log.d(TAG, "tts playing progress = " + msg.arg1);
                    break;
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.UI_CHANGE_SYNTHES_TEXT_SELECTION:
                    Log.d(TAG, "tts synthesizing progress = " + msg.arg1);
                    break;
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.UI_CHANGE_TTS_START:
                    mTheBaiduTTSAI.get().mWorkState |= TTS_START_STOP_MASK;
                    mTheBaiduTTSAI.get().mListener.onStart();
                    break;
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.UI_CHANGE_TTS_END:
                    mTheBaiduTTSAI.get().mWorkState &= ~TTS_START_STOP_MASK;
                    mTheBaiduTTSAI.get().mListener.onEnd();
                    break;
                case com.fih.featurephone.voiceassistant.baidu.tts.MainHandlerConstant.INIT_SUCCESS:
                    mTheBaiduTTSAI.get().mWorkState |= TTS_INIT_EXIT_MASK;
                    break;
            }
        }
    }

    public BaiduTTSAI(Context context, onTTSListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void initBaiduTTS() {
        Handler handler = new BaiduTTSHandler(this);
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(handler);

        Map<String, String> params = getParams();

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(
                        mContext.getString(R.string.APP_ID),
                        mContext.getString(R.string.API_KEY),
                        mContext.getString(R.string.SECRET_KEY),
                        TtsMode.ONLINE , params, listener);//TtsMode.ONLINE 纯在线。断网即不可使用, TtsMode.MIX 离在线混合。 断网或者网络请求超时使用离线

        mBaiduTTS_Synthesizer = new NonBlockSyntherizer(mContext, initConfig, handler); // 此处可以改为MySyntherizer 了解调用过程
    }

    public void releaseBaiduTTS() {
        if (null != mBaiduTTS_Synthesizer) mBaiduTTS_Synthesizer.release();
        mWorkState &= ~TTS_INIT_EXIT_MASK;
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     */
    private Map<String, String> getParams() {
        final int BAIDU_TTS_SPEAKER = 0; //0: 标准女声; 1:标准男声; 3: 情感男声; 4:情感女童声; 5:情感女声; 103；106；110: 情感男童声；111
        final int BAIDU_TTS_VOLUME = 5; //在线及离线合成的音量 。范围["0" - "15"], 不支持小数。 "0" 最轻，"15" 最响。
        final int BAIDU_TTS_SPEED = 5; //在线及离线合成的语速 。范围["0" - "15"], 不支持小数。 "0" 最慢，"15" 最快
        final int BAIDU_TTS_PITCH = 5; //在线及离线合成的语调 。范围["0" - "15"], 不支持小数。 "0" 最低沉， "15" 最尖

        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, String.valueOf(BAIDU_TTS_SPEAKER));
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(BAIDU_TTS_VOLUME));
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(BAIDU_TTS_SPEED));
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(BAIDU_TTS_PITCH));

        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE);

/*
        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource();
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, offlineResource.getModelFilename());
*/
        return params;
    }

/*    private OfflineResource createOfflineResource() {
        OfflineResource offlineResource = null;
        try {
            String voiceType = OfflineResource.VOICE_FEMALE;
            offlineResource = new OfflineResource(mContext, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
        }
        return offlineResource;
    }*/

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void startTTSSpeak(String ttsText) {
        if ( (mWorkState & TTS_INIT_EXIT_MASK) == 0) return;
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        if (!TextUtils.isEmpty(ttsText)) {
            // 合成前可以修改参数：
            Map<String, String> params = getParams();
            mBaiduTTS_Synthesizer.setParams(params);
            mBaiduTTS_Synthesizer.speak(ttsText);
        }
    }

    public void stopTTSSpeak() {
        if (isTTSRunning()) {
            mBaiduTTS_Synthesizer.stop();
            mWorkState &= ~TTS_START_STOP_MASK;
        }
    }

    public boolean isTTSRunning() {
        return ((mWorkState & TTS_START_STOP_MASK) > 0);
    }
}
