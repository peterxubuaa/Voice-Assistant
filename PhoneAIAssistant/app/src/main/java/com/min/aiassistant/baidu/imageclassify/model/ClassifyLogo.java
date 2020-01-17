package com.min.aiassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.min.aiassistant.baidu.imageclassify.parsejson.ParseLogoJson;

public class ClassifyLogo extends BaiduImageBaseModel<ParseLogoJson.Logo> {

    public ClassifyLogo(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v2/logo";
        mURLRequestParamString = "&custom_lib=false";
    }

    protected ParseLogoJson.Logo parseJson(String result) {
        return ParseLogoJson.getInstance().parse(result);
    }

    protected void handleResult(ParseLogoJson.Logo response) {
        if (null == response || null == response.mResultList
                || response.mResultList.size() == 0 || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_logo_fail));
            return;
        }

        ParseLogoJson.Result result = response.mResultList.get(0);

        if (result.mProbability < CLASSIFY_IMAGE_THRESHOLD) {
            mBaiduBaseListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
        mBaiduBaseListener.onFinalResult(response.mResultList.get(0).mName, mQuestion?
                BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
    }
}
