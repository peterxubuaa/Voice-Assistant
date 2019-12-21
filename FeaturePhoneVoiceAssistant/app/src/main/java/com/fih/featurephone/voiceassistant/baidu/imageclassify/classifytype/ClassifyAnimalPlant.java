package com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseAnimalPlantJson;

public class ClassifyAnimalPlant extends BaseClassify<ParseAnimalPlantJson.AnimalPlant> {

    public ClassifyAnimalPlant(Context context, BaiduClassifyImageAI.OnClassifyImageListener listener, String hostUrl, String requestParam) {
        super(context, listener, hostUrl, requestParam);
    }

    ParseAnimalPlantJson.AnimalPlant analyzeJson(String result) {
        return ParseAnimalPlantJson.parse(result);
    }

    void handleResult(ParseAnimalPlantJson.AnimalPlant response, boolean question) {
        if (null == mClassifyImageListener) return;

        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mClassifyImageListener.onError(mContext.getString(R.string.baidu_classify_image_animal_fail));
            return;
        }

        ParseAnimalPlantJson.Result result = response.mResultList.get(0);
        if (result.mScore >= MIN_SCORE) {
            String description = null;
            if (null != result.mBaiKeInfo) {
                description = result.mBaiKeInfo.mDescription;
            }
            mClassifyImageListener.onFinalResult(result.mName, description, question);
        } else {
            mClassifyImageListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), "", false);
        }
    }
}
