package com.min.aiassistant.baidu.nlp.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseNewsSummaryJson extends BaiduParseBaseJson {
    private static ParseNewsSummaryJson sParseNewsSummaryJson = null;

    public static ParseNewsSummaryJson getInstance() {
        if (null == sParseNewsSummaryJson) {
            sParseNewsSummaryJson = new ParseNewsSummaryJson();
        }
        return sParseNewsSummaryJson;
    }

    public class NewsSummary extends BaiduParseBaseResponse {
        public String mSummary;
    }

    public NewsSummary parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        NewsSummary newsSummary = new NewsSummary();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, newsSummary);
            if (!jsonObject.isNull("summary")) {
                newsSummary.mSummary = jsonObject.getString("summary");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return newsSummary;
    }
}
