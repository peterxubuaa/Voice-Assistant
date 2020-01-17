package com.min.aiassistant.baidu.imageprocess.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseImageProcessJson extends BaiduParseBaseJson {
    private static ParseImageProcessJson sParseImageProcessJson = null;

    public static ParseImageProcessJson getInstance() {
        if (null == sParseImageProcessJson) {
            sParseImageProcessJson = new ParseImageProcessJson();
        }
        return sParseImageProcessJson;
    }

    public class ImageProcess extends BaiduParseBaseResponse {
        public String mImage;//更新图的BASE64值
    }

    public ImageProcess parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        ImageProcess imageProcess = new ImageProcess();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, imageProcess);
            if (!jsonObject.isNull("image")) {
                imageProcess.mImage = jsonObject.getString("image");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return imageProcess;
    }
}
