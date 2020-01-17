package com.min.aiassistant.baidu.faceonline.parsejson;

import android.graphics.Point;
import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top
public class ParseFaceDetectJson extends BaiduParseBaseJson {

    private static ParseFaceDetectJson sParseFaceDetectJson = null;

    public static ParseFaceDetectJson getInstance() {
        if (null == sParseFaceDetectJson) {
            sParseFaceDetectJson = new ParseFaceDetectJson();
        }
        return sParseFaceDetectJson;
    }

    public class FaceDetect extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        int mFaceNum;//检测到的图片中的人脸数量
        public ArrayList<Face> mFaceList;
    }

    public class Face {
        String mFaceToken;//人脸图片的唯一标识
        public LocationF mLocationF;//人脸在图片中的位置
        public int mFaceProbability;//人脸置信度，范围【0~1】，代表这是一张人脸的概率，0最小、1最大。
        public Angle mAngle;//人脸旋转角度参数
        public int mAge;
        public double mBeauty;//美丑打分，范围0-100，越大表示越美。
        public FaceAttribute mExpression;//表情
        public FaceAttribute mFaceShape;//脸型
        public FaceAttribute mGender;//性别
        public FaceAttribute mGlasses;//是否带眼镜
        public FaceAttribute mRace;//人种
        public FaceAttribute mEmotion;//情绪
        public FaceAttribute mFaceType;//真实人脸/卡通人脸
        ArrayList<Point> mLandMarkList;
        ArrayList<Point> mLandMark72List;
//        ArrayList<Point> mLandMark150List;
        public Quality mQuality;//人脸质量信息
        public EyStatus mEyeStatus;//双眼状态（睁开/闭合）
    }

    public class Angle {
        public double mYaw;
        public double mPitch;
        public double mRoll;
    }

    public class FaceAttribute {
        public String mType;
        double mProbability;
    }

    public class Quality {
        Occlusion mOcclusion;
        public int mBlur;
        public int mIllumination;
        public int mCompleteness;
    }

    public static class Occlusion {
        int mLeftEye;
        int mRightEye;
        int mNose;
        int mMouth;
        int mLeftCheek;
        int mRightCheek;
        int mChinContour;
    }

    public class EyStatus {
        public double mLeftEye;
        public double mRightEye;
    }

    public FaceDetect parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceDetect faceDetect = new FaceDetect();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceDetect);
            if (!jsonObject.isNull("result")) {
                faceDetect.mResult = parseResult(jsonObject.getJSONObject("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceDetect;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("face_num")) {
                result.mFaceNum = jsonObject.getInt("face_num");
            }

            if (result.mFaceNum > 0 &&!jsonObject.isNull("face_list")) {
                result.mFaceList = parseFaces(jsonObject.getJSONArray("face_list"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    private ArrayList<Face> parseFaces(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Face> faceList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Face face = new Face();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("face_token")) {
                    face.mFaceToken = jsonObject.getString("face_token");
                }
                if (!jsonObject.isNull("location")) {
                    face.mLocationF = parseLocationF(jsonObject.getJSONObject("location"));
                }
                if (!jsonObject.isNull("face_probability")) {
                    face.mFaceProbability = jsonObject.getInt("face_probability");
                }
                if (!jsonObject.isNull("angle")) {
                    face.mAngle = parseAngle(jsonObject.getJSONObject("angle"));
                }
                if (!jsonObject.isNull("age")) {
                    face.mAge = jsonObject.getInt("age");
                }
                if (!jsonObject.isNull("beauty")) {
                    face.mBeauty = jsonObject.getDouble("beauty");
                }
                if (!jsonObject.isNull("expression")) {
                    face.mExpression = parseFaceAttr(jsonObject.getJSONObject("expression"));
                }
                if (!jsonObject.isNull("face_shape")) {
                    face.mFaceShape = parseFaceAttr(jsonObject.getJSONObject("face_shape"));
                }
                if (!jsonObject.isNull("gender")) {
                    face.mGender = parseFaceAttr(jsonObject.getJSONObject("gender"));
                }
                if (!jsonObject.isNull("glasses")) {
                    face.mGlasses = parseFaceAttr(jsonObject.getJSONObject("glasses"));
                }
                if (!jsonObject.isNull("race")) {
                    face.mRace = parseFaceAttr(jsonObject.getJSONObject("race"));
                }
                if (!jsonObject.isNull("emotion")) {
                    face.mEmotion = parseFaceAttr(jsonObject.getJSONObject("emotion"));
                }
                if (!jsonObject.isNull("face_type")) {
                    face.mFaceType = parseFaceAttr(jsonObject.getJSONObject("face_type"));
                }
                if (!jsonObject.isNull("landmark")) {
                    face.mLandMarkList = parseLandMark(jsonObject.getJSONArray("landmark"));
                }
                if (!jsonObject.isNull("landmark72")) {
                    face.mLandMark72List = parseLandMark(jsonObject.getJSONArray("landmark72"));
                }
//                if (!jsonObject.isNull("landmark150")) {
//                    face.mLandMark150List = parseLandMark(jsonObject.getJSONArray("landmark150"));
//                }
                if (!jsonObject.isNull("quality")) {
                    face.mQuality = parseQuality(jsonObject.getJSONObject("quality"));
                }
                if (!jsonObject.isNull("eye_status")) {
                    face.mEyeStatus = parseEyeStatus(jsonObject.getJSONObject("eye_status"));
                }

                faceList.add(face);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return faceList;
    }

    private Angle parseAngle(JSONObject jsonObject) {
        Angle angle = new Angle();
        try {
            if (!jsonObject.isNull("yaw")) {
                angle.mYaw = jsonObject.getDouble("yaw");
            }
            if (!jsonObject.isNull("pitch")) {
                angle.mPitch = jsonObject.getDouble("pitch");
            }
            if (!jsonObject.isNull("roll")) {
                angle.mRoll = jsonObject.getDouble("roll");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return angle;
    }

    private FaceAttribute parseFaceAttr(JSONObject jsonObject) {
        FaceAttribute faceAttribute = new FaceAttribute();
        try {
            if (!jsonObject.isNull("type")) {
                faceAttribute.mType = jsonObject.getString("type");
            }
            if (!jsonObject.isNull("probability")) {
                faceAttribute.mProbability = jsonObject.getDouble("probability");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return faceAttribute;
    }

    private ArrayList<Point> parseLandMark(JSONArray jsonArray) {
        ArrayList<Point> landMarkList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Point pt = new Point();
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (!jsonObject.isNull("x")) {
                    pt.x = (int)jsonObject.getDouble("x");
                }
                if (!jsonObject.isNull("y")) {
                    pt.y = (int)jsonObject.getDouble("y");
                }
                landMarkList.add(pt);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return landMarkList;
    }

    private Quality parseQuality(JSONObject jsonObject) {
        Quality quality = new Quality();
        try {
            if (!jsonObject.isNull("occlusion")) {
                quality.mOcclusion = parseOcclusion(jsonObject.getJSONObject("occlusion"));
            }
            if (!jsonObject.isNull("blur")) {
                quality.mBlur = jsonObject.getInt("blur");
            }
            if (!jsonObject.isNull("illumination")) {
                quality.mIllumination = jsonObject.getInt("illumination");
            }
            if (!jsonObject.isNull("completeness")) {
                quality.mCompleteness = jsonObject.getInt("completeness");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return quality;
    }

    private Occlusion parseOcclusion(JSONObject jsonObject) {
        Occlusion occlusion = new Occlusion();
        try {
            if (!jsonObject.isNull("left_eye")) {
                occlusion.mLeftEye = jsonObject.getInt("left_eye");
            }
            if (!jsonObject.isNull("right_eye")) {
                occlusion.mRightEye = jsonObject.getInt("right_eye");
            }
            if (!jsonObject.isNull("nose")) {
                occlusion.mNose = jsonObject.getInt("nose");
            }
            if (!jsonObject.isNull("mouth")) {
                occlusion.mMouth = jsonObject.getInt("mouth");
            }
            if (!jsonObject.isNull("left_cheek")) {
                occlusion.mLeftCheek = jsonObject.getInt("left_cheek");
            }
            if (!jsonObject.isNull("right_cheek")) {
                occlusion.mRightCheek = jsonObject.getInt("right_cheek");
            }
            if (!jsonObject.isNull("chin_contour")) {
                occlusion.mChinContour = jsonObject.getInt("chin_contour");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return occlusion;
    }

    private EyStatus parseEyeStatus(JSONObject jsonObject) {
        EyStatus eyeStatus = new EyStatus();
        try {
            if (!jsonObject.isNull("left_eye")) {
                eyeStatus.mLeftEye = jsonObject.getDouble("left_eye");
            }
            if (!jsonObject.isNull("right_eye")) {
                eyeStatus.mRightEye = jsonObject.getDouble("right_eye");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eyeStatus;
    }
}
