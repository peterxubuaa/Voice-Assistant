package com.fih.featurephone.voiceassistant.baidu.faceonline;

import android.app.Activity;
import android.content.Intent;

import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceCompareActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceMergeActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceUserManagerActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceDetect;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceIdentify;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BaiduFaceOnlineAI {

    public static final int FACE_IDENTIFY_ACTION = 1;
    public static final int FACE_IDENTIFY_QUESTION_ACTION = 11;
    public static final int FACE_DETECT_ACTION = 2;
    public static final int FACE_REGISTER_ACTION = 3;
    public static final int FACE_UPDATE_ACTION = 4;
    public static final int FACE_DELETE_ACTION = 5;
    public static final int FACE_DELETE_LIST_ACTION = 55;
//    public static final int FACE_QUERY_USER_LIST_ACTION = 6;
//    public static final int FACE_QUERY_USER_INFO_ACTION = 7;
//    public static final int FACE_QUERY_FACE_INFO_ACTION = 7;
    public static final int FACE_QUERY_ALL_USER_INFO_ACTION = 8;
    public static final int FACE_MERGE_ACTION = 9;
    public static final int FACE_COMPARE_ACTION = 10;

    private Activity mActivity;
    private ExecutorService mFaceExecutorService = Executors.newSingleThreadExecutor();
    private Future mFaceTaskFuture;

    private FaceIdentify mFaceIdentify;
    private FaceDetect mFaceDetect;

    public interface OnFaceOnlineListener {
        void onError(String msg);
        void onFinalResult(Object result, int resultType);
    }

    public BaiduFaceOnlineAI(Activity activity, OnFaceOnlineListener listener) {
        mActivity = activity;

        mFaceIdentify = new FaceIdentify(activity, listener);
        mFaceDetect = new FaceDetect(activity, listener);
    }

    public void initBaiduFace() {
    }

    public void releaseBaiduFace() {
    }

    public void onIdentify(final String imageFilePath, final boolean question) {
        if (mFaceTaskFuture != null && !mFaceTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mFaceIdentify.request(imageFilePath, question);
            }
        });
    }

    public void onDetect(final String imageFilePath) {
        if (mFaceTaskFuture != null && !mFaceTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mFaceDetect.request(imageFilePath);
            }
        });
    }

    public void onFaceManager() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceUserManagerActivity.class));
    }

    public void onFaceMerge() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceMergeActivity.class));
    }

    public void onFaceCompare() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceCompareActivity.class));
    }
}
