package com.fih.featurephone.voiceassistant.baidu;

import org.json.JSONException;
import org.json.JSONObject;

public class BaiduParseBaseJson {

    public class BaiduParseBaseResponse {
        Long mLogID;
        public int mErrorCode;
        public String mErrorMsg;
    }

    public void baseParse(JSONObject jsonObject, BaiduParseBaseResponse baiduParseBaseResponse) {
        try {
            if (!jsonObject.isNull("log_id")) {
                baiduParseBaseResponse.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("error_code")) {
                baiduParseBaseResponse.mErrorCode = jsonObject.getInt("error_code");
            }
            if (!jsonObject.isNull("error_msg")) {
                baiduParseBaseResponse.mErrorMsg = jsonObject.getString("error_msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class BaiKeInfo {
        String mBaiKeUrl;//对应识别结果百度百科页面链接
        String mImageUrl;//对应识别结果百科图片链接
        public String mDescription;//对应识别结果百科内容描述
    }

    protected BaiKeInfo parseBaiKeInfo(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        BaiKeInfo baiKeInfo = new BaiKeInfo();
        try {
            if (!jsonObject.isNull("baike_url")) {
                baiKeInfo.mBaiKeUrl = jsonObject.getString("baike_url");
            }
            if (!jsonObject.isNull("image_url")) {
                baiKeInfo.mImageUrl = jsonObject.getString("image_url");
            }

            if (!jsonObject.isNull("description")) {
                baiKeInfo.mDescription = jsonObject.getString("description");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return baiKeInfo;
    }

    protected class Location {
        int mLeft;
        int mTop;
        int mWidth;
        int mHeight;
    }

    protected Location parseLocation(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        Location location = new Location();
        try {
            if (!jsonObject.isNull("left")) {
                location.mLeft = jsonObject.getInt("left");
            }
            if (!jsonObject.isNull("top")) {
                location.mTop = jsonObject.getInt("top");
            }

            if (!jsonObject.isNull("width")) {
                location.mWidth = jsonObject.getInt("width");
            }
            if (!jsonObject.isNull("height")) {
                location.mHeight = jsonObject.getInt("height");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return location;
    }

    public class LocationF {
        public double mLeft;
        public double mTop;
        public int mWidth;
        public int mHeight;
        public int mRotation;
    }

    protected LocationF parseLocationF(JSONObject jsonObject) {
        LocationF locationf = new LocationF();
        try {
            if (!jsonObject.isNull("left")) {
                locationf.mLeft = jsonObject.getDouble("left");
            }
            if (!jsonObject.isNull("top")) {
                locationf.mTop = jsonObject.getDouble("top");
            }
            if (!jsonObject.isNull("width")) {
                locationf.mWidth = jsonObject.getInt("width");
            }
            if (!jsonObject.isNull("height")) {
                locationf.mHeight = jsonObject.getInt("height");
            }
            if (!jsonObject.isNull("rotation")) {
                locationf.mRotation = jsonObject.getInt("rotation");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationf;
    }
}
