package com.min.aiassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseAdvancedGeneralJson extends BaiduParseBaseJson {

    private static ParseAdvancedGeneralJson sParseAdvancedGeneralJson = null;

    public static ParseAdvancedGeneralJson getInstance() {
        if (null == sParseAdvancedGeneralJson) {
            sParseAdvancedGeneralJson = new ParseAdvancedGeneralJson();
        }
        return sParseAdvancedGeneralJson;
    }

    public class AdvancedGeneral extends BaiduParseBaseResponse{
        int mResultNum;
        public ArrayList<Result> mResultList;
    }

    public static class Result {
        public double mScore;//置信度，0-1
        String mRoot;//识别结果的上层标签，有部分钱币、动漫、烟酒等tag无上层标签
        public String mKeyword;//图片中的物体或场景名称
        public BaiKeInfo mBaiKeInfo;//对应识别结果的百科词条名称
    }

    public AdvancedGeneral parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        AdvancedGeneral advancedGeneral = new AdvancedGeneral();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, advancedGeneral);
            if (!jsonObject.isNull("result_num")) {
                advancedGeneral.mResultNum = jsonObject.getInt("result_num");
            }

            if (advancedGeneral.mResultNum > 0 && !jsonObject.isNull("result")) {
                advancedGeneral.mResultList = parseResults(jsonObject.getJSONArray("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return advancedGeneral;
    }

    private ArrayList<Result> parseResults(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Result> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Result result = new Result();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("score")) {
                    result.mScore = jsonObject.getDouble("score");
                }
                if (!jsonObject.isNull("root")) {
                    result.mRoot = jsonObject.getString("root");
                }
                if (!jsonObject.isNull("keyword")) {
                    result.mKeyword = jsonObject.getString("keyword");
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
