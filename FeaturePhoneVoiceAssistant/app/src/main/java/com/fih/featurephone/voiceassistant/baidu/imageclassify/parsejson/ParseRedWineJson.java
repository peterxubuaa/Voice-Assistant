package com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseRedWineJson extends BaseParseJson {

    public static class RedWine {
        Long mLogID;
        public Result mResult;
    }

    public static class Result {
        public int mHasDetail;//判断是否返回详细信息（除红酒中文名之外的其他字段），含有返回1，不含有返回0
        public String mWineNameCn;//红酒中文名，无法识别返回空，示例：波斯塔瓦经典赤霞珠品丽珠半甜红葡萄酒
        public String mWineNameEn;//红酒英文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：Bostavan Classic Cabernet
        public String mCountryCn;//国家中文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：摩尔多瓦
        public String mCountryEn;//国家英文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：Moldova
        public String mRegionCn;//产区中文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：波尔多
        public String mRegionEn;//产区英文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：Bordeaux
        public String mSubRegionCn;//子产区中文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：梅多克
        public String mSubRegionEn;//子产区英文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：Medoc
        public String mWineryCn;//酒庄中文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：波斯塔瓦酒庄
        public String mWineryEn;//酒庄英文名，hasdetail = 0时，表示无法识别，该字段不返回，示例：Vinaria Bostavan
        public String mGrapeCn;//葡萄品种，可能有多种葡萄，hasdetail = 0时，表示无法识别，该字段不返回，示例：品丽珠;赤霞珠
        public String mGrapeEn;//葡萄品种英文名，可能有多种葡萄，hasdetail = 0时，表示无法识别，该字段不返回，示例：Cabernet Franc;Cabernet Sauvignon

        public String mClassifyByColor;//酒类型，hasdetail = 0时，表示无法识别，该字段不返回，示例：红葡萄酒
        public String mClassifyBySugar;//糖分类型，hasdetail = 0时，表示无法识别，该字段不返回，示例：半甜型
        public String mColor;//色泽，hasdetail = 0时，表示无法识别，该字段不返回，示例：宝石红色
        public String mTasteTemperature;//品尝温度，hasdetail = 0时，表示无法识别，该字段不返回，示例：6-11℃
        public String mDescription;//酒品描述，hasdetail = 0时，表示无法识别，该字段不返回，示例：葡萄酒呈深宝石红色，具有香料、香草和新鲜水果的果香，酒体分明，口感畅顺，果香横溢，单宁软化程度高，让你回味无穷
    }

    public static RedWine parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        RedWine redWine = new RedWine();
        try {
            JSONObject jsonObject = new JSONObject(result);
            if (!jsonObject.isNull("log_id")) {
                redWine.mLogID = jsonObject.getLong("log_id");
            }
            if (!jsonObject.isNull("result")) {
                redWine.mResult = parseResults(jsonObject.getJSONObject("result"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return redWine;
    }

    private static Result parseResults(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Result result = new Result();
        try {
            if (!jsonObject.isNull("wineNameCn")) {
                result.mWineNameCn = jsonObject.getString("wineNameCn");
            }
            if (!jsonObject.isNull("hasdetail")) {
                result.mHasDetail = jsonObject.getInt("hasdetail");
            }

            if (1 == result.mHasDetail) {
                if (!jsonObject.isNull("wineNameEn")) {
                    result.mWineNameEn = jsonObject.getString("wineNameEn");
                }
                if (!jsonObject.isNull("countryCn")) {
                    result.mCountryCn = jsonObject.getString("countryCn");
                }
                if (!jsonObject.isNull("countryEn")) {
                    result.mCountryEn = jsonObject.getString("countryEn");
                }
                if (!jsonObject.isNull("regionCn")) {
                    result.mRegionCn = jsonObject.getString("regionCn");
                }
                if (!jsonObject.isNull("regionEn")) {
                    result.mRegionEn = jsonObject.getString("regionEn");
                }
                if (!jsonObject.isNull("subRegionCn")) {
                    result.mSubRegionCn = jsonObject.getString("subRegionCn");
                }
                if (!jsonObject.isNull("subRegionEn")) {
                    result.mSubRegionEn = jsonObject.getString("subRegionEn");
                }
                if (!jsonObject.isNull("wineryCn")) {
                    result.mWineryCn = jsonObject.getString("wineryCn");
                }
                if (!jsonObject.isNull("wineryEn")) {
                    result.mWineryEn = jsonObject.getString("wineryEn");
                }
                if (!jsonObject.isNull("classifyByColor")) {
                    result.mClassifyByColor = jsonObject.getString("classifyByColor");
                }
                if (!jsonObject.isNull("classifyByColor")) {
                    result.mClassifyBySugar = jsonObject.getString("classifyBySugar");
                }
                if (!jsonObject.isNull("color")) {
                    result.mColor = jsonObject.getString("color");
                }
                if (!jsonObject.isNull("grapeCn")) {
                    result.mGrapeCn = jsonObject.getString("grapeCn");
                }
                if (!jsonObject.isNull("grapeEn")) {
                    result.mGrapeEn = jsonObject.getString("grapeEn");
                }
                if (!jsonObject.isNull("tasteTemperature")) {
                    result.mTasteTemperature = jsonObject.getString("tasteTemperature");
                }
                if (!jsonObject.isNull("tasteTemperature")) {
                    result.mDescription = jsonObject.getString("description");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
