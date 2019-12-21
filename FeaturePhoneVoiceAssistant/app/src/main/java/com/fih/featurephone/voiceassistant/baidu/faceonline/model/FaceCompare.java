package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceCompareJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceCompare extends BaseFaceModel {

    public FaceCompare(Context context, BaiduFaceOnlineAI.OnFaceOnlineListener listener) {
        super(context, listener);
    }

    public void request(String firstFilePath, String secondFilePath) {
        if (null == mFaceOnlineListener) return;

        if (!FileUtils.isFileExist(firstFilePath) || !FileUtils.isFileExist(secondFilePath)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(firstFilePath, secondFilePath);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求比较人脸失败！");
            return;
        }

        ParseFaceCompareJson.FaceCompare faceCompare = ParseFaceCompareJson.getInstance().parse(response);
        if (null == faceCompare) {
            mFaceOnlineListener.onError("比较人脸失败！");
            return;
        }

        if (faceCompare.mErrorCode != 0) {
            mFaceOnlineListener.onError("比较人脸失败信息：" + faceCompare.mErrorMsg);
            return;
        }

        if (null == faceCompare.mResult) {
            mFaceOnlineListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_COMPARE_ACTION);
            return;
        }

        mFaceOnlineListener.onFinalResult(String.valueOf(faceCompare.mResult.mScore), BaiduFaceOnlineAI.FACE_COMPARE_ACTION);
    }

    private String requestHostUrl(String firstFilePath, String secondFilePath) {
        // 请求url
        String FACE_MERGE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/match";
        try {
            Map<String, Object> firstImageMap = new HashMap<>(); //模板图信息，要求被融合的人脸边缘需要与图片边缘保持一定距离，
            byte[] firstBuf = FileUtils.readImageFile(firstFilePath);//模板图信息 图片的分辨率要求在1920x1080以下
            String firstEncodeString = Base64.encodeToString(firstBuf, Base64.DEFAULT);
            firstImageMap.put("image", firstEncodeString);
            firstImageMap.put("image_type", "BASE64");
            firstImageMap.put("face_type", "LIVE");
            firstImageMap.put("quality_control", "NONE");
            firstImageMap.put("liveness_control", "NONE");

            Map<String, Object> secondImageMap = new HashMap<>();
            byte[] secondBuf = FileUtils.readImageFile(secondFilePath);
            String secondEncodeString = Base64.encodeToString(secondBuf, Base64.DEFAULT);
            secondImageMap.put("image", secondEncodeString);//目标图信息 图片的分辨率要求在1920x1080以下
            secondImageMap.put("image_type", "BASE64");
            secondImageMap.put("face_type", "LIVE");
            secondImageMap.put("quality_control", "NONE");
            secondImageMap.put("liveness_control", "NONE");

            List<Map<String,Object>> postList = new ArrayList<>();
            postList.add(firstImageMap);
            postList.add(secondImageMap);

            String jsonParam = new JSONArray(postList).toString();

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();

            return HttpUtil.post(FACE_MERGE_URL, mAccessToken, "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
