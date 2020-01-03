package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseDishJson;

public class ClassifyDish extends BaiduBaseModel<ParseDishJson.Dish> {

    public ClassifyDish(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish";
        mURLRequestParamString = "&top_num=2&baike_num=2";
    }

    protected ParseDishJson.Dish parseJson(String result) {
        return ParseDishJson.getInstance().parse(result);
    }

    protected void handleResult(ParseDishJson.Dish response) {
        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_dish_fail));
            return;
        }

        ParseDishJson.Result result = response.mResultList.get(0);

        if (result.mProbability >= CLASSIFY_IMAGE_THRESHOLD) {
            String description = String.format(mContext.getString(R.string.baidu_classify_image_dish_calorie), result.mCalorie);
            if (null != result.mBaiKeInfo) {
                description += result.mBaiKeInfo.mDescription;
            }

            if (TextUtils.isEmpty(description)) {
                mBaiduBaseListener.onFinalResult(result.mName, mQuestion?
                        BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
            } else {//有物体描述，则不需要提问时
                mBaiduBaseListener.onFinalResult(result.mName, BaiduClassifyImageAI.CLASSIFY_ACTION);
                mBaiduBaseListener.onFinalResult(description, BaiduClassifyImageAI.CLASSIFY_ACTION);
            }
        } else {
            mBaiduBaseListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
