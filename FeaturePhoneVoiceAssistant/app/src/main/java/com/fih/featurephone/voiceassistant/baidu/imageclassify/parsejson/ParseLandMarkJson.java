package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseLandMarkJson extends BaseParseJson {

    public static class LandMark {
        String mLogID;
        public Result mResult;//地标名称，无法识别则返回空字符串
    }

    public static class Result {
        public String mLandMark;
    }

    public static LandMark parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        LandMark landMark = new LandMark();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                landMark.mLogID = jsonObject.getString("log_id");
            }
            if (!jsonObject.isNull("result")) {
                landMark.mResult = parseResults(jsonObject.getJSONObject("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return landMark;
    }

    private static Result parseResults(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Result result = new Result();
        try {
            if (!jsonObject.isNull("landmark")) {
                result.mLandMark = jsonObject.getString("landmark");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
