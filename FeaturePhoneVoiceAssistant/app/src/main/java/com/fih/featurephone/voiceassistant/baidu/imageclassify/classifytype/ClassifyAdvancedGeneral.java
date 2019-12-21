package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseAdvancedGeneralJson;

public class ClassifyAdvancedGeneral extends BaseClassify<ParseAdvancedGeneralJson.Advanced_General> {
    public ClassifyAdvancedGeneral(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseAdvancedGeneralJson.Advanced_General analyzeJson(String result) {
        return ParseAdvancedGeneralJson.parse(result);
    }

    void handleResult(ParseAdvancedGeneralJson.Advanced_General response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mKeyword)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_general_fail));
            return;
        }
        ParseAdvancedGeneralJson.Result result = response.mResultList.get(0);

        if (result.mScore >= MIN_SCORE) {
            String mainInfo = result.mKeyword;// + "(" + result.mRoot + ")";
            String description = null;
            if (null != result.mBaiKeInfo) {
                description = result.mBaiKeInfo.mDescription;
            }
            mClassifyImageListener.onFinalResult(mainInfo, description, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
