package com.min.aiassistant.speechaction;

import android.content.Context;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;

public class CoupletFixAction extends FixBaseAction {
    private String[] KEYWORD_COUPLET_START;
    private String[] KEYWORD_COUPLET_STOP;

    private Context mContext;

    public CoupletFixAction(Context context) {
        mBotID = BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_COUPLET;
        mContext = context;
        KEYWORD_COUPLET_START = mContext.getResources().getStringArray(R.array.couplet_action_start_keyword);
        KEYWORD_COUPLET_STOP = mContext.getResources().getStringArray(R.array.couplet_action_stop_keyword);
    }

    @Override
    public int checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!mActive && CommonUtil.isEqualsKeyWord(query, KEYWORD_COUPLET_START)) {
            mActive = true;
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_fix_couplet_start);
            bestResponse.mHint = mContext.getString(R.string.baidu_unit_fix_couplet_help);
            return START_FIX_ACTION;
        }

        if (mActive && CommonUtil.isEqualsKeyWord(query, KEYWORD_COUPLET_STOP)) {
            mActive = false;
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_fix_couplet_stop);
            bestResponse.mHint = "";
            return STOP_FIX_ACTION;
        }

        if (mActive) {
            bestResponse.mHint = "";
            return RUNNING_FIX_ACTION;
        }

        return NONE_FIX_ACTION;
    }
}
