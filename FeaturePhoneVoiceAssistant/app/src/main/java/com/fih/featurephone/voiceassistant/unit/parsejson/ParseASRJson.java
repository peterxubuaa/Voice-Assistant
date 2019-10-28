package com.fih.featurephone.voiceassistant.unit.parsejson;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseASRJson extends ParseJson {

    public static class ASRUnit {
        int mErrNo;
        ArrayList<BotSession> mBotSessionList;
        ArrayList<UnitResponse> mUnitResponse;
    }

    public static class BotSession {
        public String mBotID;
        public String mBotSessionID;
    }

    static class UnitResponse {
        String mVersion;
        String mTimestamp;
        UnitResponse_Response mResponse;
        String mBotID;
        String mLogID;
        String mBotSession;
        String mInteractionID;
    }

    static class UnitResponse_Response {
        String mMsg;
        Base_Response_Schema mSchema;
        Base_Response_QuRes mQuRes;
        ArrayList<Base_Response_Action> mActionList;
        int mStatus;
    }

    static public ASRUnit parseASRResponse(String json) {
        ASRUnit asrUnit = new ASRUnit();
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (!jsonObj.isNull("errno")) {
                asrUnit.mErrNo = jsonObj.getInt("errno");
            }
            if (!jsonObj.isNull("bot_session_list")) {
                asrUnit.mBotSessionList = parseBotSessionList(jsonObj.getJSONArray("bot_session_list"));
            }
            if (!jsonObj.isNull("unit_response")) {
                asrUnit.mUnitResponse= parseUnitResponse(jsonObj.getJSONArray("unit_response"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return asrUnit;
    }

    static private ArrayList<BotSession> parseBotSessionList(JSONArray jsonArray) {
        if (null == jsonArray) return null;
        ArrayList<BotSession> botSessionList = new ArrayList<BotSession>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                BotSession botSession = new BotSession();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("bot_id")) {
                    botSession.mBotID = jsonObject.getString("bot_id");
                }
                if (!jsonObject.isNull("bot_session_id")) {
                    botSession.mBotSessionID = jsonObject.getString("bot_session_id");
                }
                botSessionList.add(botSession);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return botSessionList;
    }

    static private ArrayList<UnitResponse> parseUnitResponse(JSONArray jsonArray) {
        if (null == jsonArray) return null;
        ArrayList<UnitResponse> unitResponsesList = new ArrayList<UnitResponse>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                UnitResponse unitResponse = new UnitResponse();
                if (!jsonObject.isNull("version")) {
                    unitResponse.mVersion = jsonObject.getString("version");
                }
                if (!jsonObject.isNull("timestamp")) {
                    unitResponse.mTimestamp = jsonObject.getString("timestamp");
                }
                if (!jsonObject.isNull("bot_id")) {
                    unitResponse.mBotID = jsonObject.getString("bot_id");
                }
                if (!jsonObject.isNull("log_id")) {
                    unitResponse.mLogID = jsonObject.getString("log_id");
                }
                if (!jsonObject.isNull("bot_session")) {
                    unitResponse.mBotSession = jsonObject.getString("bot_session");
                }
                if (!jsonObject.isNull("interaction_id")) {
                    unitResponse.mInteractionID = jsonObject.getString("interaction_id");
                }
                if (!jsonObject.isNull("response")) {
                    unitResponse.mResponse = parseResponse(jsonObject.getJSONObject("response"));
                }
                unitResponsesList.add(unitResponse);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return unitResponsesList;
    }

    static private UnitResponse_Response parseResponse(JSONObject jsonObject) {
        UnitResponse_Response response = new UnitResponse_Response();
        try {
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
            if (!jsonObject.isNull("status")) {
                response.mStatus = jsonObject.getInt("status");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static class BotSimpleResponse {
        int mErrorCode;
        public String mRawQuery;
        public ArrayList<BotSession> mBotSessionList;
        public ArrayList<SimpleResponseAction> mActionList;
    }

    public static BotSimpleResponse getBotSimpleResponse(ASRUnit asrUnit) {
        if (null == asrUnit || 0 != asrUnit.mErrNo) return null;

        BotSimpleResponse botSimpleResponse = new BotSimpleResponse();
        botSimpleResponse.mErrorCode = asrUnit.mErrNo;

        if (null != asrUnit.mBotSessionList) {
            botSimpleResponse.mBotSessionList = new ArrayList<BotSession>();
            for (BotSession botSession : asrUnit.mBotSessionList) {
                if (TextUtils.isEmpty(botSession.mBotID)) continue;
                botSimpleResponse.mBotSessionList.add(botSession);
            }
        }

        if (null != asrUnit.mUnitResponse) {
            botSimpleResponse.mActionList = new ArrayList<SimpleResponseAction>();
            for (UnitResponse unitResponse : asrUnit.mUnitResponse) {
                SimpleResponseAction simpleResponseAction = new SimpleResponseAction();
                simpleResponseAction.mBotID = unitResponse.mBotID;
                if (null != unitResponse.mResponse) {
                    if (null != unitResponse.mResponse.mActionList && unitResponse.mResponse.mActionList.size() > 0) {
                        simpleResponseAction.mActionID = unitResponse.mResponse.mActionList.get(0).mActionID;
                        simpleResponseAction.mSay = unitResponse.mResponse.mActionList.get(0).mSay;
                        simpleResponseAction.mType = unitResponse.mResponse.mActionList.get(0).mType;
                        simpleResponseAction.mConfidence = unitResponse.mResponse.mActionList.get(0).mConfidence;
                    }
                    if (null != unitResponse.mResponse.mSchema) {
                        simpleResponseAction.mIntent = unitResponse.mResponse.mSchema.mIntent;
                        simpleResponseAction.mIntentConfidence = unitResponse.mResponse.mSchema.mIntentConfidence;
                    }
                }
                if (!"failure".equals(simpleResponseAction.mType)) {
                    botSimpleResponse.mActionList.add(simpleResponseAction);
                }
            }

            for (UnitResponse response : asrUnit.mUnitResponse) {
                if (null != response.mResponse.mQuRes && !TextUtils.isEmpty(response.mResponse.mQuRes.mRawQuery)) {
                    botSimpleResponse.mRawQuery = response.mResponse.mQuRes.mRawQuery;
                    break;
                }
            }
        }

        return botSimpleResponse;
    }

    public static ArrayList<Base_Response_Schema_Slots> getSchemaSlots(ASRUnit asrUnit, String botID) {
        if (null == asrUnit|| null == asrUnit.mUnitResponse) return null;

        for (UnitResponse unitResponse : asrUnit.mUnitResponse) {
            if (botID.equals(unitResponse.mBotID)) {
                return unitResponse.mResponse.mSchema.mSlots;
            }
        }
        return null;
    }

    public static SimpleResponseAction getBestSimpleResponseAction(BotSimpleResponse botSimpleResponse, String botID) {
        if (null == botSimpleResponse || null == botSimpleResponse.mActionList) return null;

        for (SimpleResponseAction simpleResponseAction : botSimpleResponse.mActionList) {
            if (botID.equals(simpleResponseAction.mBotID)) {
                return simpleResponseAction;
            }
        }
        return null;
    }
}
