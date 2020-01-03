package com.fih.featurephone.voiceassistant.baidu.humanbody.parsejson;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseHeadcountJson extends BaiduParseBaseJson {

    private static ParseHeadcountJson sParseHeadcountJson = null;

    public static ParseHeadcountJson getInstance() {
        if (null == sParseHeadcountJson) {
            sParseHeadcountJson = new ParseHeadcountJson();
        }
        return sParseHeadcountJson;
    }

    public class HeadCount extends BaiduParseBaseResponse {
        public int mPersonNum; //识别出的人体数目；当未设置area参数时，返回的是全图人数；设置了有效的area参数时，返回的人数是所有区域的人数总和（所有区域求并集后的不规则区域覆盖的人数）
        public String mImage; //渲染后的图片，输入参数show=true时输出该字段
    }

    public HeadCount parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        HeadCount headCount = new HeadCount();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, headCount);
            if (!jsonObject.isNull("person_num")) {
                headCount.mPersonNum = jsonObject.getInt("person_num");
            }
            if (!jsonObject.isNull("image")) {
                headCount.mImage = jsonObject.getString("image");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return headCount;
    }
}
