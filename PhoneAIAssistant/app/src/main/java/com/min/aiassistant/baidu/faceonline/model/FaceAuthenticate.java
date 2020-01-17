package com.min.aiassistant.baidu.faceonline.model;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.parsejson.ParseFaceAuthenticateJson;

//https://ai.baidu.com/ai-doc/FACE/7k37c1ucj
//https://console.bce.baidu.com/ai/?_=1577153190485&locale=zh-cn#/ai/face/app/list
//完成企业认证，即可获得公安验证接口、身份证与名字比对接口、H5视频活体接口的权限，并获赠免费调用量
public class FaceAuthenticate extends BaiduImageBaseModel<ParseFaceAuthenticateJson.FaceAuthenticate> {

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
}
