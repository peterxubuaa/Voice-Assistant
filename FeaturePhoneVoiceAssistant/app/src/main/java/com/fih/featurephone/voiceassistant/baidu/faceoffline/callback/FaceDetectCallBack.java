/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.fih.featurephone.voiceassistant.baidu.faceoffline.callback;


import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.LivenessModel;

/**
 * 人脸检测回调接口。
 */
public interface FaceDetectCallBack {
    void onFaceDetectCallback(LivenessModel livenessModel);
    void onTip(int code, String msg);
    void onFaceDetectDrawCallback(LivenessModel livenessModel);
}
