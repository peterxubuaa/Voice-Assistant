package com.fih.featurephone.voiceassistant.speechaction;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.fih.featurephone.voiceassistant.MainActivity;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.ocr.BaiduOcrAI;
import com.fih.featurephone.voiceassistant.baidu.ocr.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class OCRAction implements BaseAction {
    private String[] REGEX_OCR_SEARCH;

    private Context mContext;

    public OCRAction(Context context) {
        mContext = context;
        REGEX_OCR_SEARCH = mContext.getResources().getStringArray(R.array.ocr_regex);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);

        String language = CommonUtil.getRegexMatch(query, REGEX_OCR_SEARCH, 1);
        if (null == language) return false;

        if (language.equals("")) {
            language = BaiduOcrAI.OCR_DEFAULT_LANGUAGE;
        }

        bestResponse.reset();
        if (BaiduOcrAI.LANGUAGE_MAP.get(language) != null) {
            Intent intent = new Intent(MainActivity.LOCAL_BROADCAST_LAUNCH_CAMERA);
            intent.putExtra("OCR_LANGUAGE", language);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_ocr_start), language);
        } else {
            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_ocr_not_support), language);
        }

        return true;
    }
}
