package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduUtil;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import java.net.URLEncoder;

abstract public class BaseClassify<E> {
    final double MIN_SCORE = 0.5;
    BaiduClassifyImageAI.OnClassifyImageListener mClassifyImageListener;
    Context mContext;
    private static String mAccessToken = ""; //只需要获取一次
    private String mHostUrl;
    private String mRequestParams;

    BaseClassify(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParams) {
        mContext = context;
        mClassifyImageListener = listener;
        mHostUrl = hostUrl;
        mRequestParams = requestParams;
    }

    public void classifyImage(String imageFilePath, boolean question) {
        String responseJson = getClassifyResult(imageFilePath);
        if (TextUtils.isEmpty(responseJson)) {
            if (null != mClassifyImageListener) mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_fail));
            return;
        }

        E response = analyzeJson(responseJson);
        if (null == response) {
            if (null != mClassifyImageListener) mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_json_fail));
            return;
        }

        handleResult(response, question);
    }

    private String getClassifyResult(String imageFilePath) {
        try {
            byte[] buf = FileUtils.readImageFile(imageFilePath);
            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
            String imgParam = URLEncoder.encode(encodeString, "UTF-8");
            // 请求参数
            String param = "image=" + imgParam + mRequestParams;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = new BaiduUtil().getClassifyImageToken(mContext);//getAuth();

            return HttpUtil.post(mHostUrl, mAccessToken, "application/x-www-form-urlencoded", param);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != mClassifyImageListener) mClassifyImageListener.onError(mContext.getString(R.string.network_error));
        }
        return null;
    }

    abstract E analyzeJson(String result);

    abstract void handleResult(E response, boolean question);
}
