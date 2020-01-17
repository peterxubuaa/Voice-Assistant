package com.min.aiassistant.baidu.imageprocess.model;

import android.content.Context;
import android.util.Base64;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageprocess.BaiduImageProcessAI;
import com.min.aiassistant.baidu.imageprocess.parsejson.ParseImageProcessJson;
import com.min.aiassistant.utils.FileUtils;

import java.io.File;

public class StyleTrans extends BaiduImageBaseModel<ParseImageProcessJson.ImageProcess> {

    public StyleTrans(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-process/v1/style_trans";
        mURLRequestParamString = "&option=cartoon";//cartoon：卡通画风格, pencil：素描风格, painting：油画风格（即将上线）
    }

    protected ParseImageProcessJson.ImageProcess parseJson(String result) {
        return ParseImageProcessJson.getInstance().parse(result);
    }

    protected void handleResult(ParseImageProcessJson.ImageProcess response) {
        String styleTransImageFile = mContext.getFilesDir() + File.separator + "style_trans.jpg";
        FileUtils.deleteFile(styleTransImageFile);
        if (FileUtils.writeImageFile(Base64.decode(response.mImage, Base64.DEFAULT), styleTransImageFile)) {
            mBaiduBaseListener.onFinalResult(styleTransImageFile, BaiduImageProcessAI.COLOURIZE_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduImageProcessAI.COLOURIZE_ACTION);
        }

        //依次转换风格
        if (mURLRequestParamString.equals("&option=cartoon")) {
            mURLRequestParamString = "&option=pencil";
        } else {
            mURLRequestParamString = "&option=cartoon";
        }
    }
}
