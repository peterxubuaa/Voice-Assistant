package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceIdentifyJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FaceIdentify extends BaseFaceModel {
    private final float MIN_SCORE = 85;

    public FaceIdentify(Context context, BaiduFaceOnlineAI.OnFaceOnlineListener listener) {
        super(context, listener);
    }

    public void request(String imageFilePath, boolean question) {
        if (null == mFaceOnlineListener) return;

        if (!FileUtils.isFileExist(imageFilePath)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(imageFilePath);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求查找人脸失败！");
            return;
        }

        ParseFaceIdentifyJson.FaceIdentify faceIdentify = ParseFaceIdentifyJson.getInstance().parse(response);
        if (null == faceIdentify) {
            mFaceOnlineListener.onError("查找人脸失败！");
            return;
        }

        if (faceIdentify.mErrorCode != 0) {
            mFaceOnlineListener.onError("查找人脸失败信息：" + faceIdentify.mErrorMsg);
            return;
        }

        if (null == faceIdentify.mResult || faceIdentify.mResult.mUserList.size() == 0
                || faceIdentify.mResult.mUserList.get(0).mScore < MIN_SCORE) {
            mFaceOnlineListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);
            return;
        }

        String userInfo = faceIdentify.mResult.mUserList.get(0).mUserInfo;
        if (question) {
            mFaceOnlineListener.onFinalResult(userInfo, BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION);
        } else {
            mFaceOnlineListener.onFinalResult(userInfo, BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);
        }
    }

    private String requestHostUrl(String imageFilePath) {
        // 请求url
        String FACE_IDENTIFY_URL = "https://aip.baidubce.com/rest/2.0/face/v3/search";
        try {
            Map<String, Object> map = new HashMap<>();
            byte[] buf = FileUtils.readImageFile(imageFilePath);
            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
            map.put("image", encodeString);
            map.put("image_type", "BASE64");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。

            map.put("group_id_list", DEFAULT_GROUP_ID);//从指定的group中进行查找 用逗号分隔，上限10个
            map.put("liveness_control", "NONE");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
            map.put("quality_control", "NORMAL");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
//            map.put("user_id", "");//当需要对特定用户进行比对时，指定user_id进行比对。即人脸认证功能。
//            map.put("max_user_num", 1);//查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回50个

            String jsonParam = new JSONObject(map).toString();

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();

            return HttpUtil.post(FACE_IDENTIFY_URL, mAccessToken, "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
