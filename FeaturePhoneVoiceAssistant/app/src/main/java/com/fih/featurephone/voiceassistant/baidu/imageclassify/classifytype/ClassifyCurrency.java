package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseCurrencyJson;

public class ClassifyCurrency extends BaseClassify<ParseCurrencyJson.Currency> {

    public ClassifyCurrency(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseCurrencyJson.Currency analyzeJson(String result) {
        return ParseCurrencyJson.parse(result);
    }

    void handleResult(ParseCurrencyJson.Currency response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mCurrencyName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_currency_fail));
            return;
        }

        ParseCurrencyJson.Result result = response.mResult;
        StringBuilder sb = new StringBuilder();
        if (1 == result.mHasDetail) {
            if (!TextUtils.isEmpty(result.mCurrencyCode)) sb.append(result.mCurrencyCode).append(",");
            if (!TextUtils.isEmpty(result.mCurrencyDenomination)) sb.append(result.mCurrencyDenomination).append(",");
            if (!TextUtils.isEmpty(result.mYear)) sb.append(result.mYear);

        }
        mClassifyImageListener.onFinalResult(result.mCurrencyName, sb.toString(), question);
    }
}
