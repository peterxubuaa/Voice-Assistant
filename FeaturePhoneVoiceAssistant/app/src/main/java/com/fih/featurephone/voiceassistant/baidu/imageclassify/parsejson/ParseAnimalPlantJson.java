package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseAnimalPlantJson extends BaseParseJson {

    public static class AnimalPlant {
        Long mLogID;
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        public String mName;//动物名称，示例：蒙古马
        public double mScore;//分类结果置信度（0--1.0）
        public BaiKeInfo mBaiKeInfo;//对应识别结果的百科词条名称
    }

    public static AnimalPlant parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        AnimalPlant animalPlant = new AnimalPlant();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                animalPlant.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result")) {
                animalPlant.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return animalPlant;
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
