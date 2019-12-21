package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import org.json.JSONException;
import org.json.JSONObject;

class BaseParseJson {

    public static class BaiKeInfo {
        String mBaiKeUrl;//对应识别结果百度百科页面链接
        String mImageUrl;//对应识别结果百科图片链接
        public String mDescription;//对应识别结果百科内容描述
    }

    static BaiKeInfo parseBaiKeInfo(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        BaiKeInfo baiKeInfo = new BaiKeInfo();
        try {
            if (!jsonObject.isNull("baike_url")) {
                baiKeInfo.mBaiKeUrl = jsonObject.getString("baike_url");
            }
            if (!jsonObject.isNull("image_url")) {
                baiKeInfo.mImageUrl = jsonObject.getString("image_url");
            }

            if (!jsonObject.isNull("description")) {
                baiKeInfo.mDescription = jsonObject.getString("description");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return baiKeInfo;
    }
}
