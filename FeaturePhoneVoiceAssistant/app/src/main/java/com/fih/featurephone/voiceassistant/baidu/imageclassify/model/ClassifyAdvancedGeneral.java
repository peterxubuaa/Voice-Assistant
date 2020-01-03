package com.fih.featurephone.voiceassistant.baidu.imageclassify.model;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.parsejson.ParseAdvancedGeneralJson;

public class ClassifyAdvancedGeneral extends BaiduBaseModel<ParseAdvancedGeneralJson.AdvancedGeneral> {

    public ClassifyAdvancedGeneral(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general";
        mURLRequestParamString = "&baike_num=2";
    }

    protected ParseAdvancedGeneralJson.AdvancedGeneral parseJson(String json) {
        return ParseAdvancedGeneralJson.getInstance().parse(json);
    }

    protected void handleResult(ParseAdvancedGeneralJson.AdvancedGeneral response) {
        if (null == response || null == response.mResultList || response.mResultList.size() == 0
                || TextUtils.isEmpty(response.mResultList.get(0).mKeyword)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_classify_image_general_fail));
            return;
        }
        ParseAdvancedGeneralJson.Result result = response.mResultList.get(0);

        String mainInfo = result.mKeyword;// + "(" + result.mRoot + ")";
        String description = null;
        if (null != result.mBaiKeInfo) {
            description = result.mBaiKeInfo.mDescription;
        }

        if (TextUtils.isEmpty(description)) {
            mBaiduBaseListener.onFinalResult(mainInfo, mQuestion?
                    BaiduClassifyImageAI.CLASSIFY_QUESTION_ACTION : BaiduClassifyImageAI.CLASSIFY_ACTION);
        } else {//有物体描述，则不需要提问时
            mBaiduBaseListener.onFinalResult(mainInfo, BaiduClassifyImageAI.CLASSIFY_ACTION);
            mBaiduBaseListener.onFinalResult(description, BaiduClassifyImageAI.CLASSIFY_ACTION);
        }
    }
}
