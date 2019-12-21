package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;

import com.fih.featurephone.voiceassistant.baidu.BaiduUtil;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;

public class BaseFaceModel {
    public static final String DEFAULT_GROUP_ID = "default";

    Context mContext;
    static String mAccessToken; //只获取一次
    BaiduFaceOnlineAI.OnFaceOnlineListener mFaceOnlineListener;

    BaseFaceModel(Context context, BaiduFaceOnlineAI.OnFaceOnlineListener listener) {
        mContext = context;
        mFaceOnlineListener = listener;
    }

    String getAuthToken() {
        return new BaiduUtil().getFaceToken(mContext);
    }
}
