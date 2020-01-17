package com.min.aiassistant.baidu.faceonline.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseFaceMergeJson  extends BaiduParseBaseJson {
    private static ParseFaceMergeJson sParseFaceMergeJson = null;

    public static ParseFaceMergeJson getInstance() {
        if (null == sParseFaceMergeJson) {
            sParseFaceMergeJson = new ParseFaceMergeJson();
        }
        return sParseFaceMergeJson;
    }

    public class FaceMerge extends BaiduParseBaseResponse {
        public Result mResult;
    }

    public class Result {
        public String mMergeImage;//融合图的BASE64值
    }

    public FaceMerge parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        FaceMerge faceMerge = new FaceMerge();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, faceMerge);
            if (!jsonObject.isNull("result")) {
                faceMerge.mResult = parseResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceMerge;
    }

    private Result parseResult(JSONObject jsonObject) {
        Result result = new Result();
        try {
            if (!jsonObject.isNull("merge_image")) {
                result.mMergeImage = jsonObject.getString("merge_image");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

}
