package com.min.aiassistant.baidu.imageprocess.model;

import android.content.Context;
import android.util.Base64;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.imageprocess.BaiduImageProcessAI;
import com.min.aiassistant.baidu.imageprocess.parsejson.ParseImageProcessJson;
import com.min.aiassistant.utils.FileUtils;

import java.io.File;

public class SelfieAnime extends BaiduImageBaseModel<ParseImageProcessJson.ImageProcess> {

    public SelfieAnime(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-process/v1/selfie_anime";
    }

    protected ParseImageProcessJson.ImageProcess parseJson(String result) {
        return ParseImageProcessJson.getInstance().parse(result);
    }

    protected void handleResult(ParseImageProcessJson.ImageProcess response) {
        String mergeImageFile = mContext.getFilesDir() + File.separator + "selfie_anime.jpg";
        FileUtils.deleteFile(mergeImageFile);
        if (FileUtils.writeImageFile(Base64.decode(response.mImage, Base64.DEFAULT), mergeImageFile)) {
            mBaiduBaseListener.onFinalResult(mergeImageFile, BaiduImageProcessAI.SELFIE_ANIME_ACTION);
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduImageProcessAI.SELFIE_ANIME_ACTION);
        }
    }
}
