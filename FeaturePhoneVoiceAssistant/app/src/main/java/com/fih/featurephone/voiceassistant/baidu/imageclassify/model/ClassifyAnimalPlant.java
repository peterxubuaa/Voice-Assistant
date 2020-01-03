package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseAnimalPlantJson;

public class ClassifyAnimalPlant extends BaiduBaseModel<ParseAnimalPlantJson.AnimalPlant> {
    public static final int PLANT = 1;
    public static final int ANIMAL = 2;

    public ClassifyAnimalPlant(Context context, BaiduBaseAI.IBaiduBaseListener listener, int type) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mURLRequestParamString = "&top_num=2&baike_num=2";
        switch (type) {
            case PLANT:
                mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant";
                break;
            case ANIMAL:
                mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/animal";
                break;
        }
    }

    protected ParseAnimalPlantJson.AnimalPlant parseJson(String result) {
        return ParseAnimalPlantJson.getInstance().parse(result);
    }

    protected void handleResult(ParseAnimalPlantJson.AnimalPlant response) {
        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_animal_fail));
            return;
        }

        ParseAnimalPlantJson.Result result = response.mResultList.get(0);
        if (result.mScore >= CLASSIFY_IMAGE_THRESHOLD) {
            String description = null;
            if (null != result.mBaiKeInfo) {
                description = result.mBaiKeInfo.mDescription;
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
