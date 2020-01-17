package com.min.aiassistant.speechaction;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.ocr.BaiduOcrAI;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.GlobalValue;

public class RecognizeAction implements BaseAction {
    private String[] REGEX_RECOGNIZE;
    private String[] OCR_TYPE;
    private String[] OBJECT_TYPE;
    private String[] FACE_TYPE;

    private Context mContext;

    public RecognizeAction(Context context) {
        mContext = context;
        REGEX_RECOGNIZE = mContext.getResources().getStringArray(R.array.recognize_regex);
        OCR_TYPE = mContext.getResources().getStringArray(R.array.ocr_language_item);
        OBJECT_TYPE = mContext.getResources().getStringArray(R.array.classify_image_type_item);
        FACE_TYPE = mContext.getResources().getStringArray(R.array.face_type_item);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);

        String target = CommonUtil.getRegexMatch(query, REGEX_RECOGNIZE, 1);
        if (null == target) return false;

        if (CommonUtil.isEqualsKeyWord(target, OCR_TYPE)) {
            bestResponse.reset();
            Intent intent = new Intent(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA);
            intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, target);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_ocr_start), target);
        } else if (CommonUtil.isEqualsKeyWord(target, OBJECT_TYPE)) {
            bestResponse.reset();
            int objectType = 0;
            for (int i = 0; i < OBJECT_TYPE.length; i++) {
                if (target.equals(OBJECT_TYPE[i])) {
                    objectType = i;
                    break;
                }
            }
            Intent intent = new Intent(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA);
            intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, objectType);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_classify_image_start), target);
        } else if (CommonUtil.isEqualsKeyWord(target, FACE_TYPE)) {
            bestResponse.reset();
            Intent intent = new Intent(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            bestResponse.mAnswer = mContext.getString(R.string.baidu_face_identify_start);
        } else {
            return false;
        }

        return true;
    }
}
