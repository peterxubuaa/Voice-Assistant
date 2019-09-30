package com.min.ai.voiceassistant.speech.listener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.speech.asr.SpeechConstant;
import com.min.ai.voiceassistant.MainActivity;
import com.min.ai.voiceassistant.speech.RecogResult;

public class MessageStatusRecogListener extends StatusRecogListener {
    private final String TAG = MessageStatusRecogListener.class.getSimpleName();
    private final boolean DEBUG = MainActivity.DEBUG;

    private Handler mHandler;
    private long mSpeechEndTime = 0;

    public MessageStatusRecogListener(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onAsrReady() {
        super.onAsrReady();
        mSpeechEndTime = 0;
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_WAKEUP_READY, "引擎就绪，可以开始说话\n");
    }

    @Override
    public void onAsrBegin() {
        super.onAsrBegin();
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_BEGIN, "检测到用户说话\n");
    }

    @Override
    public void onAsrEnd() {
        super.onAsrEnd();
        mSpeechEndTime = System.currentTimeMillis();
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_END, "检测到用户说话结束\n");
    }

    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                    "临时识别结果，结果是“" + results[0] + "”；原始json：" + recogResult.getOrigalJson());
        super.onAsrPartialResult(results, recogResult);
    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        String message;
        message = "识别用户说话结束，结果是”" + results[0];
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL,
                message + "；原始json：" + recogResult.getOrigalJson());
        if (mSpeechEndTime > 0) {
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - mSpeechEndTime;
            message += "；说话结束到识别结束耗时【" + diffTime + "ms】" + currentTime;
        }
        mSpeechEndTime = 0;
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_FINISH, message + "\n");

        sendFinalResult(results[0]);
    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode, String descMessage,
                                 RecogResult recogResult) {
        super.onAsrFinishError(errorCode, subErrorCode, descMessage, recogResult);
        String message = "识别错误, 错误码：" + errorCode + " ," + subErrorCode + " ; " + descMessage + "\n";
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL, message);
        if (mSpeechEndTime > 0) {
            long diffTime = System.currentTimeMillis() - mSpeechEndTime;
            message += "。说话结束到识别结束耗时【" + diffTime + "ms】\n";
        }
        mSpeechEndTime = 0;
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_ERROR, message);
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {
        super.onAsrOnlineNluResult(nluResult);
        if (!nluResult.isEmpty()) {
            sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL, "原始语义识别结果json：" + nluResult + "\n");
        }
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {
        super.onAsrFinish(recogResult);
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_FINISH, "识别一段话结束, 如果是长语音的情况会继续识别下段话\n");
    }

    /**
     * 长语音识别结束
     */
    @Override
    public void onAsrLongFinish() {
        super.onAsrLongFinish();
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_LONG_SPEECH, "长语音识别结束\n");
    }

    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineLoaded() {
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_LOADED, "离线资源加载成功, 没有此回调可能离线语法功能不能使用\n");
    }

    /**
     * 使用离线命令词时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineUnLoaded() {
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_UNLOADED, "离线资源卸载成功\n");
    }

    @Override
    public void onAsrExit() {
        super.onAsrExit();
        sendStatusMessage(SpeechConstant.CALLBACK_EVENT_ASR_EXIT, "识别引擎结束并空闲中\n");
    }

    private void sendStatusMessage(String eventName, String message) {
        if (null == mHandler) {
            Log.i(TAG, message);
            return;
        }

        Message msg = Message.obtain();
        msg.what = mStatus;
        if (DEBUG) {
            message = eventName + "# " + message;
            msg.obj = message;
        }
        mHandler.sendMessage(msg);
    }

    private void sendFinalResult(String result) {
        if (null == mHandler) {
            Log.i(TAG, result);
            return;
        }

        Message msg = Message.obtain();
        msg.what = STATUS_FINAL_RESULT;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }
}
