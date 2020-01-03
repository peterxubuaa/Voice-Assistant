package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseFaceCompareJson extends BaiduParseBaseJson {
    private static ParseFaceCompareJson sParseFaceCompareJson = null;

    public static ParseFaceCompareJson getInstance() {
        if (null == sParseFaceCompareJson) {
            sParseFaceCompareJson = new ParseFaceCompareJson();
        }
        return sParseFaceCompareJson;
    }

    public class FaceCompare extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        public Double mScore;//人脸相似度得分，推荐阈值80分
        ArrayList<String> mFaceList;
    }

    public FaceCompare parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceCompare faceCompare = new FaceCompare();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceCompare);
            if (!jsonObject.isNull("result")) {
                faceCompare.mResult = parseResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceCompare;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("score")) {
                result.mScore = jsonObject.getDouble("score");
            }

            if (!jsonObject.isNull("face_list")) {
                result.mFaceList = new ArrayList<>();
                JSONArray jsonArray = jsonObject.getJSONArray("face_list");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonSubObject = jsonArray.getJSONObject(i);
                    if (!jsonSubObject.isNull("face_token")) {
                        result.mFaceList.add(jsonSubObject.getString("face_token"));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }
}
