package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceCompareJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//https://ai.baidu.com/ai-doc/FACE/Lk37c1tpf
public class FaceCompare extends BaiduBaseModel<ParseFaceCompareJson.FaceCompare> {

    public FaceCompare(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    public void request(String firstFilePath, String secondFilePath) {
        if (null == mBaiduBaseListener) return;

        if (!FileUtils.isFileExist(firstFilePath) || !FileUtils.isFileExist(secondFilePath)) {
            mBaiduBaseListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(firstFilePath, secondFilePath);
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求失败！");
            return;
        }

        ParseFaceCompareJson.FaceCompare faceCompare = ParseFaceCompareJson.getInstance().parse(response);
        if (null == faceCompare) {
            mBaiduBaseListener.onError("分析Json失败！");
            return;
        }

        if (faceCompare.mErrorCode != 0) {
            mBaiduBaseListener.onError("服务器返回失败信息：" + faceCompare.mErrorMsg);
            return;
        }
        if (null == faceCompare.mResult) {
            mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_COMPARE_ACTION);
            return;
        }

        mBaiduBaseListener.onFinalResult(String.valueOf(faceCompare.mResult.mScore), BaiduFaceOnlineAI.FACE_COMPARE_ACTION);
    }

    private String requestHostUrl(String firstFilePath, String secondFilePath) {
        // 请求url
        String FACE_COMPARE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/match";
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

            return HttpUtil.post(FACE_COMPARE_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected ParseFaceCompareJson.FaceCompare parseJson(String json) {//无用
        return null;
    }

    protected void handleResult(ParseFaceCompareJson.FaceCompare result){}
}
