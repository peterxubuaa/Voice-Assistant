package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseFaceIdentifyJson extends ParseFaceBaseJson {
//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top

    private static ParseFaceIdentifyJson sParseFaceIdentifyJson = null;

    public static ParseFaceIdentifyJson getInstance() {
        if (null == sParseFaceIdentifyJson) {
            sParseFaceIdentifyJson = new ParseFaceIdentifyJson();
        }
        return sParseFaceIdentifyJson;
    }

    public class FaceIdentify extends FaceBaseResponse {
        public Result mResult;
    }

    public class Result {
        String mFaceToken;
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
            if (!jsonObject.isNull("face_token")) {
                result.mFaceToken = jsonObject.getString("face_token");
            }
            if (!jsonObject.isNull("user_list")) {
                result.mUserList = parseUserList(jsonObject.getJSONArray("user_list"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
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
