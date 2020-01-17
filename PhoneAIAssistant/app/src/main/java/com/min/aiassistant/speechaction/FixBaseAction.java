package com.min.aiassistant.speechaction;

import com.min.aiassistant.baidu.unit.BaiduUnitAI;

public class FixBaseAction {
    public static final int NONE_FIX_ACTION = 0;
    public static final int START_FIX_ACTION = 1;
    public static final int STOP_FIX_ACTION = 2;
    public static final int RUNNING_FIX_ACTION = 3;

    String mBotID = "";
    boolean mActive = false;

    public int checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        return NONE_FIX_ACTION;
    }

    public String getBotID() {
        return mBotID;
    }

    public String getAdjustQuery(String query) {
        return query;
    }

    public String[] getAllQuestions(String query) {
        return new String[] {query};
    }

    public void forceAction(boolean active) {
        mActive = active;
    }
}
