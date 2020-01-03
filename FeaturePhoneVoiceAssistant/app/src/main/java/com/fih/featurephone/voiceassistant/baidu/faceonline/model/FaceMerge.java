package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceMergeJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//https://ai.baidu.com/ai-doc/FACE/5k37c1ti0
public class FaceMerge extends BaiduBaseModel<ParseFaceMergeJson.FaceMerge> {

    public FaceMerge(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    public void request(String templateFilePath, String targetFilePath) {
        if (null == mBaiduBaseListener) return;

        if (!FileUtils.isFileExist(templateFilePath) || !FileUtils.isFileExist(targetFilePath)) {
            mBaiduBaseListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(templateFilePath, targetFilePath);
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求融合人脸失败！");
            return;
        }

        ParseFaceMergeJson.FaceMerge faceMerge = ParseFaceMergeJson.getInstance().parse(response);
        if (null == faceMerge) {
            mBaiduBaseListener.onError("融合人脸失败！");
            return;
        }

        if (faceMerge.mErrorCode != 0) {
            mBaiduBaseListener.onError("融合人脸失败信息：" + faceMerge.mErrorMsg);
            return;
        }

        if (null == faceMerge.mResult || TextUtils.isEmpty(faceMerge.mResult.mMergeImage)) {
            mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
            return;
        }

        String mergeImageFile = mContext.getFilesDir() + File.separator + "merge.jpg";
        FileUtils.deleteFile(mergeImageFile);
        if (FileUtils.writeImageFile(Base64.decode(faceMerge.mResult.mMergeImage, Base64.DEFAULT), mergeImageFile)) {
            mBaiduBaseListener.onFinalResult(mergeImageFile, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_MERGE_ACTION);
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

            return HttpUtil.post(FACE_MERGE_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
