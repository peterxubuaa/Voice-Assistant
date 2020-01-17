package com.min.aiassistant.baidu.humanbody.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseBodySegmentJson extends BaiduParseBaseJson {

    private static ParseBodySegmentJson sParseBodySegmentJson = null;

    public static ParseBodySegmentJson getInstance() {
        if (null == sParseBodySegmentJson) {
            sParseBodySegmentJson = new ParseBodySegmentJson();
        }
        return sParseBodySegmentJson;
    }

    public class BodySegment extends BaiduParseBaseResponse {
        public String mLabelMap; //分割结果图片，base64编码之后的二值图像，需二次处理方能查看分割效果
        public String mScoreMap; //分割后人像前景的scoremap，归一到0-255，不用进行二次处理，直接解码保存图片即可。Base64编码后的灰度图文件，图片中每个像素点的灰度值 = 置信度 * 255，置信度为原图对应像素点位于人体轮廓内的置信度，取值范围[0, 1]
        public String mForeground; //分割后的人像前景抠图，透明背景，Base64编码后的png格式图片，不用进行二次处理，直接解码保存图片即可。将置信度大于0.5的像素抠出来，并通过image matting技术消除锯齿
    }

    public BodySegment parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        BodySegment bodySegment = new BodySegment();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, bodySegment);
            if (!jsonObject.isNull("labelmap")) {
                bodySegment.mLabelMap = jsonObject.getString("labelmap");
            }
            if (!jsonObject.isNull("scoremap")) {
                bodySegment.mScoreMap = jsonObject.getString("scoremap");
            }
            if (!jsonObject.isNull("foreground")) {
                bodySegment.mForeground = jsonObject.getString("foreground");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return bodySegment;
    }
}
