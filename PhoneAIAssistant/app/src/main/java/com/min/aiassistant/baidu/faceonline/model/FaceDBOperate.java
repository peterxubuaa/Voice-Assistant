package com.min.aiassistant.baidu.faceonline.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduBaseModel;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.activity.UserItem;
import com.min.aiassistant.baidu.faceonline.parsejson.ParseFaceDBOperateJson;
import com.min.aiassistant.utils.BitmapUtils;
import com.min.aiassistant.utils.CnToSpell;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.GlobalValue;
import com.min.aiassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FaceDBOperate extends BaiduBaseModel {

    public FaceDBOperate(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    //注册人脸信息
    public void requestFaceRegister(String imageFilePath, String userInfo) {
        if (null == mBaiduBaseListener) return;

        if (!FileUtils.isFileExist(imageFilePath) || TextUtils.isEmpty(userInfo)) {
            mBaiduBaseListener.onError("申请参数不合法！");
            return;
        }

        String userID = CnToSpell.getInstance().getSpelling(userInfo);
        String response = requestFaceRegisterHostUrl(imageFilePath, userInfo, userID);
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求注册人脸失败！");
            return;
        }

        ParseFaceDBOperateJson.FaceOperate faceRegister = ParseFaceDBOperateJson.getInstance().parse(response);
        if (null == faceRegister) {
            mBaiduBaseListener.onError("注册人脸失败！");
            return;
        }

        if (faceRegister.mErrorCode != 0 || null == faceRegister.mResult) {
            mBaiduBaseListener.onError("人脸注册失败信息：" + faceRegister.mErrorMsg);
            return;
        }

        //保存图像缩略图到本地
        String faceDirPath = FileUtils.getFaceImageDirectory().getAbsolutePath();
        String saveThumbnailJpeg = faceDirPath + File.separator + faceRegister.mResult.mFaceToken + ".jpg";
        BitmapUtils.resizeJpegFile(imageFilePath, 300, 300, saveThumbnailJpeg);

        UserItem userItem = new UserItem();
        userItem.setUserID(userID);
        userItem.setUserInfo(userInfo);
        userItem.setFaceToken(faceRegister.mResult.mFaceToken);
        userItem.setFaceLocalImagePath(saveThumbnailJpeg);
        mBaiduBaseListener.onFinalResult(userItem, BaiduFaceOnlineAI.FACE_REGISTER_ACTION);
    }

    private String requestFaceRegisterHostUrl(String imageFilePath, String userInfo, String userID) {
        // 请求url
        String FACE_DB_OPERATE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
        try {
            Map<String, Object> map = new HashMap<>();
            byte[] buf = FileUtils.readImageFile(imageFilePath);
            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
            map.put("image", encodeString);
            map.put("image_type", "BASE64");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。
            map.put("user_info", userInfo);//用户资料，长度限制256B 默认空

            map.put("group_id", GlobalValue.FACE_DEFAULT_GROUP_ID);//用户组id，标识一组用户（由数字、字母、下划线组成），长度限制48B
            map.put("user_id", userID);//用户id（由数字、字母、下划线组成），长度限制128B
            map.put("liveness_control", "NONE");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
            map.put("quality_control", "NORMAL");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
            map.put("action_type", "APPEND");//操作方式: APPEND: 当user_id在库中已经存在时，对此user_id重复注册时，新注册的图片默认会追加到该user_id下; REPLACE : 当对此user_id重复注册时,则会用新图替换库中该user_id下所有图片;默认使用APPEND

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DB_OPERATE_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //更新人脸信息
    public void requestFaceUpdate(UserItem userItem, String userInfo, String imageFilePath) {
        if (null == mBaiduBaseListener || null == userItem) return;

        String response = requestFaceUpdateHostUrl(userItem.getUserID(), userItem.getFaceToken(),
                                    userInfo, imageFilePath);
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求更新人脸信息失败！");
            return;
        }

        ParseFaceDBOperateJson.FaceOperate faceUpdate = ParseFaceDBOperateJson.getInstance().parse(response);
        if (null == faceUpdate) {
            mBaiduBaseListener.onError("更新人脸信息失败！");
            return;
        }

        if (faceUpdate.mErrorCode != 0 || null == faceUpdate.mResult) {
            mBaiduBaseListener.onError("更新人脸信息失败：" + faceUpdate.mErrorMsg);
            return;
        }

        userItem.setUserInfo(userInfo);
        if (!userItem.getFaceToken().equals(faceUpdate.mResult.mFaceToken)) { //更新了人脸图
            FileUtils.deleteFile(userItem.getFaceLocalImagePath());
            //保存图像缩略图到本地
            String faceDirPath = FileUtils.getFaceImageDirectory().getAbsolutePath();
            String saveThumbnailJpeg = faceDirPath + File.separator + faceUpdate.mResult.mFaceToken + ".jpg";
            BitmapUtils.resizeJpegFile(imageFilePath, 300, 300, saveThumbnailJpeg);
            userItem.setFaceLocalImagePath(saveThumbnailJpeg);
            userItem.setFaceToken(faceUpdate.mResult.mFaceToken);
        }

        mBaiduBaseListener.onFinalResult(userItem, BaiduFaceOnlineAI.FACE_UPDATE_ACTION);
    }

    private String requestFaceUpdateHostUrl(String userID, String faceToken, String userInfo, String imageFilePath) {
        // 请求url
        String FACE_DB_OPERATE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/update";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", GlobalValue.FACE_DEFAULT_GROUP_ID);//用户组id(由数字、字母、下划线组成，长度限制48B)，
            map.put("user_id", userID);//用户id（由数字、字母、下划线组成），长度限制48B
            map.put("user_info", userInfo);//用户资料，长度限制48B 默认空

            if (TextUtils.isEmpty(imageFilePath)) {
                map.put("image", faceToken);
                map.put("image_type", "FACE_TOKEN");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。
            } else if (FileUtils.isFileExist(imageFilePath)) {
                byte[] buf = FileUtils.readImageFile(imageFilePath);
                String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
                map.put("image", encodeString);
                map.put("image_type", "BASE64");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。
            }

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DB_OPERATE_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //删除人脸信息
    public void requestFaceDelete(UserItem userItem) {
        if (null == mBaiduBaseListener) return;

        if (null == userItem || TextUtils.isEmpty(userItem.getUserID())
                || TextUtils.isEmpty(userItem.getFaceToken())) {
            mBaiduBaseListener.onError("申请参数不合法！");
            return;
        }

        String response = requestFaceDeleteHostUrl(userItem.getUserID(), userItem.getFaceToken());
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求删除人脸信息失败！");
            return;
        }

        ParseFaceDBOperateJson.FaceOperate faceDelete = ParseFaceDBOperateJson.getInstance().parse(response);
        if (null == faceDelete) {
            mBaiduBaseListener.onError("删除人脸失败！");
            return;
        }
        if (faceDelete.mErrorCode != 0) {
            mBaiduBaseListener.onError("删除人脸信息失败：" + faceDelete.mErrorMsg);
            return;
        }

        //删除本地缓存图片文件
        FileUtils.deleteFile(userItem.getFaceLocalImagePath());

        mBaiduBaseListener.onFinalResult(userItem, BaiduFaceOnlineAI.FACE_DELETE_ACTION);
    }

    public void requestFaceListDelete(ArrayList<UserItem> userItemList) {
        if (null == mBaiduBaseListener) return;

        ArrayList<UserItem> deletedUserItemList = new ArrayList<>();
        for (UserItem userItem : userItemList) {
            String response = requestFaceDeleteHostUrl(userItem.getUserID(), userItem.getFaceToken());
            ParseFaceDBOperateJson.FaceOperate faceDelete = ParseFaceDBOperateJson.getInstance().parse(response);
            if (null == faceDelete || faceDelete.mErrorCode != 0) continue;

            //删除本地缓存图片文件
            FileUtils.deleteFile(userItem.getFaceLocalImagePath());
            deletedUserItemList.add(userItem);
        }
        mBaiduBaseListener.onFinalResult(deletedUserItemList, BaiduFaceOnlineAI.FACE_DELETE_LIST_ACTION);
    }

    private String requestFaceDeleteHostUrl(String userID, String faceToken) {
        // 请求url
        String FACE_DELETE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/delete";
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("group_id", GlobalValue.FACE_DEFAULT_GROUP_ID);//用户组id(由数字、字母、下划线组成，长度限制48B)，
            map.put("user_id", userID);//用户id（由数字、字母、下划线组成），长度限制48B
            map.put("face_token", faceToken);//用户资料，长度限制48B 默认空
            map.put("image_type", "FACE_TOKEN");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。

            String jsonParam = new JSONObject(map).toString();

            return HttpUtil.post(FACE_DELETE_URL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
