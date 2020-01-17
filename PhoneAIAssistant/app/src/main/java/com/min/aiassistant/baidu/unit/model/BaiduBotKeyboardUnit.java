package com.min.aiassistant.baidu.unit.model;

import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.BaiduBaseModel;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.baidu.unit.parsejson.ParseKeyBoardBotJson;
import com.min.aiassistant.speechaction.BaseAction;
import com.min.aiassistant.speechaction.FixBaseAction;
import com.min.aiassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaiduBotKeyboardUnit extends BaiduBaseModel {

    private BaiduUnitAI mBaiduUnitAI;
    private BaiduUnitAI.BestResponse mBestResponse;
    private String mAccessToken;
    private ArrayList<String> mBotTypeList;
    private BaiduBaseAI.IBaiduBaseListener mUnitListener;
    private ArrayList<FixBaseAction> mFixActionList;
    private ArrayList<BaseAction> mLocalActionList;

    public BaiduBotKeyboardUnit(BaiduUnitAI baiduUnitAI, BaiduBaseAI.IBaiduBaseListener listener, BaiduUnitAI.BestResponse bestResponse,
                         ArrayList<String> botTypeList, ArrayList<FixBaseAction> fixActionList, ArrayList<BaseAction> localActionList) {
        super(baiduUnitAI.mContext, listener);
        mBaiduUnitAI = baiduUnitAI;
        mBestResponse = bestResponse;
        mBotTypeList = botTypeList;
        mUnitListener = listener;
        mFixActionList = fixActionList;
        mLocalActionList = localActionList;
    }

    public void getBaiduKeyBoardBotUnit(String query) {
        mBestResponse.mAnswer = "";
        mBestResponse.mHint = "";
        if (!triggerFixAction(query)) {
            if (!triggerLocalAction(query)) {
                triggerWebAction(query);
            }
        }

        String[] results = new String[3];
        results[0] = !TextUtils.isEmpty(mBestResponse.mBotIDLabel)? query + "(" + mBestResponse.mBotIDLabel + ")" : query;//question
        results[1] = !TextUtils.isEmpty(mBestResponse.mAnswer)? mBestResponse.mAnswer : mContext.getString(R.string.baidu_unit_answer_fail);//answer
        results[2] = mBestResponse.mHint;//hint
        if (null != mUnitListener) mUnitListener.onFinalResult(results, BaiduUnitAI.UNIT_RESULT);
    }

    private boolean triggerFixAction(String query) {
        FixBaseAction selFixBaseAction = null;
        for (FixBaseAction fixBaseAction : mFixActionList) {
            int result = fixBaseAction.checkAction(query, mBestResponse);
            switch (result) {
                case FixBaseAction.NONE_FIX_ACTION:
                    continue;
                case FixBaseAction.START_FIX_ACTION:
                case FixBaseAction.STOP_FIX_ACTION:
                    Object[] notify = new Object[2];
                    notify[0] = fixBaseAction.getBotID(); //string
                    notify[1] = result; //integer
                    if (null != mUnitListener) mUnitListener.onFinalResult(notify, BaiduUnitAI.UNIT_NOTIFY);
                    return true;
                case FixBaseAction.RUNNING_FIX_ACTION:
                    selFixBaseAction = fixBaseAction;
                    break;
            }
            if (null != selFixBaseAction) {
                String[] queryList = selFixBaseAction.getAllQuestions(query);
                if (queryList.length <= 1) {
                    triggerBaiduKeyBoardBotUnit(selFixBaseAction.getAdjustQuery(query), selFixBaseAction.getBotID());
                } else {
                    BaiduUnitAI.BestResponse sumBestResponse = new BaiduUnitAI.BestResponse();
                    sumBestResponse.mAnswer = "";
                    sumBestResponse.mRawQuery = query;
                    for (String question : queryList) {
                        triggerBaiduKeyBoardBotUnit(selFixBaseAction.getAdjustQuery(question), selFixBaseAction.getBotID());
                        if (TextUtils.isEmpty(mBestResponse.mAnswer)) {
                            sumBestResponse.mAnswer += "《" + question + "》";
                        } else {
                            sumBestResponse.mAnswer += mBestResponse.mAnswer;
                        }
                    }
                    mBestResponse.mAnswer = sumBestResponse.mAnswer;
                    mBestResponse.mRawQuery = sumBestResponse.mRawQuery;
                }
                mBestResponse.mBotID = selFixBaseAction.getBotID();
                mBestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(mBestResponse.mBotID);
                return true;
            }
        }

        return false;
    }

    private boolean triggerLocalAction(String query) {
        for (BaseAction baseAction : mLocalActionList) {
            if (baseAction.checkAction(query, mBestResponse)) return true;
        }

        return false;
    }

    private void triggerWebAction(String query) {
        seekBaiduKeyBoardBotUnit(query);
    }

    private void seekBaiduKeyBoardBotUnit(String query) {
        //1. 通过关键词，重置对话
        if (!TextUtils.isEmpty(mBestResponse.mBotID) && mBaiduUnitAI.isQuitSession(query)) {
            mBestResponse.mBotID = "";
            mBestResponse.mBotIDLabel = "";
            mBestResponse.mBotSession = "";
            mBestResponse.mAnswer = mContext.getString(R.string.baidu_unit_quit_session);
            mBestResponse.mHint = "";
            return;
        }
        //2. 获取最佳对话类型
        ArrayList<BaiduUnitAI.BestBotID> bestBotIDList = mBaiduUnitAI.getBestBotID(query);
        for (BaiduUnitAI.BestBotID bestBotID : bestBotIDList) {
            if (!TextUtils.isEmpty(bestBotID.mNewQuery)) query = bestBotID.mNewQuery;
            if (!mBotTypeList.contains(bestBotID.mBotID)) continue;

            //3. 获取最佳对话的回答
            if (!bestBotID.mBotID.equals(mBestResponse.mBotID)) {
                //重置对话
                mBestResponse.mBotSession = "";
                mBestResponse.mBotSessionID = "";
            }
            triggerBaiduKeyBoardBotUnit(query, bestBotID.mBotID);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }

        //4. 继续上一次的对话
        if (!TextUtils.isEmpty(mBestResponse.mBotSessionID) || !TextUtils.isEmpty(mBestResponse.mBotSession)) {
            if (!TextUtils.isEmpty(mBestResponse.mBotID)) {
                triggerBaiduKeyBoardBotUnit(query, mBestResponse.mBotID);
                if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
            }
            //重置对话
            mBestResponse.mBotSession = "";
            mBestResponse.mBotSessionID = "";
        }

        //5. 智能回答
        mBestResponse.reset();
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA)) {
            triggerBaiduKeyBoardBotUnit(query, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }

        //6. 闲聊
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT)) {
            triggerBaiduKeyBoardBotUnit(query, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }
        //7. fail
        mBestResponse.reset();
        mBestResponse.mAnswer = mContext.getString(R.string.baidu_unit_fail_session);
    }

    private void triggerBaiduKeyBoardBotUnit(String query, String botID) {
        // 请求URL
        final String TALKURL = "https://aip.baidubce.com/rpc/2.0/unit/bot/chat";
        //请求的参数用map封装
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapRequest = new HashMap<>();
        Map<String, Object> mapQueryInfo = new HashMap<>();
        //技能唯一标识，在『我的技能』的技能列表中第一列数字即为bot_id
        map.put("version", "2.0");
        map.put("bot_id", botID); // 技能id

        if (TextUtils.isEmpty(mBestResponse.mBotSessionID)) {
            map.put("bot_session", mBestResponse.mBotSession);
        } else {
            map.put("bot_session", "{\"session_id\":\"" + mBestResponse.mBotSessionID + "\"}");
        }
        map.put("log_id", UUID.randomUUID().toString().replaceAll("-", ""));
        map.put("request", mapRequest);
        //系统自动发现不置信意图/词槽，
        //并据此主动发起澄清确认的敏感程度。
        //取值范围：0(关闭)、1(中敏感度)、2(高敏感度)。
        //取值越高BOT主动发起澄清的频率就越高，建议值为1
        mapRequest.put("bernard_level", 1);
        mapRequest.put("query", query);
        mapRequest.put("query_info", mapQueryInfo);
        mapRequest.put("user_id", "UNIT_WEB_6666");
        // 请求信息来源，可选值："ASR","KEYBOARD"。ASR为语音输入，KEYBOARD为键盘文本输入。针对ASR输入，UNIT平台内置了纠错机制，会尝试解决语音输入中的一些常见错误
        mapQueryInfo.put("source", "KEYBOARD");
        mapQueryInfo.put("type", "TEXT");
        try {
            // 请求参数
            String json = new JSONObject(map).toString();
            if (TextUtils.isEmpty(mAccessToken)) mAccessToken = getAuthToken();//for keyboard unit
            String result = HttpUtil.post(TALKURL, mAccessToken, "application/json", json);
            ParseKeyBoardBotJson.BotKeyBoardUnit botKeyBoardUnit = ParseKeyBoardBotJson.getInstance().parse(result);
            ParseKeyBoardBotJson.BotSimpleResponse botSimpleResponse = ParseKeyBoardBotJson.getInstance().getBotSimpleResponse(query, botKeyBoardUnit);
            setBestResponseFromBotUnit(botSimpleResponse);
        } catch (Exception e) {
            mBestResponse.reset();
            e.printStackTrace();
        }
    }

    private void setBestResponseFromBotUnit(ParseKeyBoardBotJson.BotSimpleResponse botSimpleResponse) {
        if (null == botSimpleResponse || botSimpleResponse.mErrorCode != 0) {
            mBestResponse.reset();
            return;
        }

        if ("satisfy".equals(botSimpleResponse.mAction.mType) || "chat".equals(botSimpleResponse.mAction.mType)
                || "clarify".equals(botSimpleResponse.mAction.mType)) {
            mBestResponse.mBotID = botSimpleResponse.mBotID;
            mBestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(mBestResponse.mBotID);
            mBestResponse.mAnswer = botSimpleResponse.mAction.mSay;
            if ("chat".equals(botSimpleResponse.mAction.mType)) {
                mBestResponse.mBotSession = "";
                mBestResponse.mBotSessionID = "";
            } else {
                mBestResponse.mBotSession = botSimpleResponse.mBotSession;
                mBestResponse.mBotSessionID = botSimpleResponse.mBotSessionID;
            }
            mBestResponse.mRawQuery = botSimpleResponse.mRawQuery;
        } else {
            mBestResponse.mBotID = "";
            mBestResponse.mBotIDLabel = "";
            mBestResponse.mAnswer = "";
            mBestResponse.mHint = "";
        }
    }
}
