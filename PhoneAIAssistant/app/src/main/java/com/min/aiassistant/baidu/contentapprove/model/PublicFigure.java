package com.min.aiassistant.baidu.contentapprove.model;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.contentapprove.BaiduContentApproveAI;
import com.min.aiassistant.baidu.contentapprove.parsejson.ParsePublicFigureJson;

import java.util.ArrayList;

//https://console.bce.baidu.com/ai/?_=1577177054789&fromai=1#/ai/antiporn/app/detail~appId=1405752
//https://ai.baidu.com/censoring#/strategylist   策略列表
//https://ai.baidu.com/censoring#/strategylist/17937628
public class PublicFigure extends BaiduImageBaseModel<ParsePublicFigureJson.PublicFigure> {

    public PublicFigure(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined";
//        mURLRequestParamString = "&imgType=0";
    }

    protected ParsePublicFigureJson.PublicFigure parseJson(String json) {
        return ParsePublicFigureJson.getInstance().parse(json);
    }

    protected void handleResult(ParsePublicFigureJson.PublicFigure publicFigure) {
        ArrayList<String> nameList = new ArrayList<>();
        if (2 == publicFigure.mConclusionType) {//2：不合规
            for (ParsePublicFigureJson.Data data : publicFigure.mDataList) {
                if (5 != data.mType) continue; //5：政治敏感识别
                for (ParsePublicFigureJson.Stars stars : data.mStarsList) {
                    if (stars.mProbability < PUBLIC_FIGURE_THRESHOLD) continue;

                    if (nameList.contains(stars.mName)) continue; //去重
                    nameList.add(stars.mName);
                }
            }
        }

        mBaiduBaseListener.onFinalResult(nameList, mQuestion? BaiduContentApproveAI.PUBLIC_FIGURE_QUESTION_ACTION :
                BaiduContentApproveAI.PUBLIC_FIGURE_ACTION);
    }
}
