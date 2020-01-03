package com.fih.featurephone.voiceassistant.baidu.humanbody;

import android.content.Context;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.humanbody.model.Gesture;
import com.fih.featurephone.voiceassistant.baidu.humanbody.model.Headcount;

public class BaiduHumanBodyAI extends BaiduBaseAI {
    public static final int GESTURE_ACTION = 1;
    public static final int GESTURE_IMAGE_ACTION = 11;
    public static final int HEAD_COUNT_ACTION = 2;
    public static final int HEAD_COUNT_IMAGE_ACTION = 22;

    private Gesture mGesture;
    private Headcount mHeadcount;

    public BaiduHumanBodyAI(Context context, IBaiduBaseListener listener) {
        super(context, listener);

        mGesture = new Gesture(context, listener);
        mHeadcount = new Headcount(context, listener);
    }

    public void onRecognizeGestureThread(final String imageFilePath) {
        new Thread() {
            @Override
            public void run() {
                mGesture.request(imageFilePath);
            }
        }.start();
    }

    public void onHeadCountThread(final String imageFilePath) {
        new Thread() {
            @Override
            public void run() {
                mHeadcount.request(imageFilePath, true);
            }
        }.start();
    }
}
