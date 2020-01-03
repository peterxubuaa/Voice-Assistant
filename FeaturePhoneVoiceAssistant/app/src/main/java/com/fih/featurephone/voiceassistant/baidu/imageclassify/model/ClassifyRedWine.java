package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseRedWineJson;

public class ClassifyRedWine extends BaiduBaseModel<ParseRedWineJson.RedWine> {

    public ClassifyRedWine(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/redwine";
    }

    protected ParseRedWineJson.RedWine parseJson(String result) {
        return ParseRedWineJson.getInstance().parse(result);
    }

    protected void handleResult(ParseRedWineJson.RedWine response) {
        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mWineNameCn)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_redwine_fail));
            return;
        }

        ParseRedWineJson.Result result = response.mResult;
        StringBuilder detail = new StringBuilder();
        if (1 == result.mHasDetail) {
            if (!TextUtils.isEmpty(result.mCountryCn)) detail.append(result.mCountryCn).append(",");
            if (!TextUtils.isEmpty(result.mRegionCn)) detail.append(result.mRegionCn).append(",");
            if (!TextUtils.isEmpty(result.mSubRegionCn)) detail.append(result.mSubRegionCn).append(",");
            if (!TextUtils.isEmpty(result.mWineryCn)) detail.append(result.mWineryCn).append(",");
            if (!TextUtils.isEmpty(result.mGrapeCn)) detail.append(result.mGrapeCn).append("。");

            if (!TextUtils.isEmpty(result.mWineNameEn)) detail.append(result.mWineNameEn).append(",");
            if (!TextUtils.isEmpty(result.mCountryEn)) detail.append(result.mCountryEn).append(",");
            if (!TextUtils.isEmpty(result.mRegionEn)) detail.append(result.mRegionEn).append(",");
            if (!TextUtils.isEmpty(result.mSubRegionEn)) detail.append(result.mSubRegionEn).append(",");
            if (!TextUtils.isEmpty(result.mWineryEn)) detail.append(result.mWineryEn).append(",");
            if (!TextUtils.isEmpty(result.mGrapeEn)) detail.append(result.mGrapeEn).append("。");

            if (!TextUtils.isEmpty(result.mClassifyByColor)) detail.append(result.mClassifyByColor).append(",");
            if (!TextUtils.isEmpty(result.mClassifyBySugar)) detail.append(result.mClassifyBySugar).append(",");
            if (!TextUtils.isEmpty(result.mColor)) detail.append(result.mColor).append(",");
            if (!TextUtils.isEmpty(result.mTasteTemperature)) detail.append(result.mTasteTemperature).append(",");
            if (!TextUtils.isEmpty(result.mDescription)) detail.append(result.mDescription);
        }

        if (TextUtils.isEmpty(detail.toString())) {
            mBaiduBaseListener.onFinalResult(result.mWineNameCn, mQuestion?
                    BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {//有物体描述，则不需要提问时
            mBaiduBaseListener.onFinalResult(result.mWineNameCn, BaiduClassifyImageAI.CLASSIFY_ACTION);
            mBaiduBaseListener.onFinalResult(detail.toString(), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
