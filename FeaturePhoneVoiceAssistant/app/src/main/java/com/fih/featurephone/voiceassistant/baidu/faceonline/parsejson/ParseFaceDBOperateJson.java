package com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

//https://ai.baidu.com/docs#/Face-ErrorCode-V3/top
public class ParseFaceDBOperateJson extends BaiduParseBaseJson {

    private static ParseFaceDBOperateJson sParseFaceDBOperateJson = null;

    public static ParseFaceDBOperateJson getInstance() {
        if (null == sParseFaceDBOperateJson) {
            sParseFaceDBOperateJson = new ParseFaceDBOperateJson();
        }
        return sParseFaceDBOperateJson;
    }

    public class FaceOperate extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        public String mFaceToken;
        LocationF mLocationF;
    }

    public FaceOperate parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceOperate faceOperate = new FaceOperate();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceOperate);
            if (!jsonObject.isNull("result")) {
                faceOperate.mResult = parseResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceOperate;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("face_token")) {
                result.mFaceToken = jsonObject.getString("face_token");
            }
            if (!jsonObject.isNull("location")) {
                result.mLocationF = parseLocationF(jsonObject.getJSONObject("location"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
