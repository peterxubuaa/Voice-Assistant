package com.min.aiassistant.baidu.nlp.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseDNNSentenceJson extends BaiduParseBaseJson {

    private static ParseDNNSentenceJson sParseDNNSentenceJson = null;

    public static ParseDNNSentenceJson getInstance() {
        if (null == sParseDNNSentenceJson) {
            sParseDNNSentenceJson = new ParseDNNSentenceJson();
        }
        return sParseDNNSentenceJson;
    }

    public class DNNSentence extends BaiduParseBaseResponse {
        String mText;
        ArrayList<Item> mItems;
        public Double mPPL; //描述句子通顺的值：数值越低，句子越通顺
    }

    public class Item {
        double mProb; //该词在句子中的概率值,取值范围[0,1]
        String mWord; //句子的切词结果
    }

    public DNNSentence parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        DNNSentence dnnSentence = new DNNSentence();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, dnnSentence);
            if (!jsonObject.isNull("text")) {
                dnnSentence.mText = jsonObject.getString("text");
            }
            if (!jsonObject.isNull("ppl")) {
                dnnSentence.mPPL = jsonObject.getDouble("ppl");
            }
            if (!jsonObject.isNull("items")) {
                dnnSentence.mItems = parseItems(jsonObject.getJSONArray("items"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dnnSentence;
    }

    private ArrayList<Item> parseItems(JSONArray jsonArray) {
        ArrayList<Item> itemList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (null == jsonObject) continue;

                Item item = new Item();
                if (!jsonObject.isNull("word")){
                    item.mWord = jsonObject.getString("word");
                }
                if (!jsonObject.isNull("prob")){
                    item.mProb = jsonObject.getDouble("prob");
                }
                itemList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return itemList;
    }
}
