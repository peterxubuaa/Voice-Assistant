package com.fih.featurephone.voiceassistant.baidu.faceonline.activity;

public class UserItem {
    private String mFaceToken;
    private String mUserInfo;
    private String mUserID;
    private boolean mChecked;
    private String mFaceLocalImagePath;

    public String getFaceLocalImagePath() {
        return mFaceLocalImagePath;
    }

    public void setFaceLocalImagePath(String faceLocalImagePath) {
        mFaceLocalImagePath = faceLocalImagePath;
    }

    public String getFaceToken() {
        return mFaceToken;
    }

    public void setFaceToken(String faceToken) {
        mFaceToken = faceToken;
    }

    public String getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(String userInfo) {
        mUserInfo = userInfo;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    boolean isChecked() {
        return mChecked;
    }

    void setChecked(boolean checked) {
        mChecked = checked;
    }
}
