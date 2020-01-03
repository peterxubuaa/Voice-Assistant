package com.fih.featurephone.voiceassistant.baidu.ocr.model;

import android.content.Context;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.parsejson.ParseQRCodeJson;

//适用于3米以上的中远距离俯拍，以头部为主要识别目标统计人数，无需正脸、全身照，适应各类人流密集场景（如：机场、车展、景区、广场等）；默认识别整图中的人数，支持指定不规则区域的人数统计，同时可输出渲染图片。
public class RecognizeQRCode extends BaiduBaseModel<ParseQRCodeJson.QRCode> {

    public RecognizeQRCode(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/qrcode";
    }

    protected ParseQRCodeJson.QRCode parseJson(String json) {
        return ParseQRCodeJson.getInstance().parse(json);
    }

    protected void handleResult(ParseQRCodeJson.QRCode qrCode) {
        if (null == qrCode.mCodesResultList) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_ocr_qrcode_fail));
            return;
        }

        StringBuilder result = new StringBuilder();
        for (ParseQRCodeJson.CodesResult codesResult : qrCode.mCodesResultList) {
            result.append(codesResult.mType).append("\n");
            result.append(codesResult.mText).append("\n");
        }

        mBaiduBaseListener.onFinalResult(result.toString(), BaiduOcrAI.OCR_QRCODE_ACTION);
    }
}
