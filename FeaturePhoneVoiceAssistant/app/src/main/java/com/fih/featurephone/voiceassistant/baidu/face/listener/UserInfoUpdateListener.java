package com.fih.featurephone.voiceassistant.baidu.face.listener;

import android.graphics.Bitmap;

import com.fih.featurephone.voiceassistant.baidu.face.model.User;

import java.util.List;

public class UserInfoUpdateListener {
    // 用户列表查询成功
    public void userListQuerySuccess(List<User> listUserInfo, boolean bAll) {}
    // 用户列表查询失败
    public void userListQueryFailure(String message) {}

    public void userListDeleteSuccess(List<User> deletedUserList) {}
    // 用户列表删除失败
    public void userListDeleteFailure(List<User> deletedUserList, String message) {}

    // 更新用户信息(除图片)成功
    public void userUpdateSuccess(User user) {}
    // 更新用户信息(除图片)失败
    public void userUpdateFailure(User user, String message) {}
    // 更新图片成功
    public void updateImageSuccess(User user, Bitmap bitmap) {}
    // 更新图片失败
    public void updateImageFailure(User user, String message) {}
}
