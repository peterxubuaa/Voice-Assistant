package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseLandMarkJson;

public class ClassifyLandMark extends BaiduBaseModel<ParseLandMarkJson.LandMark> {

    public ClassifyLandMark(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/landmark";
    }

    protected ParseLandMarkJson.LandMark parseJson(String result) {
        return ParseLandMarkJson.getInstance().parse(result);
    }

    protected void handleResult(ParseLandMarkJson.LandMark response) {
        if (null == response || null == response.mResult || TextUtils.isEmpty(response.mResult.mLandMark)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_landmark_fail));
            return;
        }

        mBaiduBaseListener.onFinalResult(response.mResult.mLandMark, mQuestion?
                BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
    }
}
