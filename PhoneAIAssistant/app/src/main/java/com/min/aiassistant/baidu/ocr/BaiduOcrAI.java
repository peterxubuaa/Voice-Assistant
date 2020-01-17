package com.min.aiassistant.baidu.ocr;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.ocr.model.RecognizeFormula;
import com.min.aiassistant.baidu.ocr.model.RecognizeHandwriting;
import com.min.aiassistant.baidu.ocr.model.RecognizeQRCode;
import com.min.aiassistant.baidu.ocr.model.RecognizeText;

import java.util.HashMap;
import java.util.Map;

//OCR可识别的语言
//"CHN_ENG";
//"CHN"; 汉语
//"ENG";英文
//"POR";葡萄牙语
//"FRE";法语
//"GER";德语
//"ITA";意大利语
//"SPA";西班牙语
//"RUS";俄语
//"JAP";日语
//"KOR";韩语

public class BaiduOcrAI extends BaiduBaseAI {
//    private static final int TEXT_TYPE = 1;
    private static final int FORMULA_TYPE = 2;
    public static final int QRCODE_TYPE = 3;
    public static final int HANDWRITING_TYPE = 4;

    public static final int OCR_TEXT_ACTION = 1;
    public static final int OCR_TEXT_QUESTION_ACTION = 2;
    public static final int OCR_FORMULA_ACTION = 3;
    public static final int OCR_QRCODE_ACTION = 5;

    public static final Map<String, String> LANGUAGE_MAP = new HashMap<>();
    public static final String OCR_DEFAULT_LANGUAGE = "中英文";

    private RecognizeText mRecognizeText;
    private RecognizeFormula mRecognizeFormula;
    private RecognizeQRCode mRecognizeQRCode;
    private RecognizeHandwriting mRecognizeHandwriting;

    public BaiduOcrAI(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        initMapValues();

        mRecognizeText = new RecognizeText(context, listener);
        mRecognizeFormula = new RecognizeFormula(context, listener);
        mRecognizeQRCode = new RecognizeQRCode(context, listener);
        mRecognizeHandwriting = new RecognizeHandwriting(context, listener);
    }

    private void initMapValues() {
        //OCR可识别的语言
        LANGUAGE_MAP.put("汉英语", "CHN_ENG");  LANGUAGE_MAP.put("中英文", "CHN_ENG");
        LANGUAGE_MAP.put("汉语", "CHN");        LANGUAGE_MAP.put("中文", "CHN");
        LANGUAGE_MAP.put("英语", "ENG");        LANGUAGE_MAP.put("英文", "ENG");
        LANGUAGE_MAP.put("法语", "FRE");        LANGUAGE_MAP.put("法文", "FRE");
        LANGUAGE_MAP.put("德语", "GER");        LANGUAGE_MAP.put("德文", "GER");
        LANGUAGE_MAP.put("俄语", "RUS");        LANGUAGE_MAP.put("俄文", "RUS");
        LANGUAGE_MAP.put("日语", "JAP");        LANGUAGE_MAP.put("日文", "JAP");
        LANGUAGE_MAP.put("韩语", "KOR");        LANGUAGE_MAP.put("韩文", "KOR");
        LANGUAGE_MAP.put("意大利语", "ITA");
        LANGUAGE_MAP.put("西班牙语", "SPA");
        LANGUAGE_MAP.put("葡萄牙语", "POR");
    }

    public void onOCRText(final String imagePath, final boolean question, final String languageType) {
        new Thread() {
            @Override
            public void run() {
                String ocrLanguage = LANGUAGE_MAP.get(languageType);
                if (TextUtils.isEmpty(ocrLanguage)) {
                    ocrLanguage = "CHN_ENG";
                }

                mRecognizeText.setLanguageType(ocrLanguage);
                mRecognizeText.request(imagePath, question);
            }
        }.start();
    }

    public void action(final int type, final String imagePath) {
        new Thread() {
            @Override
            public void run() {
                switch (type) {
                    case FORMULA_TYPE:
                        mRecognizeFormula.request(imagePath);
                        break;
                    case QRCODE_TYPE:
                        mRecognizeQRCode.request(imagePath);
                        break;
                    case HANDWRITING_TYPE:
                        mRecognizeHandwriting.request(imagePath);
                        break;
                }
            }
        }.start();
    }
}
