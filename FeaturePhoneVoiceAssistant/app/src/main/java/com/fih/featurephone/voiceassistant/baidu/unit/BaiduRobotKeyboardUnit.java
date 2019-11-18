package com.fih.featurephone.voiceassistant.baidu.unit;

import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.baidu.unit.parsejson.ParseKeyBoardRobotJson;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

class BaiduRobotKeyboardUnit {
    private final BaiduUnitAI mBaiduUnitAI;
    private final BaiduUnitAI.BestResponse mBestResponse;
    private String mAuthentication;
    private int mRobotType;
    private BaiduUnitAI.onUnitListener mUnitListener;

    BaiduRobotKeyboardUnit(BaiduUnitAI baiduUnitAI, BaiduUnitAI.BestResponse bestResponse,
                           int robotType, BaiduUnitAI.onUnitListener unitListener) {
        mBaiduUnitAI = baiduUnitAI;
        mBestResponse = bestResponse;
        mRobotType = robotType;
        mUnitListener = unitListener;
    }

    void getBaiduKeyBoardRobotUnit(String query) {
        final String WBB_ROBOT_ID = "S22464";
        final String LOCAL_ROBOT_ID = "S22856";
        final String ALL_ROBOT_ID = "S23003";

        // 请求URL
        String talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/service/chat";
        //请求的参数用map封装
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> mapDialogState = new HashMap<String, Object>();
        Map<String, Object> mapSysRememberedSkills = new HashMap<String, Object>();
        Map<String, Object> mapRequest = new HashMap<String, Object>();
        Map<String, Object> mapQueryInfo = new HashMap<String, Object>();

        map.put("version", "2.0");
        switch (mRobotType) {
            case BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_WEB:
                map.put("service_id", WBB_ROBOT_ID);
                break;
            case BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL:
                map.put("service_id", LOCAL_ROBOT_ID);
                break;
            case BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_ALL:
                map.put("service_id", ALL_ROBOT_ID);
                break;
        }
        map.put("log_id", UUID.randomUUID().toString().replaceAll("-", ""));
//        map.put("session", mBestResponse.mSession);
        map.put("session_id", mBestResponse.mSessionID);
        map.put("dialog_state", mapDialogState);
        mapDialogState.put("contexts", mapSysRememberedSkills);
        ArrayList<String> rememberedSkills = new ArrayList<String>();
        rememberedSkills.add("81459");
        rememberedSkills.add("81485");
        mapSysRememberedSkills.put("SYS_REMEMBERED_SKILLS", rememberedSkills);

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
        mapRequest.put("user_id", "UNIT_WEB_8888");
        // 请求信息来源，可选值："ASR","KEYBOARD"。ASR为语音输入，KEYBOARD为键盘文本输入。针对ASR输入，UNIT平台内置了纠错机制，会尝试解决语音输入中的一些常见错误
        mapQueryInfo.put("source", "KEYBOARD");
        mapQueryInfo.put("type", "TEXT");
        try {
            // 请求参数
            String json = new JSONObject(map).toString();
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("上次最佳回应 -> " + mBestResponse.toString() + "\n\n", true);
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("请求参数 -> " + json + "\n\n", false);
            if (TextUtils.isEmpty(mAuthentication)) mAuthentication = mBaiduUnitAI.getAuth();//for keyboard unit
            String result = HttpUtil.post(talkUrl, mAuthentication, "application/json", json);
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("返回结果 -> " + result + "\n\n", false);
            ParseKeyBoardRobotJson.RobotKeyBoardUnit robotKeyBoardUnit = ParseKeyBoardRobotJson.parseRobotKeyBoardUnit(result);
            ParseKeyBoardRobotJson.RobotSimpleResponse robotSimpleResponse = ParseKeyBoardRobotJson.getRobotSimpleResponse(query, robotKeyBoardUnit);
            BaiduUnitAI.BestResponse bestResponse = getBestResponseFromRobotUnit(robotSimpleResponse);
            if (null != mUnitListener) mUnitListener.onShowDebugInfo("本次最佳回应 -> " + mBestResponse.toString() + "\n\n", false);
            if (null != mUnitListener) mUnitListener.onFinalResult(query + "(" + (bestResponse.mBotIDLabel) + ")",
                    bestResponse.mAnswer, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BaiduUnitAI.BestResponse getBestResponseFromRobotUnit(ParseKeyBoardRobotJson.RobotSimpleResponse robotSimpleResponse) {
        if (null == robotSimpleResponse || robotSimpleResponse.mErrorCode != 0) return null;

        String botID = "";
        for (ParseKeyBoardRobotJson.SimpleResponseSkillState skillStates : robotSimpleResponse.mSkillStateList) {
            if (!TextUtils.isEmpty(skillStates.mName)) {
                botID = skillStates.mBotID;
                break;
            }
        }

        boolean bHasSatisfy = false;
        BaiduUnitAI.BestResponse chatResponse = null;
        for (ParseKeyBoardRobotJson.SimpleResponseAction action : robotSimpleResponse.mActionList) {
            if ("satisfy".equals(action.mType)) {
                bHasSatisfy = true;
                mBestResponse.mBotID = action.mBotID;
                mBestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(mBestResponse.mBotID);
                mBestResponse.mAnswer = action.mSay;
                mBestResponse.mSessionID = robotSimpleResponse.mSessionID;
                mBestResponse.mRawQuery = robotSimpleResponse.mRawQuery;
                if (!TextUtils.isEmpty(botID) && botID.equals(action.mBotID)) {
                    return mBestResponse;
                }
            } else if ("chat".equals(action.mType)) {
                chatResponse = new BaiduUnitAI.BestResponse();
                chatResponse.mBotID = action.mBotID;
                chatResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(mBestResponse.mBotID);
                chatResponse.mAnswer = action.mSay;
                chatResponse.mSessionID = robotSimpleResponse.mSessionID;
                chatResponse.mRawQuery = robotSimpleResponse.mRawQuery;
            }
        }

        if (!bHasSatisfy) {
            if (null != chatResponse) {
                //not get best response
                mBestResponse.mBotID = chatResponse.mBotID;
                mBestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(mBestResponse.mBotID);
                mBestResponse.mAnswer = chatResponse.mAnswer;
                mBestResponse.mSessionID = chatResponse.mSessionID;
                mBestResponse.mRawQuery = chatResponse.mRawQuery;
            } else {
                mBestResponse.reset();
                mBestResponse.mAnswer = "我不知道这个，请再问一次";
            }
        }
        return mBestResponse;
    }
}
