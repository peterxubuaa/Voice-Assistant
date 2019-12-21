package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseIngredientJson;

public class ClassifyIngredient extends BaseClassify<ParseIngredientJson.Ingredient> {

    public ClassifyIngredient(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseIngredientJson.Ingredient analyzeJson(String result) {
        return ParseIngredientJson.parse(result);
    }

    void handleResult(ParseIngredientJson.Ingredient response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_ingredient_fail));
            return;
        }

        ParseIngredientJson.Result result = response.mResultList.get(0);
        if (result.mScore >= MIN_SCORE) {
            mClassifyImageListener.onFinalResult(result.mName, null, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
