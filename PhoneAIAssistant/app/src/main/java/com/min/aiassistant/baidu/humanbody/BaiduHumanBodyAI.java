package com.min.aiassistant.baidu.humanbody;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.humanbody.model.BodySegment;
import com.min.aiassistant.baidu.humanbody.model.Gesture;
import com.min.aiassistant.baidu.humanbody.model.Headcount;

public class BaiduHumanBodyAI extends BaiduBaseAI {
    public static final int GESTURE_TYPE = 1;
    public static final int HEADCOUNT_TYPE = 2;
    public static final int BODY_SEGMENT_TYPE = 3;

    public static final int GESTURE_ACTION = 1;
    public static final int GESTURE_IMAGE_ACTION = 11;
    public static final int HEAD_COUNT_ACTION = 2;
    public static final int HEAD_COUNT_IMAGE_ACTION = 22;
    public static final int BODY_SEGMENT_IMAGE_ACTION = 33;

    private Gesture mGesture;
    private Headcount mHeadcount;
    private BodySegment mBodySegment;

    public BaiduHumanBodyAI(Context context, IBaiduBaseListener listener) {
        mGesture = new Gesture(context, listener);
        mHeadcount = new Headcount(context, listener);
        mBodySegment = new BodySegment(context, listener);
    }

    public void action(final int type, final String imageFilePath) {
        new Thread() {
            @Override
            public void run() {
                switch (type) {
                    case GESTURE_TYPE:
                        mGesture.request(imageFilePath);
                        break;
                    case HEADCOUNT_TYPE:
                        mHeadcount.request(imageFilePath);
                        break;
                    case BODY_SEGMENT_TYPE:
                        mBodySegment.request(imageFilePath);
                        break;
                }
            }
        }.start();
    }
}
