package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import org.json.JSONException;
import org.json.JSONObject;

class ParseFaceBaseJson {
    class FaceBaseResponse {
        Long mLogID;
        public int mErrorCode;
        public String mErrorMsg;
    }

    class Location {
        double mLeft;
        double mTop;
        int mWidth;
        int mHeight;
        int mRotation;
    }

    void baseParse(JSONObject jsonObject, FaceBaseResponse baseFaceResponse) {
        try {
            if (!jsonObject.isNull("log_id")) {
                baseFaceResponse.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("error_code")) {
                baseFaceResponse.mErrorCode = jsonObject.getInt("error_code");
            }
            if (!jsonObject.isNull("error_msg")) {
                baseFaceResponse.mErrorMsg = jsonObject.getString("error_msg");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Location parseLocation(JSONObject jsonObject) {
        Location location = new Location();
        try {
            if (!jsonObject.isNull("left")) {
                location.mLeft = jsonObject.getDouble("left");
            }
            if (!jsonObject.isNull("top")) {
                location.mTop = jsonObject.getDouble("top");
            }
            if (!jsonObject.isNull("width")) {
                location.mWidth = jsonObject.getInt("width");
            }
            if (!jsonObject.isNull("height")) {
                location.mHeight = jsonObject.getInt("height");
            }
            if (!jsonObject.isNull("rotation")) {
                location.mRotation = jsonObject.getInt("rotation");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return location;
    }
}
