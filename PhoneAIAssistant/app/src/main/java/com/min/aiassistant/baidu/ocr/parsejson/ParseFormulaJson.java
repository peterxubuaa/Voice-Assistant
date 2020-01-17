package com.min.aiassistant.baidu.ocr.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseFormulaJson extends BaiduParseBaseJson {

    private static ParseFormulaJson sParseFormulaJson = null;

    public static ParseFormulaJson getInstance() {
        if (null == sParseFormulaJson) {
            sParseFormulaJson = new ParseFormulaJson();
        }
        return sParseFormulaJson;
    }

    public class Formula extends BaiduParseBaseResponse {
        int mDirection; //图像方向，当detect_direction=true时存在。- -1:未定义，- 0:正向，- 1: 逆时针90度，- 2:逆时针180度，- 3:逆时针270度
        int mWordsResultNum;
        public ArrayList<WordsFormulaResult> mWordsResultList;
        int mFormulaResultNum;
        public ArrayList<WordsFormulaResult> mFormulaResultList;
    }

    public class WordsFormulaResult {
        public String mWords;
        Location mLocation;
    }

    public Formula parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        Formula formula = new Formula();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, formula);
            if (!jsonObject.isNull("direction")) {
                formula.mDirection = jsonObject.getInt("direction");
            }
            if (!jsonObject.isNull("words_result_num")) {
                formula.mWordsResultNum = jsonObject.getInt("words_result_num");
            }
            if (formula.mWordsResultNum > 0 && !jsonObject.isNull("words_result")) {
                formula.mWordsResultList = parseWordsResult(jsonObject.getJSONArray("words_result"));
            }
            if (!jsonObject.isNull("formula_result_num")) {
                formula.mFormulaResultNum = jsonObject.getInt("formula_result_num");
            }
            if (formula.mFormulaResultNum > 0 && !jsonObject.isNull("formula_result")) {
                formula.mFormulaResultList = parseWordsResult(jsonObject.getJSONArray("formula_result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return formula;
    }

    private ArrayList<WordsFormulaResult> parseWordsResult(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<WordsFormulaResult> wordsFormulaResultArrayList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                WordsFormulaResult wordsFormulaResult = new WordsFormulaResult();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("words")) {
                    wordsFormulaResult.mWords = jsonObject.getString("words");
                }
                if (!jsonObject.isNull("location")) {
                    wordsFormulaResult.mLocation = parseLocation(jsonObject.getJSONObject("location"));
                }

                wordsFormulaResultArrayList.add(wordsFormulaResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wordsFormulaResultArrayList;
    }
}
