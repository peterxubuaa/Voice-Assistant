package com.fih.featurephone.voiceassistant.baidu.unit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.fih.featurephone.voiceassistant.baidu.unit.parsejson.ParseASRJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaiduBotASRUnit implements EventListener {
    private final static String TAG = BaiduBotASRUnit.class.getSimpleName();

    private final BaiduUnitAI mBaiduUnitAI;
    private EventManager mASR;
    private Context mContext;
    private BaiduUnitAI.OnUnitListener mListener;
    private BaiduUnitAI.BestResponse mBestResponse;
    private ArrayList<String> mBotTypeList;

    BaiduBotASRUnit(BaiduUnitAI baiduUnitAI, BaiduUnitAI.BestResponse bestResponse,
                    ArrayList<String> botTypeList, Context context, BaiduUnitAI.OnUnitListener listener) {
        mBaiduUnitAI = baiduUnitAI;
        mBestResponse = bestResponse;
        mBotTypeList = botTypeList;
        mContext = context;
        mListener = listener;
    }

    void initBaiduASRUnit() {
        mASR = EventManagerFactory.create(mContext, "asr");
        mASR.registerListener(this); //  EventListener 中 onEvent方法
    }

    void releaseBaiduASRUnit() {
        if (null != mASR) mASR.unregisterListener(this);
        mASR = null;
    }

    void startBaiduASRUnit() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.PID, 15364); // Unit  2.0 固定pid,仅支持中文普通话
        // params.put(SpeechConstant.NLU, "enable");
//         params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0); // 长语音
        // params.put(SpeechConstant.IN_FILE, "res:///com/baidu/android/voicedemo/16k_test.pcm");
//         params.put(SpeechConstant.VAD, SpeechConstant.VAD_DNN);

        // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params);
        params.put(SpeechConstant.BOT_SESSION_LIST, unitParams());
        String json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json

        String event = SpeechConstant.ASR_START; // 替换成测试的event
        if (null != mASR) mASR.send(event, json, null, 0, 0);
    }

