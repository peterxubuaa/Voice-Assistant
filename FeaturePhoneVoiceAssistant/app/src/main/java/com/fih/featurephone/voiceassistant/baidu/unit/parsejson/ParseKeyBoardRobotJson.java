package com.fih.featurephone.voiceassistant.baidu.unit.parsejson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ParseKeyBoardRobotJson extends ParseJson {
    /*
    Robot
     */
    public static class RobotKeyBoardUnit {
        int mErrorCode;
        Robot_Result mResult;
    }

    public static class Robot_Result {
        String mVersion;
        String mSession;
        String mSessionID;
        String mTimestamp;
        String mServiceID;
        String mLogID;
        String mInteractionID;
        ArrayList<Robot_Result_Response> mResponseList;
        ArrayList<Robot_Result_DialogState_SkillStates> mDialogStateSkillStates;
    }

    public static class Robot_Result_Response {
        int mStatus;
        String mMsg;
        String mOrigin;
        Base_Response_Schema mSchema;
        ArrayList<Base_Response_Action> mActionList;
        Base_Response_QuRes mQuRes;
    }

    public static class Robot_Result_DialogState_SkillStates {
        String mKey; //bot id
        ArrayList<Result_DialogState_SkillStates_Intents> mIntents;
        JSONObject mContext;
        JSONObject mUserSlots;
    }

    public static class Result_DialogState_SkillStates_Intents {
        String mName;
        int mIndex;
    }

    static public RobotKeyBoardUnit parseRobotKeyBoardUnit(String json) {
        RobotKeyBoardUnit keyBoardUnit = new RobotKeyBoardUnit();
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (!jsonObj.isNull("error_code")) {
                keyBoardUnit.mErrorCode = jsonObj.getInt("error_code");
            }
            if (!jsonObj.isNull("result")) {
                keyBoardUnit.mResult = parseResult(jsonObj.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return keyBoardUnit;
    }


    public static class SimpleResponseSkillState {
        public String mBotID;
        public String mName;
        int mIndex;
    }

    public static class RobotSimpleResponse {
        public int mErrorCode;
        String mServiceID;
        public String mSessionID;
        public String mRawQuery;
        public ArrayList<SimpleResponseAction> mActionList;
        public ArrayList<SimpleResponseSkillState> mSkillStateList;
    }

    static public RobotSimpleResponse getRobotSimpleResponse(String query, RobotKeyBoardUnit robotKeyBoardUnit) {
        if (null == robotKeyBoardUnit) return null;

        RobotSimpleResponse simpleResponse = new RobotSimpleResponse();
        simpleResponse.mRawQuery = query;
        simpleResponse.mErrorCode = robotKeyBoardUnit.mErrorCode;
        simpleResponse.mServiceID = robotKeyBoardUnit.mResult.mServiceID;
        simpleResponse.mSessionID = robotKeyBoardUnit.mResult.mSessionID;

        simpleResponse.mActionList = new ArrayList<SimpleResponseAction>();
        for (int i = 0; i < robotKeyBoardUnit.mResult.mResponseList.size(); i++) {
            Robot_Result_Response resultResponse = robotKeyBoardUnit.mResult.mResponseList.get(i);
            if (null == resultResponse) continue;

            SimpleResponseAction simpleResponseAction = new SimpleResponseAction();
            simpleResponseAction.mBotID = resultResponse.mOrigin;
            if (null != resultResponse.mSchema) {
                simpleResponseAction.mIntent = resultResponse.mSchema.mIntent;
                simpleResponseAction.mIntentConfidence = resultResponse.mSchema.mIntentConfidence;
            }
            if (null != resultResponse.mActionList && resultResponse.mActionList.size() > 0) {
                simpleResponseAction.mActionID = resultResponse.mActionList.get(0).mActionID;
                simpleResponseAction.mConfidence = resultResponse.mActionList.get(0).mConfidence;
                simpleResponseAction.mSay = resultResponse.mActionList.get(0).mSay;
                simpleResponseAction.mType = resultResponse.mActionList.get(0).mType;
            }
            simpleResponse.mActionList.add(simpleResponseAction);
        }

        simpleResponse.mSkillStateList = new ArrayList<SimpleResponseSkillState>();
        for (int i = 0; i < robotKeyBoardUnit.mResult.mDialogStateSkillStates.size(); i++) {
            Robot_Result_DialogState_SkillStates skillStates = robotKeyBoardUnit.mResult.mDialogStateSkillStates.get(i);
            if (null == skillStates) continue;
            SimpleResponseSkillState simpleResponseSkillState = new SimpleResponseSkillState();
            simpleResponseSkillState.mBotID = skillStates.mKey;
            if (null != skillStates.mIntents && skillStates.mIntents.size() > 0) {
                simpleResponseSkillState.mName = skillStates.mIntents.get(0).mName;
                simpleResponseSkillState.mIndex = skillStates.mIntents.get(0).mIndex;
            }
            simpleResponse.mSkillStateList.add(simpleResponseSkillState);
        }
        return simpleResponse;
    }

    static private Robot_Result parseResult(JSONObject jsonObject) {
        if (null == jsonObject) return null;

        Robot_Result result = new Robot_Result();
        try {
            if (!jsonObject.isNull("version")) {
                result.mVersion = jsonObject.getString("version");
            }
            if (!jsonObject.isNull("session")) {
                result.mSession = jsonObject.getString("session");
            }
            if (!jsonObject.isNull("session_id")) {
                result.mSessionID = jsonObject.getString("session_id");
            }
            if (!jsonObject.isNull("timestamp")) {
                result.mTimestamp = jsonObject.getString("timestamp");
            }
            if (!jsonObject.isNull("service_id")) {
                result.mServiceID = jsonObject.getString("service_id");
            }
            if (!jsonObject.isNull("log_id")) {
                result.mLogID = jsonObject.getString("log_id");
            }
            if (!jsonObject.isNull("interaction_id")) {
                result.mInteractionID = jsonObject.getString("interaction_id");
            }
            if (!jsonObject.isNull("response_list")) {
                result.mResponseList = parseResponseList(jsonObject.getJSONArray("response_list"));
            }
            if (!jsonObject.isNull("dialog_state")) {
                if (!jsonObject.getJSONObject("dialog_state").isNull("skill_states")) {
                    result.mDialogStateSkillStates = parseDialogStateSkillStates(
                            jsonObject.getJSONObject("dialog_state").getJSONObject("skill_states"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    static private ArrayList<Robot_Result_Response> parseResponseList(JSONArray jsonArray) {
        if (null == jsonArray) return null;
        ArrayList<Robot_Result_Response> responseLists = new ArrayList<Robot_Result_Response>();
        try {
            for (int i = 0; i< jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Robot_Result_Response response = new Robot_Result_Response();
                if (!jsonObject.isNull("status")) {
                    response.mStatus = jsonObject.getInt("status");
                }
                if (!jsonObject.isNull("msg")) {
                    response.mMsg = jsonObject.getString("msg");
                }
                if (!jsonObject.isNull("origin")) {
                    response.mOrigin = jsonObject.getString("origin");
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
                responseLists.add(response);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseLists;
    }

    static private ArrayList<Robot_Result_DialogState_SkillStates> parseDialogStateSkillStates(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        ArrayList<Robot_Result_DialogState_SkillStates> skillStatesList = new ArrayList<Robot_Result_DialogState_SkillStates>();
        try {
            Iterator it = jsonObject.keys();
            while(it.hasNext()) {
                Robot_Result_DialogState_SkillStates skillStates = new Robot_Result_DialogState_SkillStates();
                String key = (String)it.next();
                JSONObject valueJsonObj = jsonObject.getJSONObject(key);
                skillStates.mKey = key;
                if (!valueJsonObj.isNull("intents")) {
                    skillStates.mIntents = parseIntents(valueJsonObj.getJSONArray("intents"));
                }
                if (!valueJsonObj.isNull("contexts")) {
                    skillStates.mContext = valueJsonObj.getJSONObject("contexts");
                }
                if (!valueJsonObj.isNull("user_slots")) {
                    skillStates.mUserSlots = valueJsonObj.getJSONObject("user_slots");
                }
                skillStatesList.add(skillStates);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return skillStatesList;
    }

    static private ArrayList<Result_DialogState_SkillStates_Intents> parseIntents(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Result_DialogState_SkillStates_Intents> intentsList = new ArrayList<Result_DialogState_SkillStates_Intents>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Result_DialogState_SkillStates_Intents intents = new Result_DialogState_SkillStates_Intents();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("name")) {
                    intents.mName = jsonObject.getString("name");
                }
                if (!jsonObject.isNull("index")) {
                    intents.mIndex = jsonObject.getInt("index");
                }
                intentsList.add(intents);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return intentsList;
    }
}
