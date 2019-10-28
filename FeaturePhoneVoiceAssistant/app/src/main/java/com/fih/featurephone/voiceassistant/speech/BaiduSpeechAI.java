package com.fih.featurephone.voiceassistant.speech;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fih.featurephone.voiceassistant.speech.listener.IRecogListener;
import com.fih.featurephone.voiceassistant.speech.listener.MessageStatusRecogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.fih.featurephone.voiceassistant.speech.IStatus.*;

public class BaiduSpeechAI {
    private static final String TAG = BaiduSpeechAI.class.getSimpleName();
    private static final int SPEECH_START_STOP_MASK = 0x1;
    private static final int SPEECH_INIT_EXIT_MASK = 0x2;

    private Context mContext;
    private int mWorkState = 0;
    private Recognizer mBaiduSpeech_Recognizer = null;
    private int mVADTimeout = 800; //0: 表示开启长语音； >0: 连续xxxms静音，断句间隔时间，表示一句话结束，比如800ms适合短句输入，2000ms适合长句输入;
    private int mLanguageID = 15362;//1536: 默认普通话；15362：普通话模型，加强标点；1936：普通话远场模型； 1737：英语； 1837： 四川话
    private onSpeechListener mListener;

    public interface onSpeechListener {
        void onShowDebugInfo(String info);
        void onExit();
        void onFinalResult(String result);
    }

    public BaiduSpeechAI(Context context, onSpeechListener listener) {
        mContext = context;
        mListener = listener;
    }

    static class BaiduSpeechHandler extends Handler {
        WeakReference<BaiduSpeechAI> mTheBaiduSpeechAI;

        BaiduSpeechHandler(BaiduSpeechAI speechAI) {
            mTheBaiduSpeechAI = new WeakReference<BaiduSpeechAI>(speechAI);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, msg.what + " = " + msg.obj);

            if (null != mTheBaiduSpeechAI.get().mListener) {
                mTheBaiduSpeechAI.get().mListener.onShowDebugInfo(String.valueOf(msg.obj));
            }

            switch (msg.what) {
//                处理MessageStatusRecogListener中的状态回调
//                STATUS_NONE 初始状态
//                STATUS_READY 引擎准备完毕
//                STATUS_SPEAKING 用户开始说话到用户说话完毕前
//                STATUS_RECOGNITION 用户说话完毕后，识别结束前
//                STATUS_FINISHED 获得最终识别结果
                case STATUS_NONE:
                    Log.d(TAG, "asr exit status");
                    mTheBaiduSpeechAI.get().mWorkState &= ~SPEECH_START_STOP_MASK;
                    if (null != mTheBaiduSpeechAI.get().mListener) mTheBaiduSpeechAI.get().mListener.onExit();
                    break;
                case STATUS_READY:
                    Log.d(TAG, "asr engine ready");
                    break;
                case STATUS_SPEAKING:
                    Log.d(TAG, "asr speaking");
                    break;
                case STATUS_RECOGNITION:
                    Log.d(TAG, "asr recognizing");
                    break;
                case STATUS_PARTIAL_FINISHED:
                    Log.d(TAG, "asr partial finish");
                    break;
                case STATUS_FINISHED:
                    Log.d(TAG, "asr finish");
                    break;
                case STATUS_LONG_SPEECH_FINISHED:
                    Log.d(TAG, "asr long speech finish");
                    break;
                case STATUS_STOPPED:
                    Log.d(TAG, "asr stop");
                    break;
                case STATUS_OFFLINE_LOAD:
                    Log.d(TAG, "asr offline load");
                    break;
                case STATUS_OFFLINE_UNLOAD:
                    Log.d(TAG, "asr offline unload");
                    break;
                case STATUS_FINAL_RESULT:
                    Log.d(TAG, "asr final result");
                    if (null != mTheBaiduSpeechAI.get().mListener) {
                        mTheBaiduSpeechAI.get().mListener.onFinalResult(String.valueOf(msg.obj));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void initBaiduSpeech() {
        //baidu speech
        IRecogListener listener = new MessageStatusRecogListener(new BaiduSpeechHandler(this));
        mBaiduSpeech_Recognizer = new Recognizer(mContext, listener);
        mWorkState |= SPEECH_INIT_EXIT_MASK;
    }

    private void initBaiduSpeechSettings(int vadTimeout, int languageID) {
        mVADTimeout = vadTimeout;
        mLanguageID = languageID;
    }

    public void releaseBaiduSpeech() {
        if (null != mBaiduSpeech_Recognizer) {
            mBaiduSpeech_Recognizer.release();
        }
        mWorkState &= ~SPEECH_INIT_EXIT_MASK;
    }

    //开始录音，点击“开始”按钮后调用。
    public void startBaiduSpeechRecognize() {
        if (null == mBaiduSpeech_Recognizer) return;

        if (!isBaiduSpeechRecognizeRunning()) {
            JSONObject json = new JSONObject();
            try {
                json.put("accept-audio-data", false);
                json.put("disable-punctuation", false);
                json.put("accept-audio-volume", false);
                json.put("vad.endpoint-timeout", mVADTimeout);
                json.put("pid", mLanguageID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
//            String json = "{\"accept-audio-data\":false,\"disable-punctuation\":false,\"accept-audio-volume\":false,\"vad.endpoint-timeout\":0}";
            mWorkState |= SPEECH_START_STOP_MASK;
            String param = json.toString();
            mBaiduSpeech_Recognizer.start(param);
        }
    }

    public void stopBaiduSpeechRecognize() {
        mWorkState &= ~SPEECH_START_STOP_MASK;
        if (null == mBaiduSpeech_Recognizer) return;

        mBaiduSpeech_Recognizer.stop();
    }

    private boolean isBaiduSpeechRecognizeRunning() {
        if (null == mBaiduSpeech_Recognizer) return false;

        return (mWorkState & SPEECH_START_STOP_MASK) > 0;
    }
}
