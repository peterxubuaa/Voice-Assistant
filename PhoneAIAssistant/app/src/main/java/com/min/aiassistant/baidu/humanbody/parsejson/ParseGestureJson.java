package com.min.aiassistant.baidu.humanbody.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseGestureJson extends BaiduParseBaseJson {

    private static ParseGestureJson sParseGestureJson = null;

    public static ParseGestureJson getInstance() {
        if (null == sParseGestureJson) {
            sParseGestureJson = new ParseGestureJson();
        }
        return sParseGestureJson;
    }

    public class Gesture extends BaiduParseBaseResponse {
        int mResultNum;
        public ArrayList<Result> mResultList;//检测到的目标，手势、人脸
    }

    public class Result {
        public String mClassName;//目标所属类别，24种手势、other、face
        public int mTop;//目标框上坐标
        public int mLeft;//目标框最左坐标
        public int mWidth;//目标框的宽
        public int mHeight;//目标框的高
        public double mProbability;//目标属于该类别的概率
    }

    public Gesture parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Gesture gesture = new Gesture();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, gesture);
            if (!jsonObject.isNull("result_num")) {
                gesture.mResultNum = jsonObject.getInt("result_num");
            }
            if (!jsonObject.isNull("result")) {
                gesture.mResultList = parseResult(jsonObject.getJSONArray("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return gesture;
    }

    private ArrayList<Result> parseResult(JSONArray jsonArray) {
        ArrayList<Result> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Result result = new Result();
                if (!jsonObject.isNull("classname")) {
                    result.mClassName = jsonObject.getString("classname");
                }
                if (!jsonObject.isNull("probability")) {
                    result.mProbability = jsonObject.getDouble("probability");
                }
                if (!jsonObject.isNull("top")) {
                    result.mTop = jsonObject.getInt("top");
                }
                if (!jsonObject.isNull("left")) {
                    result.mLeft = jsonObject.getInt("left");
                }
                if (!jsonObject.isNull("width")) {
                    result.mWidth = jsonObject.getInt("width");
                }
                if (!jsonObject.isNull("height")) {
                    result.mHeight = jsonObject.getInt("height");
                }

                resultList.add(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }
}
