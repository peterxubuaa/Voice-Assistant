package com.fih.featurephone.voiceassistant.baidu.faceoffline.model;

import android.util.Base64;

/**
 * 用户实体类
 */
public class User {
    private int mDBID;
    private String mUserID = null;
    private String mUserName = null;
    private String mUserInfo = null;
    private String mFaceImagePath = null;
    private String mFaceToken = null;
    private byte[] mFaceFeature = null;
    private long mCreateTime = -1;
    private long mUpdateTime = -1;

    private boolean mChecked = false;

    public User() {
    }

    public int getDBID() {
        return mDBID;
    }

    public void setDBID(int id) {
        mDBID = id;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }

    public long getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(long updateTime) {
        mUpdateTime = updateTime;
    }

    public String getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(String userInfo) {
        mUserInfo = userInfo;
    }

    public String getFaceToken() {
        if (mFaceFeature != null) {
            byte[] base = Base64.encode(mFaceFeature, Base64.NO_WRAP);
            mFaceToken = new String(base);
        }
        return mFaceToken;
    }

    public void setFaceToken(String faceToken) {
        mFaceToken = faceToken;
    }

    public String getFaceImagePath() {
        return mFaceImagePath;
    }

    public void setFaceImagePath(String faceImagePath) {
        mFaceImagePath = faceImagePath;
    }

    public byte[] getFaceFeature() {
        return mFaceFeature;
    }

    public void setFaceFeature(byte[] faceFeature) {
        mFaceFeature = faceFeature;
    }

    public boolean getChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
