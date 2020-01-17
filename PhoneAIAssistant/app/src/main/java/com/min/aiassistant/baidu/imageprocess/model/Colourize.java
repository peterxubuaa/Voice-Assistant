package com.min.aiassistant.baidu.imageprocess.model;

import android.content.Context;
import android.util.Base64;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageprocess.BaiduImageProcessAI;
import com.min.aiassistant.baidu.imageprocess.parsejson.ParseImageProcessJson;
import com.min.aiassistant.utils.FileUtils;

import java.io.File;

public class Colourize extends BaiduImageBaseModel<ParseImageProcessJson.ImageProcess> {

    public Colourize(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-process/v1/colourize";
    }

    protected ParseImageProcessJson.ImageProcess parseJson(String result) {
        return ParseImageProcessJson.getInstance().parse(result);
    }

    protected void handleResult(ParseImageProcessJson.ImageProcess response) {
        String colourizeImageFile = mContext.getFilesDir() + File.separator + "colourize.jpg";
        FileUtils.deleteFile(colourizeImageFile);
        if (FileUtils.writeImageFile(Base64.decode(response.mImage, Base64.DEFAULT), colourizeImageFile)) {
            mBaiduBaseListener.onFinalResult(colourizeImageFile, BaiduImageProcessAI.COLOURIZE_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduImageProcessAI.COLOURIZE_ACTION);
        }
    }
}
