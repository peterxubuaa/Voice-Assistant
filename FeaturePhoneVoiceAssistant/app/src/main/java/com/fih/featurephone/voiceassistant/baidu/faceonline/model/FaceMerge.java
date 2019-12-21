package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceMergeJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FaceMerge extends BaseFaceModel {

    public FaceMerge(Context context, BaiduFaceOnlineAI.OnFaceOnlineListener listener) {
        super(context, listener);
    }

    public void request(String templateFilePath, String targetFilePath) {
        if (null == mFaceOnlineListener) return;

        if (!FileUtils.isFileExist(templateFilePath) || !FileUtils.isFileExist(targetFilePath)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(templateFilePath, targetFilePath);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求融合人脸失败！");
            return;
        }

        ParseFaceMergeJson.FaceMerge faceMerge = ParseFaceMergeJson.getInstance().parse(response);
        if (null == faceMerge) {
            mFaceOnlineListener.onError("融合人脸失败！");
            return;
        }

        if (faceMerge.mErrorCode != 0) {
            mFaceOnlineListener.onError("融合人脸失败信息：" + faceMerge.mErrorMsg);
            return;
        }

        if (null == faceMerge.mResult || TextUtils.isEmpty(faceMerge.mResult.mMergeImage)) {
            mFaceOnlineListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
            return;
        }

        String mergeImageFile = mContext.getFilesDir() + File.separator + "merge.jpg";
        FileUtils.deleteFile(mergeImageFile);
        if (FileUtils.writeImageFile(Base64.decode(faceMerge.mResult.mMergeImage, Base64.DEFAULT), mergeImageFile)) {
            mFaceOnlineListener.onFinalResult(mergeImageFile, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
        } else {
            mFaceOnlineListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
        }
    }

    private String requestHostUrl(String templateFilePath, String targetFilePath) {
        // 请求url
        String FACE_MERGE_URL = "https://aip.baidubce.com/rest/2.0/face/v1/merge";
        try {
            Map<String, Object> map = new HashMap<>();

            Map<String, Object> image_templateMap = new HashMap<>(); //模板图信息，要求被融合的人脸边缘需要与图片边缘保持一定距离，
            byte[] templateBuf = FileUtils.readImageFile(templateFilePath);//模板图信息 图片的分辨率要求在1920x1080以下
            String templateEncodeString = Base64.encodeToString(templateBuf, Base64.DEFAULT);
            image_templateMap.put("image", templateEncodeString);
            image_templateMap.put("image_type", "BASE64");
            image_templateMap.put("quality_control", "NONE");
            map.put("image_template", image_templateMap);

            Map<String, Object> image_targetMap = new HashMap<>();
            byte[] targetBuf = FileUtils.readImageFile(targetFilePath);
            String targetEncodeString = Base64.encodeToString(targetBuf, Base64.DEFAULT);
            image_targetMap.put("image", targetEncodeString);//目标图信息 图片的分辨率要求在1920x1080以下
            image_targetMap.put("image_type", "BASE64");
            image_targetMap.put("quality_control", "NONE");
            map.put("image_target", image_targetMap);

            String jsonParam = new JSONObject(map).toString();

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();

            return HttpUtil.post(FACE_MERGE_URL, mAccessToken, "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
