package com.min.aiassistant.baidu.ocr.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseBasicTextJson extends BaiduParseBaseJson {

    private static ParseBasicTextJson sParseOCRTextJson = null;

    public static ParseBasicTextJson getInstance() {
        if (null == sParseOCRTextJson) {
            sParseOCRTextJson = new ParseBasicTextJson();
        }
        return sParseOCRTextJson;
    }

    public class OCRText extends BaiduParseBaseResponse {
        int mDirection; //图像方向，当detect_direction=true时存在。- -1:未定义，- 0:正向，- 1: 逆时针90度，- 2:逆时针180度，- 3:逆时针270度
        int mWordsResultNum;
        public ArrayList<WordsResult> mWordsResultList;
    }

    public class WordsResult {
        public String mWords;
        Probability mProbability;
    }

    public class Probability {//识别结果中每一行的置信度值
        double mVariance; //行置信度方差
        double mAverage; //行置信度平均值
        double mMin; //行置信度最小值
    }

    public OCRText parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        OCRText ocrText = new OCRText();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, ocrText);
            if (!jsonObject.isNull("direction")) {
                ocrText.mDirection = jsonObject.getInt("direction");
            }
            if (!jsonObject.isNull("words_result_num")) {
                ocrText.mWordsResultNum = jsonObject.getInt("words_result_num");
            }
            if (ocrText.mWordsResultNum > 0 && !jsonObject.isNull("words_result")) {
                ocrText.mWordsResultList = parseWordsResult(jsonObject.getJSONArray("words_result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return ocrText;
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
                if (!jsonObject.isNull("probability")) {
                    wordsResult.mProbability = parseProbability(jsonObject.getJSONObject("probability"));
                }

                wordsResultList.add(wordsResult);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wordsResultList;
    }

    private Probability parseProbability(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        try {
            Probability probability = new Probability();
            if (!jsonObject.isNull("variance")) {
                probability.mVariance = jsonObject.getDouble("variance");
            }
            if (!jsonObject.isNull("average")) {
                probability.mAverage = jsonObject.getDouble("average");
            }
            if (!jsonObject.isNull("min")) {
                probability.mMin = jsonObject.getDouble("min");
            }
            return probability;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
