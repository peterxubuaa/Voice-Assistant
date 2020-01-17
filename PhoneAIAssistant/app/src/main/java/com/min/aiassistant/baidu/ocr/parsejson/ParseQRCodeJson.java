package com.min.aiassistant.baidu.ocr.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseQRCodeJson extends BaiduParseBaseJson {

    private static ParseQRCodeJson sParseQRCodeJson = null;

    public static ParseQRCodeJson getInstance() {
        if (null == sParseQRCodeJson) {
            sParseQRCodeJson = new ParseQRCodeJson();
        }
        return sParseQRCodeJson;
    }

    public class QRCode extends BaiduParseBaseResponse {
        int mCodesResultNum;
        public ArrayList<CodesResult> mCodesResultList;
    }

    public class CodesResult {
        public String mType;
        public String mText;
    }

    public QRCode parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        QRCode qrCode = new QRCode();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, qrCode);
            if (!jsonObject.isNull("codes_result_num")) {
                qrCode.mCodesResultNum = jsonObject.getInt("codes_result_num");
            }
            if (qrCode.mCodesResultNum > 0 && !jsonObject.isNull("codes_result")) {
                qrCode.mCodesResultList = parseCodesResult(jsonObject.getJSONArray("codes_result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return qrCode;
    }

    private ArrayList<CodesResult> parseCodesResult(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<CodesResult> codesResultsList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                CodesResult codesResult = new CodesResult();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("type")) {
                    codesResult.mType = jsonObject.getString("type");
                }
                if (!jsonObject.isNull("text")) {
                    codesResult.mText = jsonObject.getString("text");
                }

                codesResultsList.add(codesResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return codesResultsList;
    }
}
