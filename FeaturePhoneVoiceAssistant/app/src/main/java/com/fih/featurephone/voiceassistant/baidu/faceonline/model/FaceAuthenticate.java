package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceAuthenticateJson;

//https://ai.baidu.com/ai-doc/FACE/7k37c1ucj
//https://console.bce.baidu.com/ai/?_=1577153190485&locale=zh-cn#/ai/face/app/list
//完成企业认证，即可获得公安验证接口、身份证与名字比对接口、H5视频活体接口的权限，并获赠免费调用量
public class FaceAuthenticate extends BaiduBaseModel<ParseFaceAuthenticateJson.FaceAuthenticate> {

    public FaceAuthenticate(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = MAP_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/face/v3/person/verify";
        mURLRequestParamMap.put("liveness_control", "HIGH");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
        mURLRequestParamMap.put("quality_control", "HIGH");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败

    }

    protected ParseFaceAuthenticateJson.FaceAuthenticate parseJson(String json) {
        return ParseFaceAuthenticateJson.getInstance().parse(json);
    }

    protected void handleResult(ParseFaceAuthenticateJson.FaceAuthenticate faceAuthenticate) {
        final int FACE_AUTHENTICATE_THRESHOLD = 85;
        if (null == faceAuthenticate.mResult
                || faceAuthenticate.mResult.mScore < FACE_AUTHENTICATE_THRESHOLD) {
            mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_AUTHENTICATE_ACTION);
            return;
        }

        mBaiduBaseListener.onFinalResult("身份验证成功", BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION);

    }

    public void request(String imageFilePath, String idCardNumber, String name) {
        mURLRequestParamMap.put("name", name);//姓名（注：需要是UTF-8编码的中文）
        mURLRequestParamMap.put("id_card_number", idCardNumber);//身份证号码

        super.request(imageFilePath);
    }
//
//    private String requestHostUrl(String imageFilePath, String idCardNumber, String name) {
//        String FACE_AUTHENTICATE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/person/verify";// 请求url
//        try {
//            Map<String, Object> map = new HashMap<>();
//            byte[] buf = FileUtils.readImageFile(imageFilePath);
//            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
//            map.put("image", encodeString);
//            map.put("image_type", "BASE64");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。
//            map.put("name", name);//姓名（注：需要是UTF-8编码的中文）
//            map.put("id_card_number", idCardNumber);//身份证号码
//            map.put("liveness_control", "HIGH");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
//            map.put("quality_control", "HIGH");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
//
//            String jsonParam = new JSONObject(map).toString();
//
//            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
//            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();
//
//            return HttpUtil.post(FACE_AUTHENTICATE_URL, mAccessToken, "application/json", jsonParam);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
