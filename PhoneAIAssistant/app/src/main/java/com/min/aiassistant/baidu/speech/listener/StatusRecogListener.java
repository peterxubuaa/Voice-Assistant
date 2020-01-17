package com.min.aiassistant.baidu.speech.listener;

import android.util.Log;

import com.min.aiassistant.baidu.speech.IStatus;
import com.min.aiassistant.baidu.speech.RecogResult;

/**
 * 根据回调，判断asr引擎的状态
 *
 * 通常状态变化如下：
 *
 * STATUS_NONE 初始状态
 * STATUS_READY 引擎准备完毕
 * STATUS_SPEAKING 用户开始说话到用户说话完毕前
 * STATUS_RECOGNITION 用户说话完毕后，识别结束前
 * STATUS_FINISHED 获得最终识别结果
 */
public class StatusRecogListener implements IRecogListener, IStatus {

    private static final String TAG = "StatusRecogListener";

    /**
     * 识别的引擎当前的状态
     */
    int mStatus = STATUS_NONE;

    @Override
    public void onAsrReady() {
        mStatus = STATUS_READY;
    }

    @Override
    public void onAsrBegin() {
        mStatus = STATUS_SPEAKING;
    }

    @Override
    public void onAsrEnd() {
        mStatus = STATUS_RECOGNITION;
    }

    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {
        mStatus = STATUS_PARTIAL_FINISHED;
    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        mStatus = STATUS_FINISHED;
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {
        mStatus = STATUS_FINISHED;
    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode,  String descMessage,
                                 RecogResult recogResult) {
        mStatus = STATUS_FINISHED_ERROR;
    }

    @Override
    public void onAsrLongFinish() {
        mStatus = STATUS_LONG_SPEECH_FINISHED;
    }

    @Override
    public void onAsrVolume(int volumePercent, int volume) {
        Log.i(TAG, "音量百分比" + volumePercent + " ; 音量" + volume);
    }

    @Override
    public void onAsrAudio(byte[] data, int offset, int length) {
        if (offset != 0 || data.length != length) {
            byte[] actualData = new byte[length];
            System.arraycopy(data, 0, actualData, 0, length);
            data = actualData;
        }

        Log.i(TAG, "音频数据回调, length:" + data.length);
    }

    @Override
    public void onAsrExit() {
        mStatus = STATUS_NONE;
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {
        mStatus = STATUS_FINISHED;
    }

    @Override
    public void onOfflineLoaded() {
        mStatus = STATUS_OFFLINE_LOAD;
    }

    @Override
    public void onOfflineUnLoaded() {
        mStatus = STATUS_OFFLINE_UNLOAD;
    }
}
