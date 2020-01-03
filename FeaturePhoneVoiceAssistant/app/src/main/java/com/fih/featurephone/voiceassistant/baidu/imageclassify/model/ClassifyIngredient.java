package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseIngredientJson;

public class ClassifyIngredient extends BaiduBaseModel<ParseIngredientJson.Ingredient> {

    public ClassifyIngredient(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/classify/ingredient";
        mURLRequestParamString = "&top_num=2";
    }

    protected ParseIngredientJson.Ingredient parseJson(String result) {
        return ParseIngredientJson.getInstance().parse(result);
    }

    protected void handleResult(ParseIngredientJson.Ingredient response) {
        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_ingredient_fail));
            return;
        }

        ParseIngredientJson.Result result = response.mResultList.get(0);
        if (result.mScore >= CLASSIFY_IMAGE_THRESHOLD) {
            mBaiduBaseListener.onFinalResult(result.mName, mQuestion?
                        BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
