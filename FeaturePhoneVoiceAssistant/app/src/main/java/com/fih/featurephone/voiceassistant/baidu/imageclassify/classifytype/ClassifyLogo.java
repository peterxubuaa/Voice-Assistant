package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseLogoJson;

public class ClassifyLogo extends BaseClassify<ParseLogoJson.Logo> {

    public ClassifyLogo(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseLogoJson.Logo analyzeJson(String result) {
        return ParseLogoJson.parse(result);
    }

    void handleResult(ParseLogoJson.Logo response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList
                || response.mResultList.size() == 0 || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_logo_fail));
            return;
        }

        ParseLogoJson.Result result = response.mResultList.get(0);

        if (result.mProbability >= MIN_SCORE) {
            mClassifyImageListener.onFinalResult(response.mResultList.get(0).mName, null, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
