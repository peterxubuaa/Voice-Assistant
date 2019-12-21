package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseLandMarkJson;

public class ClassifyLandMark extends BaseClassify<ParseLandMarkJson.LandMark> {

    public ClassifyLandMark(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseLandMarkJson.LandMark analyzeJson(String result) {
        return ParseLandMarkJson.parse(result);
    }

    void handleResult(ParseLandMarkJson.LandMark response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mLandMark)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_landmark_fail));
            return;
        }

        mClassifyImageListener.onFinalResult(response.mResult.mLandMark, null, question);
    }
}
