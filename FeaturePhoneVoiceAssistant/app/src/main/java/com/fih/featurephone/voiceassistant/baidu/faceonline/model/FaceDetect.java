package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceDetectJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FaceDetect extends BaseFaceModel {
    private final Map<String, String> FACE_EXPRESSION_MAP = new HashMap<String, String>() {
        {
            put("none", "不笑");
            put("smile", "微笑");
            put("laugh", "大笑");
        }
    };
    private final Map<String, String> FACE_SHAPE_MAP = new HashMap<String, String>() {
        {
            put("square", "正方形");
            put("triangle", "三角形");
            put("oval", "椭圆");
            put("heart", "心形");
            put("round", "圆形");
        }
    };
    private final Map<String, String> FACE_GENDER_MAP = new HashMap<String, String>() {
        {
            put("male", "男性");
            put("female", "女性");
        }
    };
    private final Map<String, String> FACE_GLASSES_MAP = new HashMap<String, String>() {
        {
            put("none", "无眼镜");
            put("common", "普通眼镜");
            put("sun", "墨镜");
        }
    };
    private final Map<String, String> FACE_EMOTION_MAP = new HashMap<String, String>() {
        {
            put("angry", "愤怒");
            put("disgust", "厌恶");
            put("fear", "恐惧");
            put("happy", "高兴");
            put("sad", "伤心");
            put("surprise", "惊讶");
            put("neutral", "无表情");
            put("pouty", "撅嘴");
            put("grimace", "鬼脸");
            put("", "无表情");
        }
    };
    private final Map<String, String> FACE_TYPE_MAP = new HashMap<String, String>() {
        {
            put("human", "真实人脸");
            put("cartoon", "卡通人脸");
        }
    };
    private final Map<String, String> FACE_RACE_MAP = new HashMap<String, String>() {
        {
            put("yellow", "黄色人种");
            put("white", "白色人种");
            put("black", "黑色人种");
            put("brown", "棕色人种");
        }
    };

    public FaceDetect(Context context, BaiduFaceOnlineAI.OnFaceOnlineListener listener) {
        super(context, listener);
    }

    public void request(String imageFilePath) {
        if (null == mFaceOnlineListener) return;

        if (!FileUtils.isFileExist(imageFilePath)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestHostUrl(imageFilePath);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求检测人脸失败！");
            return;
        }

        ParseFaceDetectJson.FaceDetect faceDetect = ParseFaceDetectJson.parse(response);
        if (null == faceDetect) {
            mFaceOnlineListener.onError("检测人脸失败！");
            return;
        }

        if (faceDetect.mErrorCode != 0) {
            mFaceOnlineListener.onError("检测人脸失败信息：" + faceDetect.mErrorMsg);
            return;
        }

        if (null == faceDetect.mResult || faceDetect.mResult.mFaceList.size() == 0) {
            mFaceOnlineListener.onFinalResult("没有检测到人脸", BaiduFaceOnlineAI.FACE_DETECT_ACTION);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (ParseFaceDetectJson.Face face : faceDetect.mResult.mFaceList) {
            sb.append("年龄：").append(face.mAge).append("\n");
            sb.append("美丑打分：").append(face.mBeauty).append("\n");
            sb.append("表情：").append(FACE_EXPRESSION_MAP.get(face.mExpression.mType)).append("\n");
            sb.append("性别：").append(FACE_GENDER_MAP.get(face.mGender.mType)).append("\n");
            sb.append("眼镜：").append(FACE_GLASSES_MAP.get(face.mGlasses.mType)).append("\n");
            sb.append("人种：").append(FACE_RACE_MAP.get(face.mRace.mType)).append("\n");
            sb.append("情绪：").append(FACE_EMOTION_MAP.get(face.mEmotion.mType)).append("\n");
            sb.append("脸型：").append(FACE_SHAPE_MAP.get(face.mFaceShape.mType)).append("\n");
            sb.append("人脸类别：").append(FACE_TYPE_MAP.get(face.mFaceType.mType)).append("\n\n");

            sb.append("顺时针旋转角：").append(face.mLocation.mRotation).append("\n");
            sb.append("左右旋转角：").append(face.mAngle.mYaw).append("\n");
            sb.append("俯仰角度：").append(face.mAngle.mPitch).append("\n");
            sb.append("平面内旋转角：").append(face.mAngle.mRoll).append("\n");
            sb.append("左眼状态：").append(getEyeStatusDescription(face.mEyeStatus.mLeftEye)).append("\n");
            sb.append("右眼状态：").append(getEyeStatusDescription(face.mEyeStatus.mRightEye)).append("\n");
            sb.append("左眼遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mLeftEye)).append("\n");
            sb.append("右眼遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mRightEye)).append("\n");
            sb.append("鼻子遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mNose)).append("\n");
            sb.append("嘴巴遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mMouth)).append("\n");
            sb.append("左脸颊遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mLeftCheek)).append("\n");
            sb.append("右脸颊遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mRightCheek)).append("\n");
            sb.append("下巴颊遮挡比例：").append(getOcclusionDescription(face.mQuality.mOcclusion.mChinContour)).append("\n");
            sb.append("人脸模糊程度：").append(getBlurDescription(face.mQuality.mBlur)).append("\n");
            sb.append("脸部光照程度[0~255]：").append(face.mQuality.mIllumination).append("\n");
            sb.append("人脸完整度：").append(face.mQuality.mCompleteness == 1? "是" : "否");
            mFaceOnlineListener.onFinalResult(sb.toString(), BaiduFaceOnlineAI.FACE_DETECT_ACTION);
        }
    }

    private String getBlurDescription(double rate){
        if (1 == rate) {
            return "完全模糊";
        } else if (0 == rate) {
            return "完全清晰";
        } else if (rate < 0.4) {
            return "大部分清晰";
        } else if (rate > 0.6) {
            return "大部分模糊";
        } else {
            return "比较模糊";
        }
    }

    private String getEyeStatusDescription(double rate) {
        if (1 == rate) {
            return "完全睁眼";
        } else if (0 == rate) {
            return "完全闭眼";
        } else if (rate < 0.4) {
            return "基本闭眼";
        } else if (rate > 0.6) {
            return "基本睁眼";
        } else {
            return "眯眼";
        }
    }

    private String getOcclusionDescription(double rate) {
        if (1 == rate) {
            return "完全遮挡";
        } else if (0 == rate) {
            return "没有遮挡";
        } else if (rate < 0.4) {
            return "小部分遮挡";
        } else if (rate > 0.6) {
            return "大部分遮挡";
        } else {
            return "一半遮挡";
        }
    }

    private String requestHostUrl(String imageFilePath) {
        // 请求url
        String FACE_DETECT_URL = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
        try {
            Map<String, Object> map = new HashMap<>();
            byte[] buf = FileUtils.readImageFile(imageFilePath);
            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
//            String imgParam = URLEncoder.encode(encodeString, "UTF-8");
            map.put("image", encodeString);
            map.put("face_field", "age,beauty,expression,face_shape,gender,glasses,landmark,landmark150,race,quality,eye_status,emotion,face_type");
            map.put("image_type", "BASE64");
            map.put("max_face_num", 1);
            map.put("liveness_control", "NONE");

            String jsonParam = new JSONObject(map).toString();

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();

            return HttpUtil.post(FACE_DETECT_URL, mAccessToken, "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
