package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseDishJson;

public class ClassifyDish extends BaseClassify<ParseDishJson.Dish> {

    public ClassifyDish(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseDishJson.Dish analyzeJson(String result) {
        return ParseDishJson.parse(result);
    }

    void handleResult(ParseDishJson.Dish response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_dish_fail));
            return;
        }

        ParseDishJson.Result result = response.mResultList.get(0);

        if (result.mProbability >= MIN_SCORE) {
            String description = String.format(mContext.getString(R.string.baidu_classify_image_dish_calorie), result.mCalorie);
            if (null != result.mBaiKeInfo) {
                description += result.mBaiKeInfo.mDescription;
            }
            mClassifyImageListener.onFinalResult(result.mName, description, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
