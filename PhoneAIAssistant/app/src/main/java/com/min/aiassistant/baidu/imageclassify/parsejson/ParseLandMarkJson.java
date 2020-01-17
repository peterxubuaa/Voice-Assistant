package com.min.aiassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseLandMarkJson extends BaiduParseBaseJson {

    private static ParseLandMarkJson sParseLandMarkJson = null;

    public static ParseLandMarkJson getInstance() {
        if (null == sParseLandMarkJson) {
            sParseLandMarkJson = new ParseLandMarkJson();
        }
        return sParseLandMarkJson;
    }

    public class LandMark extends BaiduParseBaseResponse {
        public Result mResult;//地标名称，无法识别则返回空字符串
    }

    public class Result {
        public String mLandMark;
    }

    public LandMark parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        LandMark landMark = new LandMark();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, landMark);
            if (!jsonObject.isNull("result")) {
                landMark.mResult = parseResults(jsonObject.getJSONObject("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return landMark;
    }

    private Result parseResults(JSONObject jsonObject) {
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
