package com.min.aiassistant.baidu.nlp.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseCorrectTextJson extends BaiduParseBaseJson {

    private static ParseCorrectTextJson sParseCorrectTextJson = null;

    public static ParseCorrectTextJson getInstance() {
        if (null == sParseCorrectTextJson) {
            sParseCorrectTextJson = new ParseCorrectTextJson();
        }
        return sParseCorrectTextJson;
    }

    public class CorrectText extends BaiduParseBaseResponse {
        String mText;
        public Item mItem;
    }

    public class Item {
        double mScore;
        public String mCorrectQuery;
        public ArrayList<VecFragment> mVecFragmentList;
    }

    public class VecFragment {
        public String mOriFrag;
        public String mCorrectFrag;
        int mBeginPos;
        int mEndPos;
    }

    public CorrectText parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        CorrectText correctText = new CorrectText();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, correctText);
            if (!jsonObject.isNull("text")) {
                correctText.mText = jsonObject.getString("text");
            }
            if (!jsonObject.isNull("item")) {
                correctText.mItem = parseItem(jsonObject.getJSONObject("item"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return correctText;
    }

    private Item parseItem(JSONObject jsonObject) {
        Item item = new Item();
        try {
            if (!jsonObject.isNull("correct_query")) {
                item.mCorrectQuery = jsonObject.getString("correct_query");
            }
            if (!jsonObject.isNull("score")) {
                item.mScore = jsonObject.getDouble("score");
            }
            if (!jsonObject.isNull("vec_fragment")) {
                item.mVecFragmentList = parseVecFragment(jsonObject.getJSONArray("vec_fragment"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return item;
    }

    private ArrayList<VecFragment> parseVecFragment(JSONArray jsonArray) {
        ArrayList<VecFragment> vecFragments = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (null == jsonObject) continue;

                VecFragment vecFragment = new VecFragment();
                if (!jsonObject.isNull("ori_frag")){
                    vecFragment.mOriFrag = jsonObject.getString("ori_frag");
                }
                if (!jsonObject.isNull("correct_frag")){
                    vecFragment.mCorrectFrag = jsonObject.getString("correct_frag");
                }
                if (!jsonObject.isNull("begin_pos")){
                    vecFragment.mBeginPos = jsonObject.getInt("begin_pos");
                }
                if (!jsonObject.isNull("end_pos")){
                    vecFragment.mEndPos = jsonObject.getInt("end_pos");
                }
                vecFragments.add(vecFragment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  vecFragments;
    }
}
