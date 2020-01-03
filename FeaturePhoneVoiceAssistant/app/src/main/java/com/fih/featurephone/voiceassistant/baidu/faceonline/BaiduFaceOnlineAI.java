package com.fih.featurephone.voiceassistant.baidu.faceonline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceCompareActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceMergeActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.activity.OnlineFaceUserManagerActivity;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceAuthenticate;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceDetect;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceIdentify;

public class BaiduFaceOnlineAI extends BaiduBaseAI {

    public static final int FACE_IDENTIFY_ACTION = 100;
    public static final int FACE_IDENTIFY_IMAGE_ACTION = 101;
    public static final int FACE_IDENTIFY_QUESTION_ACTION = 102;
    public static final int FACE_DETECT_ACTION = 200;
    public static final int FACE_DETECT_IMAGE_ACTION = 201;
    public static final int FACE_REGISTER_ACTION = 300;
    public static final int FACE_UPDATE_ACTION = 400;
    public static final int FACE_DELETE_ACTION = 500;
    public static final int FACE_DELETE_LIST_ACTION = 501;
//    public static final int FACE_QUERY_USER_LIST_ACTION = 600;
//    public static final int FACE_QUERY_USER_INFO_ACTION = 700;
//    public static final int FACE_QUERY_FACE_INFO_ACTION = 701;
    public static final int FACE_QUERY_ALL_USER_INFO_ACTION = 800;
    public static final int FACE_MERGE_ACTION = 900;
    public static final int FACE_COMPARE_ACTION = 1000;
    public static final int FACE_AUTHENTICATE_ACTION = 1100;
    public static final int FACE_AUTHENTICATE_IMAGE_ACTION = 1101;

    private Activity mActivity;

    private FaceIdentify mFaceIdentify;
    private FaceDetect mFaceDetect;
    private FaceAuthenticate mFaceAuthenticate;

    public BaiduFaceOnlineAI(Context context, IBaiduBaseListener listener) {
        super(context, listener);

        mActivity = (Activity)context;

        mFaceIdentify = new FaceIdentify(context, listener);
        mFaceDetect = new FaceDetect(context, listener);
        mFaceAuthenticate =  new FaceAuthenticate(context, listener);
    }

    public void initBaiduFace() {
    }

    public void releaseBaiduFace() {
    }

    public void onIdentifyThread(final String imageFilePath, final boolean question, final int type) {
        new Thread() {
            @Override
            public void run() {
                mFaceIdentify.request(imageFilePath, question, type);
            }
        }.start();
    }

    public void onDetectThread(final String imageFilePath) {
        new Thread() {
            @Override
            public void run() {
                mFaceDetect.request(imageFilePath);
            }
        }.start();
    }

    public void onAuthenticateThread(final String imageFilePath, final String idCardNumber, final String name) {
        new Thread() {
            @Override
            public void run() {
                mFaceAuthenticate.request(imageFilePath, idCardNumber, name);
            }
        }.start();
    }

    public void onFaceManagerActivity() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceUserManagerActivity.class));
    }

    public void onFaceMergeActivity() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceMergeActivity.class));
    }

    public void onFaceCompareActivity() {
        mActivity.startActivity(new Intent(mActivity, OnlineFaceCompareActivity.class));
    }
}
