package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseFaceQueryJson extends ParseFaceBaseJson {
//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top

    private static ParseFaceQueryJson sParseFaceQueryJson = null;

    public static ParseFaceQueryJson getInstance() {
        if (null == sParseFaceQueryJson) {
            sParseFaceQueryJson = new ParseFaceQueryJson();
        }
        return sParseFaceQueryJson;
    }

    //用于查询指定用户组中的用户列表
    public class FaceQueryUserList_Result {
        public ArrayList<String> mUserIDList;
    }

    public class FaceQueryUserList extends FaceBaseResponse{
        public FaceQueryUserList_Result mResult;
    }

    public FaceQueryUserList parseQueryUserList(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceQueryUserList queryUserList = new FaceQueryUserList();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, queryUserList);

            if (!jsonObject.isNull("result")) {
                queryUserList.mResult = parseUserListResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return queryUserList;
    }

    private FaceQueryUserList_Result parseUserListResult(JSONObject jsonObject) {
        FaceQueryUserList_Result result = new FaceQueryUserList_Result();
        try {
            if (!jsonObject.isNull("user_id_list")) {
                result.mUserIDList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("user_id_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.mUserIDList.add(jsonArray.getString(i));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    //获取人脸库中某个用户的信息(user_info信息和用户所属的组)
    public class FaceQueryUserInfo_Result {
        public ArrayList<FaceQueryUserInfo_User> mUserList;
    }

    public class FaceQueryUserInfo_User {
        public String mUserInfo;
        String mGroupID;
    }

    public class FaceQueryUserInfo extends FaceBaseResponse {
        public FaceQueryUserInfo_Result mResult;
    }

    public FaceQueryUserInfo parseQueryUserInfo(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceQueryUserInfo queryUserInfo = new FaceQueryUserInfo();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, queryUserInfo);
            if (!jsonObject.isNull("result")) {
                queryUserInfo.mResult = parseUserInfoResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return queryUserInfo;
    }

    private FaceQueryUserInfo_Result parseUserInfoResult(JSONObject jsonObject) {
        FaceQueryUserInfo_Result result = new FaceQueryUserInfo_Result();
        try {
            if (!jsonObject.isNull("user_list")) {
                result.mUserList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("user_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    if (null == subJsonObject) continue;

                    FaceQueryUserInfo_User user = new FaceQueryUserInfo_User();
                    if (!subJsonObject.isNull("user_info")) {
                        user.mUserInfo = subJsonObject.getString("user_info");
                    }
                    if (!subJsonObject.isNull("group_id")) {
                        user.mGroupID = subJsonObject.getString("group_id");
                    }
                    result.mUserList.add(user);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    //用于获取一个用户的全部人脸列表。
    public class FaceQueryFaceInfo_Result {
        public ArrayList<FaceQueryFaceInfo_Face> mFaceList;
    }

    public class FaceQueryFaceInfo_Face {
        public String mFaceToken;
        String mCreateTime;
    }

    public class FaceQueryFaceInfo extends FaceBaseResponse {
        public FaceQueryFaceInfo_Result mResult;
    }

    public FaceQueryFaceInfo parseQueryFaceInfo(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceQueryFaceInfo queryFaceInfo = new FaceQueryFaceInfo();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, queryFaceInfo);

            if (!jsonObject.isNull("result")) {
                queryFaceInfo.mResult = parseFaceInfoResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return queryFaceInfo;
    }

    private FaceQueryFaceInfo_Result parseFaceInfoResult(JSONObject jsonObject) {
        FaceQueryFaceInfo_Result result = new FaceQueryFaceInfo_Result();
        try {
            if (!jsonObject.isNull("face_list")) {
                result.mFaceList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("face_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subJsonObject = jsonArray.getJSONObject(i);
                    if (null == subJsonObject) continue;

                    FaceQueryFaceInfo_Face face = new FaceQueryFaceInfo_Face();
                    if (!subJsonObject.isNull("face_token")) {
                        face.mFaceToken = subJsonObject.getString("face_token");
                    }
                    if (!subJsonObject.isNull("ctime")) {
                        face.mCreateTime = subJsonObject.getString("ctime");
                    }
                    result.mFaceList.add(face);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
