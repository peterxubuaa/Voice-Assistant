package com.min.aiassistant.baidu.unit.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseKeyBoardBotJson extends BaiduParseBaseJson {
    private static ParseKeyBoardBotJson sParseKeyBoardBotJson = null;

    public static ParseKeyBoardBotJson getInstance() {
        if (null == sParseKeyBoardBotJson) {
            sParseKeyBoardBotJson = new ParseKeyBoardBotJson();
        }
        return sParseKeyBoardBotJson;
    }

    public class BotKeyBoardUnit extends BaiduParseBaseResponse {
        Bot_Result mResult;
    }

    public class Bot_Result {
        String mVersion;
        String mBotSession;
        String mBotSessionID;
        String mTimestamp;
        String mBotID;
        String mLogID;
        String mInteractionID;
        Result_Response mResponse;
    }

    class Result_Response {
        int mStatus;
        String mMsg;
        Base_Response_Schema mSchema;
        ArrayList<Base_Response_Action> mActionList;
        Base_Response_QuRes mQuRes;
    }

    class Base_Response_Schema {
        double mIntentConfidence;
        ArrayList<Base_Response_Schema_Slots> mSlots;
        double mDomainConfidence;
        JSONArray mSluTags;
        String mIntent;
    }

    class Base_Response_Schema_Slots {
        Double mConfidence;
        String mName;
        String mOriginalWord;
        String mNormalizedWord;
    }

    public class BotSimpleResponse {
        public int mErrorCode;
        public String mBotID;
        public String mBotSession;
        public String mBotSessionID;
        public String mRawQuery;
        public SimpleResponseAction mAction;
    }

    public class SimpleResponseAction {
        String mBotID; //response_list -> origin
        String mIntent; //response_list -> schema -> intent
        Double mIntentConfidence; ////response_list -> schema -> intent_confidence
        String mActionID; //response_list -> action_list -> action_id
        public String mSay; //response_list -> action_list -> say
        public String mType; //response_list -> action_list -> type
        Double mConfidence;//response_list -> action_list -> confidence
    }

    class Base_Response_Action {
        String mActionID;
        Base_Response_Action_RefineDetail mRefineDetail;
        double mConfidence;
        String mCustomReply;
        String mSay;
        String mType;
    }

    class Base_Response_Action_RefineDetail {
        JSONArray mOptionList;
        String mInteract;
        String mClarifyReason;
    }

    class Base_Response_QuRes {
        JSONArray mCandidates;
        String mQuResChosen;
        Base_Response_QuRes_SentimentAnalysis mSentimentAnalysis;
        ArrayList<Base_Response_QuRes_LexicalAnalysis> mLexicalAnalysis;
        String mRawQuery;
        int mStatus;
        int mTimestamp;
    }

    class Base_Response_QuRes_SentimentAnalysis {
        double mPval;
        String mLabel;
    }

    class Base_Response_QuRes_LexicalAnalysis {
        JSONArray mETypes;
        JSONArray mBasicWord;
        double mWeight;
        String mTerm;
        String mType;
    }

    public BotKeyBoardUnit parse(String json) {
        BotKeyBoardUnit botKeyBoardUnit = new BotKeyBoardUnit();
        try {
            JSONObject jsonObject = new JSONObject(json);
            baseParse(jsonObject, botKeyBoardUnit);
            if (!jsonObject.isNull("result")) {
                botKeyBoardUnit.mResult = parseBotResult(jsonObject.getJSONObject("result"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return botKeyBoardUnit;
    }

    private Bot_Result parseBotResult(JSONObject jsonObject) {
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

    private String getBotSessionID(String botSession) {
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

    private Result_Response parseResponse(JSONObject jsonObject) {
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

    private Base_Response_QuRes parseQuRes(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        Base_Response_QuRes quRes =  new Base_Response_QuRes();
        try {
            if (!jsonObject.isNull("candidates")) {
                quRes.mCandidates = jsonObject.getJSONArray("candidates");
            }
            if (!jsonObject.isNull("qu_res_chosen")) {
                quRes.mQuResChosen = jsonObject.getString("qu_res_chosen");
            }
            if (!jsonObject.isNull("sentiment_analysis")) {
                quRes.mSentimentAnalysis = parseSentimentAnalysis(jsonObject.getJSONObject("sentiment_analysis"));
            }
            if (!jsonObject.isNull("lexical_analysis")) {
                quRes.mLexicalAnalysis = parseLexicalAnalysis(jsonObject.getJSONArray("lexical_analysis"));
            }
            if (!jsonObject.isNull("raw_query")) {
                quRes.mRawQuery = jsonObject.getString("raw_query");
            }
            if (!jsonObject.isNull("status")) {
                quRes.mStatus = jsonObject.getInt("status");
            }
            if (!jsonObject.isNull("timestamp")) {
                quRes.mTimestamp = jsonObject.getInt("timestamp");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return quRes;
    }

    public BotSimpleResponse getBotSimpleResponse(String query, BotKeyBoardUnit botKeyBoardUnit) {
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

    private Base_Response_Schema parseSchema(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        Base_Response_Schema schema = new Base_Response_Schema();
        try {
            if (!jsonObject.isNull("intent_confidence")) {
                schema.mIntentConfidence = jsonObject.getDouble("intent_confidence");
            }
            if (!jsonObject.isNull("slots")) {
                schema.mSlots = parseSlots(jsonObject.getJSONArray("slots"));
            }
            if (!jsonObject.isNull("domain_confidence")) {
                schema.mDomainConfidence = jsonObject.getDouble("domain_confidence");
            }
            if (!jsonObject.isNull("slu_tags")) {
                schema.mSluTags = jsonObject.getJSONArray("slu_tags");
            }
            if (!jsonObject.isNull("intent")) {
                schema.mIntent = jsonObject.getString("intent");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return schema;
    }

    private ArrayList<Base_Response_Schema_Slots> parseSlots(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Base_Response_Schema_Slots> slotList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Base_Response_Schema_Slots slot = new Base_Response_Schema_Slots();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                slot.mName = jsonObject.getString("name");
                slot.mOriginalWord = jsonObject.getString("original_word");
                slot.mNormalizedWord = jsonObject.getString("normalized_word");
                slot.mConfidence = jsonObject.getDouble("confidence");
                slotList.add(slot);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return slotList;
    }

    private Base_Response_QuRes_SentimentAnalysis parseSentimentAnalysis(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        Base_Response_QuRes_SentimentAnalysis sentimentAnalysis =  new Base_Response_QuRes_SentimentAnalysis();
        try {
            if (!jsonObject.isNull("pval")) {
                sentimentAnalysis.mPval = jsonObject.getDouble("pval");
            }
            if (!jsonObject.isNull("label")) {
                sentimentAnalysis.mLabel = jsonObject.getString("label");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sentimentAnalysis;
    }

    private ArrayList<Base_Response_QuRes_LexicalAnalysis> parseLexicalAnalysis(JSONArray jsonArray) {
        if (null == jsonArray) return null;
        ArrayList<Base_Response_QuRes_LexicalAnalysis> lexicalAnalysesList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Base_Response_QuRes_LexicalAnalysis lexicalAnalysis = new Base_Response_QuRes_LexicalAnalysis();
                if (!jsonObject.isNull("etypes")) {
                    lexicalAnalysis.mETypes = jsonObject.getJSONArray("etypes");
                }
                if (!jsonObject.isNull("basic_word")) {
                    lexicalAnalysis.mBasicWord = jsonObject.getJSONArray("basic_word");
                }
                if (!jsonObject.isNull("weight")) {
                    lexicalAnalysis.mWeight = jsonObject.getDouble("weight");
                }
                if (!jsonObject.isNull("term")) {
                    lexicalAnalysis.mTerm = jsonObject.getString("term");
                }
                if (!jsonObject.isNull("type")) {
                    lexicalAnalysis.mType = jsonObject.getString("type");
                }
                lexicalAnalysesList.add(lexicalAnalysis);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lexicalAnalysesList;
    }

    private ArrayList<Base_Response_Action> parseActionList(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Base_Response_Action> actionList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Base_Response_Action action = new Base_Response_Action();
                if (!jsonObject.isNull("action_id")) {
                    action.mActionID = jsonObject.getString("action_id");
                }
                if (!jsonObject.isNull("refine_detail")) {
                    action.mRefineDetail = parseRefineDetail(jsonObject.getJSONObject("refine_detail"));
                }
                if (!jsonObject.isNull("confidence")) {
                    action.mConfidence = jsonObject.getDouble("confidence");
                }
                if (!jsonObject.isNull("custom_reply")) {
                    action.mCustomReply = jsonObject.getString("custom_reply");
                }
                if (!jsonObject.isNull("say")) {
                    action.mSay = jsonObject.getString("say");
                }
                if (!jsonObject.isNull("type")) {
                    action.mType = jsonObject.getString("type");
                }
                actionList.add(action);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return actionList;
    }

    private Base_Response_Action_RefineDetail parseRefineDetail(JSONObject jsonObject) {
        if (null == jsonObject) return null;
        Base_Response_Action_RefineDetail refineDetail = new Base_Response_Action_RefineDetail();
        try {
            if (!jsonObject.isNull("option_list")) {
                refineDetail.mOptionList = jsonObject.getJSONArray("option_list");
            }
            if (!jsonObject.isNull("interact")) {
                refineDetail.mInteract = jsonObject.getString("interact");
            }
            if (!jsonObject.isNull("clarify_reason")) {
                refineDetail.mClarifyReason = jsonObject.getString("clarify_reason");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return refineDetail;
    }
}
