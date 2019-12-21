package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseCarJson  extends BaseParseJson {

    public static class Car {
        long mLogID;
        String mColorResult;//颜色
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        public String mName;//车型名称，示例：宝马x6
        public double mScore;//置信度，示例：0.5321
        String mYear;//年份
        public BaiKeInfo mBaiKeInfo;
        LocationResult mLocationResult;//车在图片中的位置信息
    }

    public static class LocationResult {
        int mLeft;
        int mTop;
        int mWidth;
        int mHeight;
    }

    public static Car parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Car car = new Car();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                car.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("color_result")) {
                car.mColorResult = jsonObject.getString("color_result");
            }

            if (!jsonObject.isNull("result")) {
                car.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return car;
    }

    private static ArrayList<Result> parseResults(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Result> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Result result = new Result();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("name")) {
                    result.mName = jsonObject.getString("name");
                }
                if (!jsonObject.isNull("score")) {
                    result.mScore = jsonObject.getDouble("score");
                }
                if (!jsonObject.isNull("year")) {
                    result.mYear = jsonObject.getString("year");
                }
                if (!jsonObject.isNull("baike_info")) {
                    result.mBaiKeInfo = parseBaiKeInfo(jsonObject.getJSONObject("baike_info"));
                }
                if (!jsonObject.isNull("location_result")) {
                    result.mLocationResult = parseLocationInfo(jsonObject.getJSONObject("location_result"));
                }
                resultList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private static LocationResult parseLocationInfo(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        LocationResult locationResult = new LocationResult();
        try {
            if (!jsonObject.isNull("left")) {
                locationResult.mLeft = jsonObject.getInt("left");
            }
            if (!jsonObject.isNull("top")) {
                locationResult.mTop = jsonObject.getInt("top");
            }
            if (!jsonObject.isNull("width")) {
                locationResult.mWidth = jsonObject.getInt("width");
            }
            if (!jsonObject.isNull("height")) {
                locationResult.mHeight = jsonObject.getInt("height");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return locationResult;
    }
}
