package com.fih.featurephone.voiceassistant.baidu.ocr.model;

import android.content.Context;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.parsejson.ParseFormulaJson;

//适用于3米以上的中远距离俯拍，以头部为主要识别目标统计人数，无需正脸、全身照，适应各类人流密集场景（如：机场、车展、景区、广场等）；默认识别整图中的人数，支持指定不规则区域的人数统计，同时可输出渲染图片。
public class RecognizeFormula extends BaiduBaseModel<ParseFormulaJson.Formula> {

    public RecognizeFormula(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/ocr/v1/formula";
        mURLRequestParamString = "&detect_direction=true" + "&recognize_granularity=big" + "&disp_formula=true";
    }

    protected ParseFormulaJson.Formula parseJson(String json) {
        return ParseFormulaJson.getInstance().parse(json);
    }

    protected void handleResult(ParseFormulaJson.Formula formula) {
        if (null == formula.mWordsResultList) {
            mBaiduBaseListener.onError(mContext.getString(R.string.baidu_ocr_formula_fail));
            return;
        }

        StringBuilder result = new StringBuilder();
        for (ParseFormulaJson.WordsFormulaResult wordsResult : formula.mWordsResultList) {
            result.append(wordsResult.mWords).append("\n");
        }

        for (ParseFormulaJson.WordsFormulaResult formulaResult : formula.mFormulaResultList) {
            result.append(formulaResult.mWords).append("\n");
        }
        mBaiduBaseListener.onFinalResult(result.toString(), BaiduOcrAI.OCR_FORMULA_ACTION);
    }
}