/*    void stopBaiduASRUnit() {
        if (null != mASR) mASR.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
        mBestResponse.reset();
    }*/

    void cancelBaiduASRUnit() {
        if (null != mASR) mASR.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0); //
        mBestResponse.reset();
    }

    private JSONArray unitParams() {
        JSONArray json = new JSONArray();
        try {
            for (String bot_id : mBotTypeList) { //WEB_BOTID_MAP
                JSONObject bot = new JSONObject();
                bot.put("bot_id", bot_id);
                if (bot_id.equals(mBestResponse.mBotID)) {
                    bot.put("bot_session_id", mBestResponse.mBotSessionID);
                } else {
                    bot.put("bot_session_id", "");
                }
                bot.put("bot_session", "");
                json.put(bot);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // 基于sdk集成1.2 自定义输出事件类 EventListener 回调方法
    // 基于SDK集成3.1 开始回调事件
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        eventTrigger(name, params);

//        if (true) {
            String logTxt = "name: " + name;
            if (params != null && !params.isEmpty()) {
                logTxt += " ;params :" + params;
            }
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                if (params != null && params.contains("\"nlu_result\"")) {
                    if (length > 0 && data.length > 0) {
                        logTxt += ", 语义解析结果：" + new String(data, offset, length);
                    }
                }
            } else if (data != null) {
                logTxt += " ;data length=" + data.length;
            }

            Log.i(TAG, logTxt);
            if (null != mListener) mListener.onShowDebugInfo(logTxt + "\n\n", "asr.ready".equals(name));
//        }
    }

    private void eventTrigger(String name, String params) {
        if(name.equals("asr.exit")) {
            if (null != mListener) mListener.onExit();
        } else if(name.equals("unit.finish")) {
            ParseASRJson.ASRUnit asrUnit = ParseASRJson.parseASRResponse(params);
            ParseASRJson.BotSimpleResponse botSimpleResponse = ParseASRJson.getBotSimpleResponse(asrUnit);
            getBestResponseFromBotUnit(botSimpleResponse, mBestResponse);
            if (null != mListener) mListener.onFinalResult(
                        mBestResponse.mRawQuery + "(" + (mBestResponse.mBotIDLabel) + ")",
                                mBestResponse.mAnswer, null);
//            if (mBaiduUnitAI.isLocalEvent(mBestResponse.mBotID)) {
//                ArrayList<ParseJson.Base_Response_Schema_Slots> slotList = ParseASRJson.getSchemaSlots(asrUnit, mBestResponse.mBotID);
//                ParseJson.SimpleResponseAction action = ParseASRJson.getBestSimpleResponseAction(botSimpleResponse, mBestResponse.mBotID);
//                mBaiduUnitAI.triggerLocalEvent(action, slotList, mBestResponse);
//            }
        }
//        } else if (name.equals("asr.ready")) {
//        } else if(name.equals("asr.sn")) {
//        } else if(name.equals("asr.begin")) {
//        } else if(name.equals("asr.end")) {
//        } else if(name.equals("asr.finish")) {
//        } else if(name.equals("asr.partial")) {
    }

    private void getBestResponseFromBotUnit(ParseASRJson.BotSimpleResponse botSimpleResponse, BaiduUnitAI.BestResponse bestResponse) {
        //1.
        if (null == botSimpleResponse) {
            bestResponse.mAnswer = "问的啥玩意？听不懂！";
            return;
        }

        String answer;
        bestResponse.mRawQuery = botSimpleResponse.mRawQuery;
        //2. 是否重置对话
        if (!TextUtils.isEmpty(bestResponse.mBotID) && mBaiduUnitAI.isQuitSession(bestResponse.mRawQuery)) {
            bestResponse.mBotID = "";
            bestResponse.mBotIDLabel = "";
            bestResponse.mAnswer = "好的，我明白了。请继续提问！";
            return;
        }

        //3.
        if (TextUtils.isEmpty(bestResponse.mBotID)) {
            //第一次对话，从中找关键字，区分对话类别
            ArrayList<BaiduUnitAI.BestBotID> bestBotIDList = mBaiduUnitAI.getBestBotID(bestResponse.mRawQuery);
            for (BaiduUnitAI.BestBotID bestBotID : bestBotIDList) {
                bestResponse.mBotID = bestBotID.mBotID;
                if (!TextUtils.isEmpty(bestResponse.mBotID)) {
                    answer = getAnswer(bestResponse.mBotID, botSimpleResponse.mActionList);
                    if (!TextUtils.isEmpty(answer)) {//找到预期类别的回答
                        bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(bestResponse.mBotID);
                        bestResponse.mBotSessionID = getSessionID(bestResponse.mBotID, botSimpleResponse.mBotSessionList);
                        bestResponse.mAnswer = answer;
                        return;
                    }
                } else {
                    //从bot session中发现线索
                    for (ParseASRJson.BotSession botSession : botSimpleResponse.mBotSessionList) {
                        if (!TextUtils.isEmpty(botSession.mBotSessionID)) {
                            answer = getAnswer(botSession.mBotID, botSimpleResponse.mActionList);
                            if (!TextUtils.isEmpty(answer)) {
                                bestResponse.mBotID = botSession.mBotID;
                                bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(botSession.mBotID);
                                bestResponse.mBotSessionID = botSession.mBotSessionID;
                                bestResponse.mAnswer = answer;
                                return;
                            }
                        }
                    }
                }
            }
        } else {
            //后续关联对话
            answer = getAnswer(bestResponse.mBotID, botSimpleResponse.mActionList);
            if (!TextUtils.isEmpty(answer)) {//找到预期类别的回答
                bestResponse.mBotSessionID = getSessionID(bestResponse.mBotID, botSimpleResponse.mBotSessionList);
                bestResponse.mAnswer = answer;
                return;
            } else { //没找到上次对话类别的回答
                ArrayList<BaiduUnitAI.BestBotID> bestBotIDList = mBaiduUnitAI.getBestBotID(bestResponse.mRawQuery);
                for (BaiduUnitAI.BestBotID bestBotID : bestBotIDList) {
                    bestResponse.mBotID = bestBotID.mBotID;
                    if (!TextUtils.isEmpty(bestResponse.mBotID)) {
                        answer = getAnswer(bestResponse.mBotID, botSimpleResponse.mActionList);
                        if (!TextUtils.isEmpty(answer)) {
                            bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(bestResponse.mBotID);
                            bestResponse.mAnswer = answer;
                            return;
                        }
                    }
                }
            }
        }

        //4.
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA)) {
            answer = getAnswer(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA, botSimpleResponse.mActionList);//(智能问答)
            if (!TextUtils.isEmpty(answer)) {
                bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA);
                bestResponse.mBotSessionID = getSessionID(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA, botSimpleResponse.mBotSessionList);
                bestResponse.mAnswer = answer;
                return;
            }
        }
        //5. 从action中找答案
        for (ParseASRJson.SimpleResponseAction action : botSimpleResponse.mActionList) {
            if (mBotTypeList.contains(action.mBotID) && !TextUtils.isEmpty(action.mSay)) {
                bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(action.mBotID);
                bestResponse.mBotSessionID = getSessionID(action.mBotID, botSimpleResponse.mBotSessionList);
                bestResponse.mAnswer = action.mSay;
                return;
            }
        }
        //6.
        if (mBotTypeList.contains(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT)) {
            answer = getAnswer(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT, botSimpleResponse.mActionList);//(闲聊)
            if (!TextUtils.isEmpty(answer)) {
                bestResponse.mBotIDLabel = mBaiduUnitAI.getBotIDLabel(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT);
                bestResponse.mBotSessionID = getSessionID(BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT, botSimpleResponse.mBotSessionList);
                bestResponse.mAnswer = answer;
                return;
            }
        }
        //7. fail
        bestResponse.mAnswer = "问的啥玩意？听不懂！";
    }

    private String getAnswer(String botID, ArrayList<ParseASRJson.SimpleResponseAction> responseList) {
        if (TextUtils.isEmpty(botID)) return "";
        for (ParseASRJson.SimpleResponseAction response : responseList) {
            if (botID.equals(response.mBotID) && !"failure".equals(response.mType)) {
                return response.mSay;
            }
        }
        return "";
    }

    private String getSessionID(String botID, ArrayList<ParseASRJson.BotSession> botSessionList) {
        if (TextUtils.isEmpty(botID)) return "";
        for (ParseASRJson.BotSession botSession : botSessionList) {
            if (botID.equals(botSession.mBotID)) {
                return botSession.mBotSessionID;
            }
        }
        return "";
    }
}
