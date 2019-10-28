package com.fih.featurephone.voiceassistant.tts.control;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * 在新线程中调用initTTs方法。防止UI柱塞
 * <p>
 * Created by fujiayi on 2017/5/24.
 */

public class NonBlockSyntherizer extends MySyntherizer {
    private static final int INIT = 1;
    private static final int RELEASE = 11;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    public NonBlockSyntherizer(Context context, InitConfig initConfig, Handler mainHandler) {
        super(context, mainHandler);
        initThread();
        runInHandlerThread(INIT, initConfig);
    }

    private void initThread() {
        mHandlerThread = new HandlerThread("NonBlockSyntherizer-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case INIT:
                        InitConfig config = (InitConfig) msg.obj;
                        boolean isSuccess = init(config);
                        if (isSuccess) {
                            // speak("初始化成功");
                            sendToUiThread("NonBlockSyntherizer 初始化成功");
                        } else {
                            sendToUiThread("合成引擎初始化失败, 请查看日志");
                        }
                        break;
                    case RELEASE:
                        NonBlockSyntherizer.super.release();
                        break;
                    default:
                        break;
                }

            }
        };
    }

    @Override
    public void release() {
        runInHandlerThread(RELEASE, null);
        mHandlerThread.quitSafely();
    }

    private void runInHandlerThread(int action, Object obj) {
        Message msg = Message.obtain();
        msg.what = action;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }
}
