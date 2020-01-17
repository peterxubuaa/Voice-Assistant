package com.min.aiassistant.baidu.ocr.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseHandwritingJson extends BaiduParseBaseJson {

    private static ParseHandwritingJson sParseHandwritingJson = null;

    public static ParseHandwritingJson getInstance() {
        if (null == sParseHandwritingJson) {
            sParseHandwritingJson = new ParseHandwritingJson();
        }
        return sParseHandwritingJson;
    }

    public class Handwriting extends BaiduParseBaseResponse {
        int mWordsResultNum;
        public ArrayList<WordsResult> mWordsResultList;
    }

    public class WordsResult {
        public String mWords;
        Location mLocation;
    }

    public Handwriting parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Handwriting handwriting = new Handwriting();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, handwriting);
            if (!jsonObject.isNull("words_result_num")) {
                handwriting.mWordsResultNum = jsonObject.getInt("words_result_num");
            }
            if (handwriting.mWordsResultNum > 0 && !jsonObject.isNull("words_result")) {
                handwriting.mWordsResultList = parseWordsResult(jsonObject.getJSONArray("words_result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return handwriting;
    }

    private ArrayList<WordsResult> parseWordsResult(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<WordsResult> wordsResultList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                WordsResult wordsResult = new WordsResult();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("words")) {
                    wordsResult.mWords = jsonObject.getString("words");
                }
                if (!jsonObject.isNull("location")) {
                    wordsResult.mLocation = parseLocation(jsonObject.getJSONObject("location"));
                }

                wordsResultList.add(wordsResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wordsResultList;
    }
}
