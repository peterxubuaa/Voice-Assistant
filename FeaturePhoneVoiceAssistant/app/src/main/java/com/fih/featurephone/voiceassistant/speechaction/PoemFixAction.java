package com.fih.featurephone.voiceassistant.speechaction;

import android.content.Context;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class PoemFixAction extends FixBaseAction {
    private String[] KEYWORD_POEM_START;
    private String[] KEYWORD_POEM_STOP;

    private Context mContext;

    public PoemFixAction(Context context) {
        mBotID = BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_POEM;
        mContext = context;
        KEYWORD_POEM_START = mContext.getResources().getStringArray(R.array.poem_action_start_keyword);
        KEYWORD_POEM_STOP = mContext.getResources().getStringArray(R.array.poem_action_stop_keyword);
    }

    @Override
    public int checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!mActive && CommonUtil.isEqualsKeyWord(query, KEYWORD_POEM_START)) {
            mActive = true;
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_fix_poem_start);
            bestResponse.mHint = mContext.getString(R.string.baidu_unit_fix_poem_help);
            return START_FIX_ACTION;
        }

        if (mActive && CommonUtil.isEqualsKeyWord(query, KEYWORD_POEM_STOP)) {
            mActive = false;
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_fix_poem_stop);
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
