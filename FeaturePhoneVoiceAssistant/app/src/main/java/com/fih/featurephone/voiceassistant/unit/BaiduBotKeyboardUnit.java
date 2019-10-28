package com.fih.featurephone.voiceassistant.unit;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.unit.parsejson.ParseKeyBoardBotJson;
import com.fih.featurephone.voiceassistant.speechaction.BaseAction;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BaiduBotKeyboardUnit {

    private BaiduUnitAI mBaiduUnitAI;
    private BaiduUnitAI.BestResponse mBestResponse;
    private String mAuthentication;
    private ArrayList<String> mBotTypeList;
    private BaiduUnitAI.onUnitListener mUnitListener;
    private ArrayList<BaseAction> mLocalActionList;

    BaiduBotKeyboardUnit(BaiduUnitAI baiduUnitAI, BaiduUnitAI.BestResponse bestResponse,
                         ArrayList<String> botTypeList, BaiduUnitAI.onUnitListener unitListener,
                         ArrayList<BaseAction> localActionList) {
        mBaiduUnitAI = baiduUnitAI;
        mBestResponse = bestResponse;
        mBotTypeList = botTypeList;
        mUnitListener = unitListener;
        mLocalActionList = localActionList;
    }

    BaiduUnitAI.BestResponse getBaiduKeyBoardBotUnit(String query) {
        if (null != mUnitListener) mUnitListener.onShowDebugInfo("上次最佳回应 -> " + mBestResponse.toString() + "\n\n", true);
        if (!triggerLocalAction(query)) {
            triggerWebAction(query);
        }
        if (null != mUnitListener) mUnitListener.onFinalResult(
                query + "(" + (mBestResponse.mBotIDLabel) + ")",
                        mBestResponse.mAnswer, mBestResponse.mHint);
        if (null != mUnitListener) mUnitListener.onShowDebugInfo("本次最佳回应 -> " + mBestResponse.toString() + "\n\n", false);
        return mBestResponse;
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
            mBestResponse.mAnswer = mBaiduUnitAI.mContext.getString(R.string.baidu_unit_quit_session);
            return;
        }
        //2. 获取最佳对话类型
        String[] result = mBaiduUnitAI.getBestBotID(query);
        String botID = result[0];
        if (!TextUtils.isEmpty(result[1])) query = result[1];
        if (!mBotTypeList.contains(botID)) botID = null;

        if (TextUtils.isEmpty(botID) ){
            //继续上一次的对话
            if (!TextUtils.isEmpty(mBestResponse.mBotSessionID) || !TextUtils.isEmpty(mBestResponse.mBotSession)) {
                botID = mBestResponse.mBotID;
            }
        } else {
            //重置对话
            if (!TextUtils.isEmpty(mBestResponse.mBotSessionID) || !TextUtils.isEmpty(mBestResponse.mBotSession)) {
                mBestResponse.mBotSession = "";
                mBestResponse.mBotSessionID = "";
            }
        }
        //3. 获取最佳对话的回答
        if (!TextUtils.isEmpty(botID)) {
            triggerBaiduKeyBoardBotUnit(query, botID);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }

        //4. 智能回答
        mBestResponse.reset();
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA)) {
            triggerBaiduKeyBoardBotUnit(query, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }
        //5. 从备选bot中逐个对话，找到满意回答
//        for (String bakBotID : mBotTypeList) {
//            triggerBaiduKeyBoardBotUnit(query, bakBotID);
//            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
//        }

        //6. 闲聊
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT)) {
            triggerBaiduKeyBoardBotUnit(query, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT);
            if (!TextUtils.isEmpty(mBestResponse.mAnswer)) return;
        }
        //7. fail
        mBestResponse.reset();
        mBestResponse.mAnswer = mBaiduUnitAI.mContext.getString(R.string.baidu_unit_fail_session);
    }

    private void triggerBaiduKeyBoardBotUnit(String query, String botID) {
        // 请求URL
        final String TALKURL = "https://aip.baidubce.com/rpc/2.0/unit/bot/chat";
        //请求的参数用map封装
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> mapRequest = new HashMap<String, Object>();
        Map<String, Object> mapQueryInfo = new HashMap<String, Object>();
        /*
         *  技能唯一标识，在『我的技能』的技能列表中第一列数字即为bot_id
         */
        map.put("version", "2.0");
        map.put("bot_id", botID); // 技能id

        if (TextUtils.isEmpty(mBestResponse.mBotSessionID)) {
            map.put("bot_session", mBestResponse.mBotSession);
        } else {
//            "{\"session_id\":\"value\"}"
            map.put("bot_session", "{\"session_id\":\"" + mBestResponse.mBotSessionID + "\"}");
//          Map<String, Object> mapBotSession = new HashMap<>();
//            map.put("bot_session", mapBotSession);
//            mapBotSession.put("session_id", mBestResponse.mBotSessionID);
        }
        map.put("log_id", UUID.randomUUID().toString().replaceAll("-", ""));
        map.put("request", mapRequest);
        /*
         *  系统自动发现不置信意图/词槽，
         *  并据此主动发起澄清确认的敏感程度。
         *  取值范围：0(关闭)、1(中敏感度)、2(高敏感度)。
         *  取值越高BOT主动发起澄清的频率就越高，建议值为1
         */
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
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("请求参数 -> " + json + "\n\n", false);
            if (TextUtils.isEmpty(mAuthentication)) mAuthentication = mBaiduUnitAI.getAuth();//for keyboard unit
            String result = HttpUtil.post(TALKURL, mAuthentication, "application/json", json);
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("返回结果 -> " + result + "\n\n", false);
            ParseKeyBoardBotJson.BotKeyBoardUnit botKeyBoardUnit = ParseKeyBoardBotJson.parseBotKeyBoardUnit(result);
            ParseKeyBoardBotJson.BotSimpleResponse botSimpleResponse = ParseKeyBoardBotJson.getBotSimpleResponse(query, botKeyBoardUnit);
            setBestResponseFromBotUnit(botSimpleResponse);
//            if (mBaiduUnitAI.isLocalEvent(mBestResponse.mBotID)) {
//                ArrayList<ParseJson.Base_Response_Schema_Slots> slotList = ParseKeyBoardBotJson.getSchemaSlots(botKeyBoardUnit);
//                mBaiduUnitAI.triggerLocalEvent(botSimpleResponse.mAction, slotList, mBestResponse);
//            }
        } catch (Exception e) {
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
        }
    }
}
