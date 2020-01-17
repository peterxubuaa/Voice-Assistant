package com.min.aiassistant.baidu.speech;

import android.content.Context;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.min.aiassistant.baidu.speech.listener.IRecogListener;
import com.min.aiassistant.baidu.speech.listener.RecogEventAdapter;

/**
 * Created by fujiayi on 2017/6/13.
 * EventManager内的方法如send 都可以在主线程中进行，SDK中做过处理
 */

class Recognizer {
    /**
     * SDK 内部核心 EventManager 类
     */
    private EventManager mASR;

    // SDK 内部核心 事件回调类， 用于开发者写自己的识别回调逻辑
    private EventListener mEventListener;

    // 未release前，只能new一个
    private static volatile boolean mIsInited = false;

    /**
     * 初始化
     *
     * @param recogListener 将EventListener结果做解析的DEMO回调。使用RecogEventAdapter 适配EventListener
     */
    Recognizer(Context context, IRecogListener recogListener) {
        this(context, new RecogEventAdapter(recogListener));
    }

    /**
     * 初始化 提供 EventManagerFactory需要的Context和EventListener
     *
     * @param eventListener 识别状态和结果回调
     */
    private Recognizer(Context context, EventListener eventListener) {
        if (mIsInited) {
            throw new RuntimeException("还未调用release()，请勿新建一个新类");
        }
        mIsInited = true;
        mEventListener = eventListener;
        // SDK集成步骤 初始化asr的EventManager示例，多次得到的类，只能选一个使用
        mASR = EventManagerFactory.create(context, "asr");
        // SDK集成步骤 设置回调event， 识别引擎会回调这个类告知重要状态和识别结果
        mASR.registerListener(mEventListener);
    }

    void start(String json) {
        mASR.send(SpeechConstant.ASR_START, json, null, 0, 0);
    }

    /**
     * 提前结束录音等待识别结果。
     */
    void stop() {
        // SDK 集成步骤（可选）停止录音
        if (!mIsInited) {
            throw new RuntimeException("release() was called");
        }
        mASR.send(SpeechConstant.ASR_STOP, "{}", null, 0, 0);
    }

    /**
     * 取消本次识别，取消后将立即停止不会返回识别结果。
     * cancel 与stop的区别是 cancel在stop的基础上，完全停止整个识别流程，
     */
    void cancel() {
        if (!mIsInited) {
            throw new RuntimeException("release() was called");
        }
        // SDK集成步骤 (可选） 取消本次识别
        mASR.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
    }

    void release() {
        if (mASR == null) {
            mIsInited = false;
            return;
        }
        cancel();
        // SDK 集成步骤（可选），卸载listener
        mASR.unregisterListener(mEventListener);
        mASR = null;
        mIsInited = false;
    }
}
