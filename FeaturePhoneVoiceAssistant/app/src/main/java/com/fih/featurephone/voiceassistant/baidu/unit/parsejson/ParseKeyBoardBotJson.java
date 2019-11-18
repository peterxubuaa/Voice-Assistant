package com.fih.featurephone.voiceassistant.baidu.unit.parsejson;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseKeyBoardBotJson extends ParseJson {
     /*
    Bot
     */
    public static class BotKeyBoardUnit {
        int mErrorCode;
        String mErrorMsg;
        Bot_Result mResult;
    }

    public static class Bot_Result {
        String mVersion;
        String mBotSession;
        String mBotSessionID;
        String mTimestamp;
        String mBotID;
        String mLogID;
        String mInteractionID;
        Result_Response mResponse;
    }

    static class Result_Response {
        int mStatus;
        String mMsg;
        Base_Response_Schema mSchema;
        ArrayList<Base_Response_Action> mActionList;
        Base_Response_QuRes mQuRes;
    }

    public static class BotSimpleResponse {
        public int mErrorCode;
        public String mBotID;
        public String mBotSession;
        public String mBotSessionID;
        public String mRawQuery;
        public SimpleResponseAction mAction;
    }

    static public BotKeyBoardUnit parseBotKeyBoardUnit(String json) {
        BotKeyBoardUnit botKeyBoardUnit = new BotKeyBoardUnit();
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (!jsonObj.isNull("error_code")) {
                botKeyBoardUnit.mErrorCode = jsonObj.getInt("error_code");
            }
            if (!jsonObj.isNull("error_msg")) {
                botKeyBoardUnit.mErrorMsg = jsonObj.getString("error_msg");
            }
            if (!jsonObj.isNull("result")) {
                botKeyBoardUnit.mResult = parseBotResult(jsonObj.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return botKeyBoardUnit;
    }

    static private Bot_Result parseBotResult(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Bot_Result result = new Bot_Result();
        try {
            if (!jsonObject.isNull("version")) {
                result.mVersion = jsonObject.getString("version");
            }
            if (!jsonObject.isNull("timestamp")) {
                result.mTimestamp = jsonObject.getString("timestamp");
            }
            if (!jsonObject.isNull("bot_id")) {
                result.mBotID = jsonObject.getString("bot_id");
            }
            if (!jsonObject.isNull("bot_session")) {
                result.mBotSession = jsonObject.getString("bot_session");
                result.mBotSessionID = getBotSessionID(result.mBotSession);
            }
            if (!jsonObject.isNull("log_id")) {
                result.mLogID = jsonObject.getString("log_id");
            }
            if (!jsonObject.isNull("interaction_id")) {
                result.mInteractionID = jsonObject.getString("interaction_id");
            }
            if (!jsonObject.isNull("response")) {
                result.mResponse = parseResponse(jsonObject.getJSONObject("response"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    static private String getBotSessionID(String botSession) {
        if (TextUtils.isEmpty(botSession)) return null;
        try {
            JSONObject jsonObj = new JSONObject(botSession);
            if (!jsonObj.isNull("session_id")) {
                return jsonObj.getString("session_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    static private Result_Response parseResponse(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Result_Response response = new Result_Response();
        try {
            if (!jsonObject.isNull("status")) {
                response.mStatus = jsonObject.getInt("status");
            }
            if (!jsonObject.isNull("msg")) {
                response.mMsg = jsonObject.getString("msg");
            }
            if (!jsonObject.isNull("schema")) {
                response.mSchema = parseSchema(jsonObject.getJSONObject("schema"));
            }
            if (!jsonObject.isNull("qu_res")) {
                response.mQuRes = parseQuRes(jsonObject.getJSONObject("qu_res"));
            }
            if (!jsonObject.isNull("action_list")) {
                response.mActionList = parseActionList(jsonObject.getJSONArray("action_list"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    static public BotSimpleResponse getBotSimpleResponse(String query, BotKeyBoardUnit botKeyBoardUnit) {
        if (null == botKeyBoardUnit) return null;

        BotSimpleResponse botSimpleResponse = new BotSimpleResponse();
        botSimpleResponse.mRawQuery = query;
        botSimpleResponse.mErrorCode = botKeyBoardUnit.mErrorCode;
        if (null != botKeyBoardUnit.mResult) {
            botSimpleResponse.mBotID = botKeyBoardUnit.mResult.mBotID;
            botSimpleResponse.mBotSession = botKeyBoardUnit.mResult.mBotSession;
            botSimpleResponse.mBotSessionID = botKeyBoardUnit.mResult.mBotSessionID;

            botSimpleResponse.mAction = new SimpleResponseAction();
            botSimpleResponse.mAction.mBotID = botSimpleResponse.mBotID;
            if (null != botKeyBoardUnit.mResult.mResponse.mSchema) {
                botSimpleResponse.mAction.mIntent = botKeyBoardUnit.mResult.mResponse.mSchema.mIntent;
                botSimpleResponse.mAction.mIntentConfidence = botKeyBoardUnit.mResult.mResponse.mSchema.mIntentConfidence;
            }
            if (null != botKeyBoardUnit.mResult.mResponse.mActionList && botKeyBoardUnit.mResult.mResponse.mActionList.size() > 0) {
                botSimpleResponse.mAction.mType = botKeyBoardUnit.mResult.mResponse.mActionList.get(0).mType;
                botSimpleResponse.mAction.mSay = botKeyBoardUnit.mResult.mResponse.mActionList.get(0).mSay;
                botSimpleResponse.mAction.mActionID = botKeyBoardUnit.mResult.mResponse.mActionList.get(0).mActionID;
                botSimpleResponse.mAction.mConfidence = botKeyBoardUnit.mResult.mResponse.mActionList.get(0).mConfidence;
            }
        }

        return botSimpleResponse;
    }

/*    public static ArrayList<Base_Response_Schema_Slots> getSchemaSlots(BotKeyBoardUnit botKeyBoardUnit) {
        if (null == botKeyBoardUnit || null == botKeyBoardUnit.mResult
                || null == botKeyBoardUnit.mResult.mResponse
                || null == botKeyBoardUnit.mResult.mResponse.mSchema) return null;

        return botKeyBoardUnit.mResult.mResponse.mSchema.mSlots;
    }*/
}
