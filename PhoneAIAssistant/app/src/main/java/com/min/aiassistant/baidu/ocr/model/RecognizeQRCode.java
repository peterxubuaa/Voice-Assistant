package com.min.aiassistant.baidu.ocr.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.ocr.parsejson.ParseQRCodeJson;

public class RecognizeQRCode extends BaiduImageBaseModel<ParseQRCodeJson.QRCode> {

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
