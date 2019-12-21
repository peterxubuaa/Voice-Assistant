package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseCarJson;

public class ClassifyCar extends BaseClassify<ParseCarJson.Car> {

    public ClassifyCar(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseCarJson.Car analyzeJson(String result) {
        return ParseCarJson.parse(result);
    }

    void handleResult(ParseCarJson.Car response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_car_fail));
            return;
        }

        ParseCarJson.Result result = response.mResultList.get(0);
        if (result.mScore >= MIN_SCORE) {
            String description = null;
            if (null != response.mResultList.get(0).mBaiKeInfo) {
                description = response.mResultList.get(0).mBaiKeInfo.mDescription;
            }
            mClassifyImageListener.onFinalResult(response.mResultList.get(0).mName, description, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
