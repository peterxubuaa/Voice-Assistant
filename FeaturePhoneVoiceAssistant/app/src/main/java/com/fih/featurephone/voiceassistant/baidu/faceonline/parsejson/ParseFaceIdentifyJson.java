package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top
public class ParseFaceIdentifyJson extends BaiduParseBaseJson {

    private static ParseFaceIdentifyJson sParseFaceIdentifyJson = null;

    public static ParseFaceIdentifyJson getInstance() {
        if (null == sParseFaceIdentifyJson) {
            sParseFaceIdentifyJson = new ParseFaceIdentifyJson();
        }
        return sParseFaceIdentifyJson;
    }

    public class FaceIdentify extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        public int mFace_Num;
        public ArrayList<Face> mFaceList;
    }

    public class Face {
        String mFaceToken;
        public LocationF mLocationF;
        public ArrayList<User> mUserList;
    }

    public class User {
        String mGroupID;
        String mUserID;
        public String mUserInfo;
        public Double mScore;
    }

    public FaceIdentify parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceIdentify faceIdentify = new FaceIdentify();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceIdentify);
            if (!jsonObject.isNull("result")) {
                faceIdentify.mResult = parseResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceIdentify;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("face_num")) {
                result.mFace_Num = jsonObject.getInt("face_num");
            }

            if (result.mFace_Num > 0) { //M : N
                if (!jsonObject.isNull("face_list")) {
                    result.mFaceList = parseFaceList(jsonObject.getJSONArray("face_list"));
                }
            } else { // 1 : N
                result.mFaceList = new ArrayList<>();
                Face face = new Face();
                if (!jsonObject.isNull("face_token")) {
                    face.mFaceToken = jsonObject.getString("face_token");
                }
                if (!jsonObject.isNull("user_list")) {
                    face.mUserList = parseUserList(jsonObject.getJSONArray("user_list"));
                }
                result.mFaceList.add(face);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Face> parseFaceList(JSONArray jsonArray) {
        ArrayList<Face> faceList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Face face = new Face();
                if (!jsonObject.isNull("face_token")) {
                    face.mFaceToken = jsonObject.getString("face_token");
                }
                if (!jsonObject.isNull("location")) {
                    face.mLocationF = parseLocationF(jsonObject.getJSONObject("location"));
                }
                if (!jsonObject.isNull("user_list")) {
                    face.mUserList = parseUserList(jsonObject.getJSONArray("user_list"));
                }

                faceList.add(face);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return faceList;
    }

    private ArrayList<User> parseUserList(JSONArray jsonArray) {
        ArrayList<User> userList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User user = new User();
                if (!jsonObject.isNull("group_id")) {
                    user.mGroupID = jsonObject.getString("group_id");
                }
                if (!jsonObject.isNull("user_id")) {
                    user.mUserID = jsonObject.getString("user_id");
                }
                if (!jsonObject.isNull("user_info")) {
                    user.mUserInfo = jsonObject.getString("user_info");
                }
                if (!jsonObject.isNull("score")) {
                    user.mScore = jsonObject.getDouble("score");
                }

                userList.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userList;
    }
}
