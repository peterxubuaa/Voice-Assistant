package com.min.aiassistant.baidu.faceonline.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.parsejson.ParseFaceDetectJson;
import com.min.aiassistant.utils.BitmapUtils;
import com.min.aiassistant.utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//https://ai.baidu.com/ai-doc/FACE/yk37c1u4t
public class FaceDetect extends BaiduImageBaseModel<ParseFaceDetectJson.FaceDetect> {
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

    public FaceDetect(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);

        mURLRequestParamType = MAP_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/face/v3/detect";
        mURLRequestParamMap.put("face_field", "age,beauty,expression,face_shape,gender,glasses,landmark,landmark72,landmark150,race,quality,eye_status,emotion,face_type");
        mURLRequestParamMap.put("image_type", "BASE64");
        mURLRequestParamMap.put("max_face_num", 10);
        mURLRequestParamMap.put("liveness_control", "NONE");
    }

    protected ParseFaceDetectJson.FaceDetect parseJson(String json) {
        return ParseFaceDetectJson.getInstance().parse(json);
    }

    protected void handleResult(ParseFaceDetectJson.FaceDetect faceDetect) {
         if (null == faceDetect.mResult || faceDetect.mResult.mFaceList.size() == 0) {
             mBaiduBaseListener.onFinalResult("没有检测到人脸", BaiduFaceOnlineAI.FACE_DETECT_ACTION);
            return;
        }

        String faceBaseImageFilePath = mContext.getFilesDir() + File.separator + "face_detect_";
        Bitmap inputBitmap = BitmapFactory.decodeFile(mImageFilePath);
        int index = 1;
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

            sb.append("顺时针旋转角：").append(face.mLocationF.mRotation).append("\n");
            sb.append("左右旋转角：").append(face.mAngle.mYaw).append("\n");
            sb.append("俯仰角度：").append(face.mAngle.mPitch).append("\n");
            sb.append("平面内旋转角：").append(face.mAngle.mRoll).append("\n");
            sb.append("左眼状态：").append(getEyeStatusDescription(face.mEyeStatus.mLeftEye)).append("\n");
            sb.append("右眼状态：").append(getEyeStatusDescription(face.mEyeStatus.mRightEye)).append("\n");
            sb.append("人脸模糊程度：").append(getBlurDescription(face.mQuality.mBlur)).append("\n");
            sb.append("脸部光照程度[0~255]：").append(face.mQuality.mIllumination).append("\n");
            sb.append("人脸完整度：").append(face.mQuality.mCompleteness == 1? "完整" : "残缺").append("\n");
            sb.append("人脸置信度：").append(getFaceProbabilityDescription(face.mFaceProbability)).append(face.mFaceProbability).append("\n");

            Rect cropRect = new Rect((int)face.mLocationF.mLeft, (int)face.mLocationF.mTop,
                    (int)face.mLocationF.mLeft + face.mLocationF.mWidth, (int)face.mLocationF.mTop + face.mLocationF.mHeight);
            String faceImageFilePath = faceBaseImageFilePath + index + ".jpg";
            index++;
            if (FileUtils.isFileExist(faceImageFilePath)) FileUtils.deleteFile(faceImageFilePath);
            BitmapUtils.saveCropJpeg(inputBitmap, cropRect, faceImageFilePath);
            mBaiduBaseListener.onFinalResult(faceImageFilePath, BaiduFaceOnlineAI.FACE_DETECT_IMAGE_ACTION);

            mBaiduBaseListener.onFinalResult(sb.toString(), BaiduFaceOnlineAI.FACE_DETECT_ACTION);
            sb.delete(0, sb.length());
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

    private String getFaceProbabilityDescription(double rate) {
        if (1 == rate) {
            return "一定是人脸";
        } else if (0 == rate) {
            return "不是人脸";
        } else if (rate < 0.3) {
            return "基本不是人脸";
        } else if (rate > 0.7) {
            return "基本是人脸";
        } else {
            return "可能是人脸";
        }
    }
}
