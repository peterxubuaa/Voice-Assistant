package com.fih.featurephone.voiceassistant.baidu.humanbody.model;

import android.content.Context;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.humanbody.BaiduHumanBodyAI;
import com.fih.featurephone.voiceassistant.baidu.humanbody.parsejson.ParseHeadcountJson;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.io.File;

//适用于3米以上的中远距离俯拍，以头部为主要识别目标统计人数，无需正脸、全身照，适应各类人流密集场景（如：机场、车展、景区、广场等）；默认识别整图中的人数，支持指定不规则区域的人数统计，同时可输出渲染图片。
public class Headcount extends BaiduBaseModel<ParseHeadcountJson.HeadCount> {

    public Headcount(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_num";
        mURLRequestParamString = "&show=true";//是否输出渲染的图片，默认不返回，选true时返回渲染后的图片(base64)，其它无效值或为空则默认false
    }

    protected ParseHeadcountJson.HeadCount parseJson(String json) {
        return ParseHeadcountJson.getInstance().parse(json);
    }

    protected void handleResult(ParseHeadcountJson.HeadCount headcount) {
        mBaiduBaseListener.onFinalResult("一共有 " + headcount.mPersonNum + " 人", BaiduHumanBodyAI.HEAD_COUNT_ACTION);

        String headcountImageFile = mContext.getFilesDir() + File.separator + "headcount.jpg";
        FileUtils.deleteFile(headcountImageFile);
        if (FileUtils.writeImageFile(Base64.decode(headcount.mImage, Base64.DEFAULT), headcountImageFile)) {
            mBaiduBaseListener.onFinalResult(headcountImageFile, BaiduHumanBodyAI.HEAD_COUNT_IMAGE_ACTION);
        }
    }
}
