package com.min.aiassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduBaseModel;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.activity.UserItem;
import com.min.aiassistant.baidu.faceonline.parsejson.ParseFaceQueryJson;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FaceDBQuery extends BaiduBaseModel {
    private final String FACE_LOCAL_IMAGE_DIR = FileUtils.getFaceImageDirectory().getAbsolutePath();

    public FaceDBQuery(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    //查找用户管理列表所需信息
    public void requestAllUserItem(String groupID, int start, int length) {
        if (null == mBaiduBaseListener) return;

        String userListResponse = requestUserListHostUrl(groupID, start, length);
        if (TextUtils.isEmpty(userListResponse)) {
            mBaiduBaseListener.onError("从服务器获取用户ID列表失败！");
            return;
        }

        ParseFaceQueryJson.FaceQueryUserList userList = ParseFaceQueryJson.getInstance().parseQueryUserList(userListResponse);
        if (null == userList || null == userList.mResult
                || 0 == userList.mResult.mUserIDList.size()) {
            mBaiduBaseListener.onError("查询用户ID列表失败！");
            return;
        }

        ArrayList<UserItem> userItemList = new ArrayList<>();
        for (String userID : userList.mResult.mUserIDList) {
            String userInfoResponse = requestUserInfoHostUrl(groupID, userID);
            ParseFaceQueryJson.FaceQueryUserInfo userInfo = ParseFaceQueryJson.getInstance().parseQueryUserInfo(userInfoResponse);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String faceInfoResponse = requestFaceInfoHostUrl(groupID, userID);
            ParseFaceQueryJson.FaceQueryFaceInfo faceInfo = ParseFaceQueryJson.getInstance().parseQueryFaceInfo(faceInfoResponse);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (null == userInfo || null == faceInfo) continue;

            UserItem userItem = new UserItem();
            userItem.setUserID(userID);
            if (null != userInfo.mResult && userInfo.mResult.mUserList.size() > 0) {
                userItem.setUserInfo(userInfo.mResult.mUserList.get(0).mUserInfo);
            }
            if (null != faceInfo.mResult && faceInfo.mResult.mFaceList.size() > 0) {
                userItem.setFaceToken(faceInfo.mResult.mFaceList.get(0).mFaceToken);
                userItem.setFaceLocalImagePath(FACE_LOCAL_IMAGE_DIR + File.separator + userItem.getFaceToken() + ".jpg");
            }
            userItemList.add(userItem);
        }

        mBaiduBaseListener.onFinalResult(userItemList, BaiduFaceOnlineAI.FACE_QUERY_ALL_USER_INFO_ACTION);
    }

    //用于查询指定用户组中的用户列表
/*    public void requestUserList(String groupID, int start, int length) {
        if (null == mFaceOnlineListener) return;

        if (TextUtils.isEmpty(groupID) || start < 0 || length < 0) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestUserListHostUrl(groupID, start, length);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求查询人脸组失败！");
            return;
        }

        ParseFaceQueryJson.FaceQueryUserList userList = ParseFaceQueryJson.getInstance().parseQueryUserList(response);
        if (null == userList) {
            mFaceOnlineListener.onError("查询人脸组失败！");
            return;
        }

        if (userList.mErrorCode != 0 || null == userList.mResult) {
            mFaceOnlineListener.onError("查询人脸组失败：" + userList.mErrorMsg);
            return;
        }

        mFaceOnlineListener.onFinalResult(userList.mResult.mUserIDList, BaiduFaceOnlineAI.FACE_QUERY_USER_LIST_ACTION);
    }*/

    private String requestUserListHostUrl(String groupID, int start, int length) {
        // 请求url
        String FACE_DB_QUERY_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/getusers";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", groupID);//用户组id，标识一组用户（由数字、字母、下划线组成），长度限制48B
            map.put("start", start);
            map.put("length", length);

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DB_QUERY_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取人脸库中某个用户的信息(user_info信息和用户所属的组)
 /*   public void requestUserInfo(String groupID, String userID) {
        if (null == mFaceOnlineListener) return;

        if (TextUtils.isEmpty(groupID) || TextUtils.isEmpty(userID)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestUserInfoHostUrl(groupID, userID);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求查询人脸信息失败！");
            return;
        }

        ParseFaceQueryJson.FaceQueryUserInfo userInfo = ParseFaceQueryJson.getInstance().parseQueryUserInfo(response);
        if (null == userInfo) {
            mFaceOnlineListener.onError("查询人脸信息失败！");
            return;
        }

        if (userInfo.mErrorCode != 0 || null == userInfo.mResult) {
            mFaceOnlineListener.onError("查询人脸信息失败：" + userInfo.mErrorMsg);
            return;
        }

        mFaceOnlineListener.onFinalResult(userInfo.mResult.mUserList, BaiduFaceOnlineAI.FACE_QUERY_USER_INFO_ACTION);
    }*/

    private String requestUserInfoHostUrl(String groupID, String userID) {
        // 请求url
        String FACE_DB_QUERY_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/get";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", groupID);//用户组id(由数字、字母、下划线组成，长度限制48B)，如传入“@ALL”则从所有组中查询用户信息。注：处于不同组，但uid相同的用户，我们认为是同一个用户。
            map.put("user_id", userID);//用户id（由数字、字母、下划线组成），长度限制48B

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DB_QUERY_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //用于获取一个用户的全部人脸列表。
 /*   public void requestFaceInfo(String groupID, String userID) {
        if (null == mFaceOnlineListener) return;

        if (TextUtils.isEmpty(groupID) || TextUtils.isEmpty(userID)) {
            mFaceOnlineListener.onError("申请参数不合法！");
            return;
        }

        String response = requestFaceInfoHostUrl(groupID, userID);
        if (TextUtils.isEmpty(response)) {
            mFaceOnlineListener.onError("向服务器请求查询人脸信息失败！");
            return;
        }

        ParseFaceQueryJson.FaceQueryFaceInfo faceInfo = ParseFaceQueryJson.getInstance().parseQueryFaceInfo(response);
        if (null == faceInfo) {
            mFaceOnlineListener.onError("查询人脸信息失败！");
            return;
        }

        if (faceInfo.mErrorCode != 0 || null == faceInfo.mResult) {
            mFaceOnlineListener.onError("查询人脸信息失败：" + faceInfo.mErrorMsg);
            return;
        }

        mFaceOnlineListener.onFinalResult(faceInfo.mResult.mFaceList, BaiduFaceOnlineAI.FACE_QUERY_FACE_INFO_ACTION);
    }*/

    private String requestFaceInfoHostUrl(String groupID, String userID) {
        // 请求url
        String FACE_DB_QUERY_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/getlist";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", groupID);//用户组id(由数字、字母、下划线组成，长度限制48B)，
            map.put("user_id", userID);//用户id（由数字、字母、下划线组成），长度限制48B

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DB_QUERY_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
