package com.fih.featurephone.voiceassistant.baidu.unit.parsejson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParseJson {

    static class Base_Response_Schema {
        double mIntentConfidence;
        ArrayList<Base_Response_Schema_Slots> mSlots;
        double mDomainConfidence;
        JSONArray mSluTags;
        String mIntent;
    }

    static class Base_Response_Schema_Slots {
        Double mConfidence;
        String mName;
        String mOriginalWord;
        String mNormalizedWord;
    }

    static Base_Response_Schema parseSchema(JSONObject jsonObject) {
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

    static private ArrayList<Base_Response_Schema_Slots> parseSlots(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Base_Response_Schema_Slots> slotList = new ArrayList<Base_Response_Schema_Slots>();
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

    public static class SimpleResponseAction {
        public String mBotID; //response_list -> origin
        String mIntent; //response_list -> schema -> intent
        Double mIntentConfidence; ////response_list -> schema -> intent_confidence
        String mActionID; //response_list -> action_list -> action_id
        public String mSay; //response_list -> action_list -> say
        public String mType; //response_list -> action_list -> type
        Double mConfidence;//response_list -> action_list -> confidence
    }


    static class Base_Response_Action {
        String mActionID;
        Base_Response_Action_RefineDetail mRefineDetail;
        double mConfidence;
        String mCustomReply;
        String mSay;
        String mType;
    }


    static class Base_Response_Action_RefineDetail {
        JSONArray mOptionList;
        String mInteract;
        String mClarifyReason;
    }


    static private Base_Response_Action_RefineDetail parseRefineDetail(JSONObject jsonObject) {
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

    static class Base_Response_QuRes {
        JSONArray mCandidates;
        String mQuResChosen;
        Base_Response_QuRes_SentimentAnalysis mSentimentAnalysis;
        ArrayList<Base_Response_QuRes_LexicalAnalysis> mLexicalAnalysis;
        String mRawQuery;
        int mStatus;
        int mTimestamp;
    }

    static class Base_Response_QuRes_SentimentAnalysis {
        double mPval;
        String mLabel;
    }

    static class Base_Response_QuRes_LexicalAnalysis {
        JSONArray mETypes;
        JSONArray mBasicWord;
        double mWeight;
        String mTerm;
        String mType;
    }

    static Base_Response_QuRes parseQuRes(JSONObject jsonObject) {
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

    static private Base_Response_QuRes_SentimentAnalysis parseSentimentAnalysis(JSONObject jsonObject) {
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

    static private ArrayList<Base_Response_QuRes_LexicalAnalysis> parseLexicalAnalysis(JSONArray jsonArray) {
        if (null == jsonArray) return null;
        ArrayList<Base_Response_QuRes_LexicalAnalysis> lexicalAnalysesList = new ArrayList<Base_Response_QuRes_LexicalAnalysis>();
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

    static ArrayList<Base_Response_Action> parseActionList(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Base_Response_Action> actionList = new ArrayList<Base_Response_Action>();
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

}
