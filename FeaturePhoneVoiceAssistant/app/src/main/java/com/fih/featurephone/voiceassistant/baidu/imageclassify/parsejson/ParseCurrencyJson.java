package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseCurrencyJson {

    public static class Currency {
        Long mLogID;
        public Result mResult;
    }

    public static class Result {
        public String mCurrencyName;//货币名称，无法识别返回空，示例：新加坡元
        public int mHasDetail;//判断是否返回详细信息（除货币名称之外的其他字段），含有返回1，不含有返回0
        public String mCurrencyCode;//货币代码，hasdetail = 0时，表示无法识别，该字段不返回，示例：SGD
        public String mCurrencyDenomination;//货币面值，hasdetail = 0时，表示无法识别，该字段不返回，示例：50元
        public String mYear;//货币年份，hasdetail = 0时，表示无法识别，该字段不返回，示例：2004年
    }

    public static Currency parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Currency currency = new Currency();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                currency.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result")) {
                currency.mResult = parseResults(jsonObject.getJSONObject("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return currency;
    }

    private static Result parseResults(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Result result = new Result();
        try {
            if (!jsonObject.isNull("currencyName")) {
                result.mCurrencyName = jsonObject.getString("currencyName");
            }
            if (!jsonObject.isNull("hasdetail")) {
                result.mHasDetail = jsonObject.getInt("hasdetail");
            }

            if (1 == result.mHasDetail) {
                if (!jsonObject.isNull("currencyCode")) {
                    result.mCurrencyCode = jsonObject.getString("currencyCode");
                }
                if (!jsonObject.isNull("currencyDenomination")) {
                    result.mCurrencyDenomination = jsonObject.getString("currencyDenomination");
                }
                if (!jsonObject.isNull("year")) {
                    result.mYear = jsonObject.getString("year");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
