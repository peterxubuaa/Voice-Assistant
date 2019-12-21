package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseRedWineJson;

public class ClassifyRedWine extends BaseClassify<ParseRedWineJson.RedWine> {

    public ClassifyRedWine(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseRedWineJson.RedWine analyzeJson(String result) {
        return ParseRedWineJson.parse(result);
    }

    void handleResult(ParseRedWineJson.RedWine response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mWineNameCn)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_redwine_fail));
            return;
        }

        ParseRedWineJson.Result result = response.mResult;
        StringBuilder sb = new StringBuilder();
        if (1 == result.mHasDetail) {
            if (!TextUtils.isEmpty(result.mCountryCn)) sb.append(result.mCountryCn).append(",");
            if (!TextUtils.isEmpty(result.mRegionCn)) sb.append(result.mRegionCn).append(",");
            if (!TextUtils.isEmpty(result.mSubRegionCn)) sb.append(result.mSubRegionCn).append(",");
            if (!TextUtils.isEmpty(result.mWineryCn)) sb.append(result.mWineryCn).append(",");
            if (!TextUtils.isEmpty(result.mGrapeCn)) sb.append(result.mGrapeCn).append("。");

            if (!TextUtils.isEmpty(result.mWineNameEn)) sb.append(result.mWineNameEn).append(",");
            if (!TextUtils.isEmpty(result.mCountryEn)) sb.append(result.mCountryEn).append(",");
            if (!TextUtils.isEmpty(result.mRegionEn)) sb.append(result.mRegionEn).append(",");
            if (!TextUtils.isEmpty(result.mSubRegionEn)) sb.append(result.mSubRegionEn).append(",");
            if (!TextUtils.isEmpty(result.mWineryEn)) sb.append(result.mWineryEn).append(",");
            if (!TextUtils.isEmpty(result.mGrapeEn)) sb.append(result.mGrapeEn).append("。");

            if (!TextUtils.isEmpty(result.mClassifyByColor)) sb.append(result.mClassifyByColor).append(",");
            if (!TextUtils.isEmpty(result.mClassifyBySugar)) sb.append(result.mClassifyBySugar).append(",");
            if (!TextUtils.isEmpty(result.mColor)) sb.append(result.mColor).append(",");
            if (!TextUtils.isEmpty(result.mTasteTemperature)) sb.append(result.mTasteTemperature).append(",");
            if (!TextUtils.isEmpty(result.mDescription)) sb.append(result.mDescription);

        }
        mClassifyImageListener.onFinalResult(result.mWineNameCn, sb.toString(), question);
    }
}
