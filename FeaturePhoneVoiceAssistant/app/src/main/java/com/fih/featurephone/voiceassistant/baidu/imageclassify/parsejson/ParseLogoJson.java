package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseLogoJson {

    public static class Logo {
        Long mLogID;
        int mResultNum;
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        Location mLocation;//位置信息（左起像素位置、上起像素位置、像素宽、像素高）
        public String mName;//识别的品牌名称
        public double mProbability;//分类结果置信度（0--1.0）
        int mType;//type=0为1千种高优商标识别结果;type=1为2万类logo库的结果；其它type为自定义logo库结果
    }

    public static class Location {
        int mLeft;
        int mTop;
        int mWidth;
        int mHeight;
    }

    public static Logo parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Logo log = new Logo();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                log.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result_num")) {
                log.mResultNum = jsonObject.getInt("result_num");
            }

            if (log.mResultNum > 0 && !jsonObject.isNull("result")) {
                log.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return log;
    }

    private static ArrayList<Result> parseResults(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Result> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Result result = new Result();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("location")) {
                    result.mLocation = parseLocation(jsonObject.getJSONObject("location"));
                }
                if (!jsonObject.isNull("name")) {
                    result.mName = jsonObject.getString("name");
                }
                if (!jsonObject.isNull("probability")) {
                    result.mProbability = jsonObject.getDouble("probability");
                }
                if (!jsonObject.isNull("type")) {
                    result.mType = jsonObject.getInt("type");
                }
                resultList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    private static Location parseLocation(JSONObject jsonObject) {
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
}
