package com.min.aiassistant.baidu.humanbody.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.humanbody.BaiduHumanBodyAI;
import com.min.aiassistant.baidu.humanbody.parsejson.ParseBodySegmentJson;
import com.min.aiassistant.utils.FileUtils;

import java.io.File;

public class BodySegment extends BaiduImageBaseModel<ParseBodySegmentJson.BodySegment> {

    public BodySegment(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/body_seg";
        mURLRequestParamString = "&type=foreground";//labelmap - 二值图像，需二次处理方能查看分割效果, scoremap - 人像前景灰度图, foreground - 人像前景抠图，透明背景
    }

    protected ParseBodySegmentJson.BodySegment parseJson(String json) {
        return ParseBodySegmentJson.getInstance().parse(json);
    }

    protected void handleResult(ParseBodySegmentJson.BodySegment bodySegment) {
        String bodySegmentImageFile = mContext.getFilesDir() + File.separator + "body_segment.jpg";
        FileUtils.deleteFile(bodySegmentImageFile);

        if (!TextUtils.isEmpty(bodySegment.mForeground) && FileUtils.writeImageFile(Base64.decode(bodySegment.mForeground, Base64.DEFAULT), bodySegmentImageFile)) {
            mBaiduBaseListener.onFinalResult(bodySegmentImageFile, BaiduHumanBodyAI.BODY_SEGMENT_IMAGE_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduHumanBodyAI.BODY_SEGMENT_IMAGE_ACTION);
        }
    }
}
