package com.min.aiassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.min.aiassistant.baidu.imageclassify.parsejson.ParseCurrencyJson;

public class ClassifyCurrency extends BaiduImageBaseModel<ParseCurrencyJson.Currency> {

    public ClassifyCurrency(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/currency";
    }

    protected ParseCurrencyJson.Currency parseJson(String result) {
        return ParseCurrencyJson.getInstance().parse(result);
    }

    protected void handleResult(ParseCurrencyJson.Currency response) {
        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mCurrencyName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_currency_fail));
            return;
        }

        ParseCurrencyJson.Result result = response.mResult;
        StringBuilder detail = new StringBuilder();
        if (1 == result.mHasDetail) {
            if (!TextUtils.isEmpty(result.mCurrencyCode)) detail.append(result.mCurrencyCode).append(",");
            if (!TextUtils.isEmpty(result.mCurrencyDenomination)) detail.append(result.mCurrencyDenomination).append(",");
            if (!TextUtils.isEmpty(result.mYear)) detail.append(result.mYear);

        }

        if (TextUtils.isEmpty(detail.toString())) {
            mBaiduBaseListener.onFinalResult(result.mCurrencyName, mQuestion?
                    BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {//有物体描述，则不需要提问时
            mBaiduBaseListener.onFinalResult(result.mCurrencyName, BaiduClassifyImageAI.CLASSIFY_ACTION);
            mBaiduBaseListener.onFinalResult(detail.toString(), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
