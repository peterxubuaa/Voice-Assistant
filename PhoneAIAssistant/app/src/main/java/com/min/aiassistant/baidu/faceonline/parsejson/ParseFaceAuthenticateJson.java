package com.min.aiassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseFaceAuthenticateJson extends BaiduParseBaseJson {
//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top

    private static ParseFaceAuthenticateJson sParseFaceAuthenticateJson = null;

    public static ParseFaceAuthenticateJson getInstance() {
        if (null == sParseFaceAuthenticateJson) {
            sParseFaceAuthenticateJson = new ParseFaceAuthenticateJson();
        }
        return sParseFaceAuthenticateJson;
    }

    public class FaceAuthenticate extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        public double mScore;//与公安小图相似度可能性，用于验证生活照与公安小图是否为同一人，有正常分数时为[0~100]，推荐阈值80，超过即判断为同一人
    }

    public FaceAuthenticate parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceAuthenticate faceAuthenticate = new FaceAuthenticate();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceAuthenticate);
            if (!jsonObject.isNull("result")) {
                faceAuthenticate.mResult = parseResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceAuthenticate;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("score")) {
                result.mScore = jsonObject.getDouble("score");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
