package com.fih.featurephone.voiceassistant.baidu.faceoffline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.activity.FaceAuthActivity;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.activity.FaceRGBIdentifyActivity;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.activity.FaceUserManagerActivity;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.listener.SdkInitListener;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.utils.ConfigUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.io.File;

public class BaiduFaceOfflineAI {
    private Activity mActivity;
    private boolean mReady;
    private OnFaceOfflineListener mListener;

    public interface OnFaceOfflineListener {
        void onError(String msg);
        void onFinalResult(Object result, int resultType);
    }

    public BaiduFaceOfflineAI(Activity activity, OnFaceOfflineListener listener) {
        mActivity = activity;
        mListener = listener;
        mReady = false;
    }

    public void initBaiduFace() {
        final String configFilePath = mActivity.getFilesDir() + File.separator + "faceConfig.txt";
        boolean isConfigExit = ConfigUtils.isConfigExit(configFilePath);
        boolean isInitConfig = ConfigUtils.initConfig(configFilePath);
        if (isInitConfig && isConfigExit) {
            CommonUtil.toast(mActivity, mActivity.getString(R.string.baidu_face_config_success));
        } else {
            CommonUtil.toast(mActivity, mActivity.getString(R.string.baidu_face_config_fail));
            ConfigUtils.modifyJson(configFilePath);
        }
        initOffLineFaceLicense();
    }

    public void releaseBaiduFace() {
    }
    /**
     * 启动应用程序，如果之前初始过，自动初始化鉴权和模型（可以添加到Application 中）
     */
    private void initOffLineFaceLicense() {
        if (FaceSDKManager.sInitStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().init(mActivity, new SdkInitListener() {
                @Override
                public void initStart() {}
                @Override
                public void initLicenseSuccess() {
                    CommonUtil.toast(mActivity,
                            mActivity.getString(R.string.baidu_face_auth_success));
                }
                @Override
                public void initLicenseFail(int errorCode, String msg) {
                    // 如果授权失败，跳转授权页面
                    CommonUtil.toast(mActivity,
                            mActivity.getString(R.string.baidu_face_auth_fail) + msg + " (" + errorCode + ")");
                    mActivity.startActivity(new Intent(mActivity, FaceAuthActivity.class));
                }
                @Override
                public void initModelSuccess() {
                    mReady = true;
                    CommonUtil.toast(mActivity, mActivity.getString(R.string.baidu_face_model_success));
                }
                @Override
                public void initModelFail(int errorCode, String msg) {
                    CommonUtil.toast(mActivity,
                            mActivity.getString(R.string.baidu_face_model_fail) + msg + " (" + errorCode + ")");
                }
            });
        }
    }

    public void onFaceManager() {
        if (mReady) mActivity.startActivity(new Intent(mActivity, FaceUserManagerActivity.class));
    }

    public void onFaceDetect(int requestResult) {
        if (mReady) mActivity.startActivityForResult(new Intent(mActivity, FaceRGBIdentifyActivity.class), requestResult);
    }

    public void identifyUser(String faceImagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(faceImagePath);
        if (null != bitmap) {
            FaceSDKManager.getInstance().identifyImage(bitmap, mListener);
        }
    }
}
