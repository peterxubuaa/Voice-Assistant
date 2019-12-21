package com.fih.featurephone.voiceassistant.baidu.speech;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.baidu.speech.listener.IRecogListener;
import com.fih.featurephone.voiceassistant.baidu.speech.listener.MessageStatusRecogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import static com.fih.featurephone.voiceassistant.baidu.speech.IStatus.*;

public class BaiduSpeechAI {
    private static final String TAG = BaiduSpeechAI.class.getSimpleName();
    private static final int SPEECH_START_STOP_MASK = 0x1;
    private static final int SPEECH_INIT_EXIT_MASK = 0x2;

    private Context mContext;
    private int mWorkState = 0;
    private Recognizer mBaiduSpeech_Recognizer = null;
/*
    https://ai.baidu.com/docs#/ASR-Android-SDK/top
    语言：目前支持中文普通话，四川话，粤语，和英语四个
    输入法模型：适用于较长的句子输入。默认有标点，不支持在线语义; 开启标点后，不支持本地语义。
    搜索模型：适用于较短的句子输入。无标点，支持在线语义和本地语义。
    自训练平台模型： 在输入法模型和搜索模型的基础上，可以自行上传词库和句库，生成您自己的训练模型。
    在线语义：在线语义只支持普通话（本地语义也是只支持普通话）。在线语义对识别结果的文字，再做结构化解析，找到语句的“关键词”。在线语义详细说明请查看“语义理解协议”文档。
*/
    //0: 表示开启长语音； >0: 连续xxxms静音，断句间隔时间，表示一句话结束，比如800ms适合短句输入，2000ms适合长句输入;
    final private int VAD_TIMEOUT = 800;
/*  PID	语言	模型	是否有标点	在线语义	备注
    1536	普通话	搜索模型	无标点	不支持	默认PID
    15362	普通话	搜索模型	加强标点（逗号、句号、问号、感叹号）	不支持
    15363	普通话	搜索模型	加强标点（逗号、句号、问号、感叹号）	支持通用场景语义解析
    1537	普通话	输入法模型	有标点（逗号）	不支持
    15372	普通话	输入法模型	加强标点（逗号、句号、问号、感叹号）	不支持
    15373	普通话	输入法模型	加强标点（逗号、句号、问号、感叹号）	支持通用场景语义解析
    1737	英语		无标点	不支持
    17372	英语		加强标点（逗号、句号、问号）	不支持
    1637	粤语		有标点（逗号）	不支持
    16372	粤语		加强标点（逗号、句号、问号、感叹号）	不支持
    1837	四川话		有标点（逗号）	不支持
    1936	普通话	远场模型	有标点（逗号）	不支持
    19362	普通话	远场模型	加强标点（逗号、句号、问号、感叹号）	不支持
    19363	普通话	远场模型	加强标点（逗号、句号、问号、感叹号）	支持通用场景语义解析*/
    private int mLanguageID = 15363;
    private OnSpeechListener mListener;

    public interface OnSpeechListener {
        void onShowDebugInfo(String info);
        void onExit();
        void onFinalResult(String result);
        void onError(String msg);
    }

    public BaiduSpeechAI(Context context, OnSpeechListener listener) {
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
                case STATUS_FINISHED_ERROR:
                    if (msg.arg1 != 0){
                        mTheBaiduSpeechAI.get().mListener.onError(String.valueOf(msg.obj));
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

    public void initBaiduSpeechSettings(int languageType) {
        if (BaiduUnitAI.BAIDU_UNIT_SPEECH_CHINESE == languageType) {
            mLanguageID = 15363;
        } else if (BaiduUnitAI.BAIDU_UNIT_SPEECH_ENGLISH == languageType) {
            mLanguageID = 17372;
        } else if (BaiduUnitAI.BAIDU_UNIT_SPEECH_SICHUANESE == languageType) {
            mLanguageID = 1837; //18372?
        } else if (BaiduUnitAI.BAIDU_UNIT_SPEECH_CANTONESE == languageType) {
            mLanguageID = 16372;
        }
    }

    public void releaseBaiduSpeech() {
        if (null != mBaiduSpeech_Recognizer) {
            mBaiduSpeech_Recognizer.release();
            mBaiduSpeech_Recognizer = null;
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
                json.put("vad.endpoint-timeout", VAD_TIMEOUT);
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

    public void cancelBaiduSpeechRecognize() {
        mWorkState &= ~SPEECH_START_STOP_MASK;
        if (null == mBaiduSpeech_Recognizer) return;

        mBaiduSpeech_Recognizer.cancel();
    }

    private boolean isBaiduSpeechRecognizeRunning() {
        if (null == mBaiduSpeech_Recognizer) return false;

        return (mWorkState & SPEECH_START_STOP_MASK) > 0;
    }
}
