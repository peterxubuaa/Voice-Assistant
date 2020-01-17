package com.min.aiassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.min.aiassistant.baidu.imageclassify.parsejson.ParseCarJson;

public class ClassifyCar extends BaiduImageBaseModel<ParseCarJson.Car> {

    public ClassifyCar(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/car";
        mURLRequestParamString = "&top_num=2&baike_num=2";
    }

    protected ParseCarJson.Car parseJson(String result) {
        return ParseCarJson.getInstance().parse(result);
    }

    protected void handleResult(ParseCarJson.Car response) {
        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mName)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_car_fail));
            return;
        }


        ParseCarJson.Result result = response.mResultList.get(0);
        if (result.mScore < CLASSIFY_IMAGE_THRESHOLD) {
            mBaiduBaseListener.onFinalResult(mContext.getString(R.string.baidu_classify_image_score_fail), BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
        String description = null;
        if (null != response.mResultList.get(0).mBaiKeInfo) {
            description = response.mResultList.get(0).mBaiKeInfo.mDescription;
        }
        if (TextUtils.isEmpty(description)) {
            mBaiduBaseListener.onFinalResult(response.mResultList.get(0).mName, mQuestion?
                    BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {//有物体描述，则不需要提问时
            mBaiduBaseListener.onFinalResult(response.mResultList.get(0).mName, BaiduClassifyImageAI.CLASSIFY_ACTION);
            mBaiduBaseListener.onFinalResult(description, BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
