/*
 * Copyright (C) 2019 Baidu, Inc. All Rights Reserved.
 */
package com.fih.featurephone.voiceassistant.baidu.face.callback;


/**
 * 人脸特征抽取回调接口。
 *
 * @Time: 2019/5/30
 * @Author: v_zhangxiaoqing01
 */
public interface FaceFeatureCallBack {
    void onFaceFeatureCallBack(float featureSize, byte[] feature);
}
