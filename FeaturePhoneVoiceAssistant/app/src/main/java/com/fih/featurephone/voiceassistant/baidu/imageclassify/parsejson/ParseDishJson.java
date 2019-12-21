package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseDishJson  extends BaseParseJson {

    public static class Dish {
        Long mLogID;
        int mResultNum;
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        public String mName;//菜名，示例：鱼香肉丝
        public double mCalorie;//卡路里，每100g的卡路里含量
        public double mProbability;//识别结果中每一行的置信度值，0-1
        public BaiKeInfo mBaiKeInfo;//对应识别结果的百科词条名称
    }

    public static Dish parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Dish dish = new Dish();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                dish.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result_num")) {
                dish.mResultNum = jsonObject.getInt("result_num");
            }

            if (dish.mResultNum > 0 && !jsonObject.isNull("result")) {
                dish.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dish;
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
                if (!jsonObject.isNull("calorie")) {
                    result.mCalorie = jsonObject.getDouble("calorie");
                }
                if (!jsonObject.isNull("keyword")) {
                    result.mProbability = jsonObject.getDouble("probability");
                }
                if (!jsonObject.isNull("baike_info")) {
                    result.mBaiKeInfo = parseBaiKeInfo(jsonObject.getJSONObject("baike_info"));
                }
                resultList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
