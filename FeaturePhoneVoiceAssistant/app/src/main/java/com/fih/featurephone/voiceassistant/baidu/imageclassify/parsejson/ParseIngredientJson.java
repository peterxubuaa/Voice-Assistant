package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseIngredientJson extends BaseParseJson {

    public static class Ingredient {
        Long mLogID;
        int mResultNum;
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        public String mName;//图像中的食材名称
        public double mScore;//分类结果置信度（0--1.0）
    }

    public static Ingredient parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Ingredient ingredient = new Ingredient();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                ingredient.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result_num")) {
                ingredient.mResultNum = jsonObject.getInt("result_num");
            }

            if (ingredient.mResultNum > 0 && !jsonObject.isNull("result")) {
                ingredient.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ingredient;
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
                resultList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
