package com.min.aiassistant.baidu.ocr.model;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduImageBaseModel;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.ocr.parsejson.ParseFormulaJson;

public class RecognizeFormula extends BaiduImageBaseModel<ParseFormulaJson.Formula> {

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
