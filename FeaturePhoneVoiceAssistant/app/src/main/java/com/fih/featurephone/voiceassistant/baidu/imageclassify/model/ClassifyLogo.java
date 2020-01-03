package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseLogoJson;

public class ClassifyLogo extends BaiduBaseModel<ParseLogoJson.Logo> {

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

        if (result.mProbability >= CLASSIFY_IMAGE_THRESHOLD) {
            mBaiduBaseListener.onFinalResult(response.mResultList.get(0).mName, mQuestion?
                    BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
