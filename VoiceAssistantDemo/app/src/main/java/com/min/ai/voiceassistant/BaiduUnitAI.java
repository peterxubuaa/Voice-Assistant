package com.min.ai.voiceassistant;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class BaiduUnitAI implements EventListener {
    private final String TAG = BaiduUnitAI.class.getSimpleName();
    private final boolean DEBUG = true;
    //OK: 81459(天气), 81461(闲聊), 81469(成语问答), 81465(单位换算), 81476(智能对联), 81481(智能写诗), 81467(计算器), 81482(名词解释)
    //81485(智能问答)
    //Pending: 81472(屏幕控制),  81483(音乐), 81479(故事),81487(打电话), 81462(电影)
    private Map<String, String> BOTID_MAP = new HashMap<String, String>() {
                            {
                                put("81459","天气");
                                put("81461","闲聊");
                                put("81469","成语问答");
                                put("81465","单位换算");
                                put("81476","智能对联");
                                put("81481","智能写诗");
                                put("81467","计算器");
                                put("81482","名词解释");
                                put("81485","智能问答");
                            }
        };
//    private final String BOT_ID_LIST[] = new String[] {"81459", "81461", "81469", "81465", "81476", "81481", "81467", "81482", "81485" };
    private String[] WEATHER = new String[]{"天气", "气候", "温度"};
    private String[] UNIT_CONVERSION = new String[]{"等于"};
    private String[] CALCULATOR = new String[]{"+", "-", "÷", "×", "开方", "平方", "开根号", "根号"};
//    private String[] IAQ = new String[]{"多大", "多高", "多远", "多深", "是啥", "是什么", "解释", "意思是", "是啥"};
    private String[] POEM = new String[]{"诗", "题词"};
    private String[] COUPLET = new String[]{"对联"};
    private String[] QUIT_LAST_BOTID = new String[]{"不是", "搞错了", "搞没搞错", "说点别的", "换个回答", "不对"};
    private EventManager mASR;
    private Context mContext;
    private onUnitListener mListener;
    private BestResponse mBestResponse;

    class UnitResponse {
        String mBotID; //81482, 81459, 81481, 81461, 81469, 81465, 81467, 81476, 81485
        String mLogID;
        String mInteractionId;
        String mIntent;//BUILT_SUBSTANTIVE, BUILT_POEM, BUILT_CHAT, BUILT_IDIOM_QA, BUILT_MEASUREMENT, BUILT_CALCULATOR, BUILT_COUPLET, BUILT_IAQ
        String mRawQuery;
        String mSay;
        String mActionID; //built_substantive_satisfy, fail_action, built_poem_satisfy, built_idiom_qa_satisfy, build_measurement_satisfy, build_calculator_satisfy, build_couplet_satisfy, build_iaq_satisfy
        String mType; //failure, satisfy, chat
        String mConfidence; // 0 ~ 100
    }

    class BotSession {
        String mBotID;
        String mBotSessionID;
    }

    class BestResponse {
        String mBotID;
        String mBotIDLabel;
        String mBotSessionID;
        String mRawQuery;
        String mAnswer;

        void clear() {
            mBotID = "";
            mBotIDLabel = "";
            mBotSessionID = "";
            mRawQuery = "";
            mAnswer = "";
        }
    }

    public interface onUnitListener {
        void onShowDebugInfo(String info, boolean reset);
        void onExit();
        void onFinalResult(String question, String answer);
    }

    BaiduUnitAI(Context context, onUnitListener listener) {
        mContext = context;
        mListener = listener;
        mBestResponse = new BestResponse();
    }

    void initBaiduUnit() {
        mASR = EventManagerFactory.create(mContext, "asr");
        mASR.registerListener(this); //  EventListener 中 onEvent方法
    }

    void releaseBaiduUnit() {
        mASR.unregisterListener(this);
        mASR = null;
    }

    void startBaiduUnit() {
        Map<String, Object> params = new LinkedHashMap<>();
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
        mASR.send(event, json, null, 0, 0);
    }

    void stopBaiduUnit() {
        mASR.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
        mBestResponse.clear();
    }

    /**
     * Unit 2.0具体功能请通过Unit的QQ群，工单，论坛咨询。语音相关反馈方式不回复Unit相关问题
     */
    private JSONArray unitParams() {
        JSONArray json = new JSONArray();
        try {
            for (String bot_id : BOTID_MAP.keySet()) {
                JSONObject bot = new JSONObject();
//                bot.put("version","2.0");
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

        if (DEBUG) {
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
            mListener.onShowDebugInfo(logTxt + "\n\n", "asr.ready".equals(name));
        }
    }

    private void eventTrigger(String name, String params) {
        if (name.equals("asr.ready")) {
        } else if(name.equals("asr.sn")) {
        } else if(name.equals("asr.begin")) {
        } else if(name.equals("asr.end")) {
        } else if(name.equals("asr.finish")) {
        } else if(name.equals("asr.exit")) {
            mListener.onExit();
        } else if(name.equals("asr.partial")) {
        } else if(name.equals("unit.finish")) {
            parseResponse(params);
            if (DEBUG) Log.i(TAG, mBestResponse.mAnswer);
            mListener.onFinalResult(mBestResponse.mRawQuery + "(" + (mBestResponse.mBotIDLabel) + ")",
                    mBestResponse.mAnswer);
        }
    }

    private void parseResponse(String params) {
        try {
            JSONObject jsonObj = new JSONObject(params);
            ArrayList<UnitResponse> responseList = new ArrayList<>();
            if (!jsonObj.isNull("unit_response")) {
                JSONArray unit_responseJsonArray = jsonObj.getJSONArray("unit_response");
                for (int unit_response_index = 0; unit_response_index < unit_responseJsonArray.length(); unit_response_index++) {
                    JSONObject unit_responseJsonObj = unit_responseJsonArray.getJSONObject(unit_response_index);
                    UnitResponse response = new UnitResponse();
                    if (!unit_responseJsonObj.isNull("bot_id")) {
                        response.mBotID = unit_responseJsonObj.getString("bot_id");
                    }
                    if (!unit_responseJsonObj.isNull("log_id")) {
                        response.mLogID = unit_responseJsonObj.getString("log_id");
                    }
                    if (!unit_responseJsonObj.isNull("interaction_id")) {
                        response.mInteractionId = unit_responseJsonObj.getString("interaction_id");
                    }
                    if (!unit_responseJsonObj.isNull("response")) {
                        JSONObject responseJsonObj = unit_responseJsonObj.getJSONObject("response");
                        if (!responseJsonObj.isNull("schema")) {
                            JSONObject schemaJsonObj = responseJsonObj.getJSONObject("schema");
                            if (!schemaJsonObj.isNull("intent")) {
                                response.mIntent = schemaJsonObj.getString("intent");
                            }
                        }
                        if (!responseJsonObj.isNull("qu_res")) {
                            JSONObject qu_resJsonObj = responseJsonObj.getJSONObject("qu_res");
                            if (!qu_resJsonObj.isNull("raw_query")) {
                                response.mRawQuery = qu_resJsonObj.getString("raw_query");
                            }
                            if (!responseJsonObj.isNull("action_list")) {
                                JSONArray action_listJsonArray = responseJsonObj.getJSONArray("action_list");
//                                for (int action_list_index = 0; action_list_index < action_listJsonArray.length(); action_list_index++) {
                                if (action_listJsonArray.length() > 0) {
                                    //first one has best confidence
                                    JSONObject action_listObj = action_listJsonArray.getJSONObject(0);
                                    if (!action_listObj.isNull("say")) {
                                        response.mSay = action_listObj.getString("say");
                                    }
                                    if (!action_listObj.isNull("action_id")) {
                                        response.mActionID = action_listObj.getString("action_id");
                                    }
                                    if (!action_listObj.isNull("type")) {
                                        response.mType = action_listObj.getString("type");
                                    }
                                    if (!action_listObj.isNull("confidence")) {
                                        response.mConfidence = action_listObj.getString("confidence");
                                    }
                                }
                            }
                        }
                    }
                    responseList.add(response);
                }
            }

            ArrayList<BotSession> botSessionList = new ArrayList<>();
            if (!jsonObj.isNull("bot_session_list")) {
                JSONArray bot_session_listJsonArray = jsonObj.getJSONArray("bot_session_list");
                for (int bot_session_list_index = 0; bot_session_list_index < bot_session_listJsonArray.length(); bot_session_list_index++) {
                    BotSession botSession = new BotSession();
                    JSONObject bot_session_listObj = bot_session_listJsonArray.getJSONObject(bot_session_list_index);
                    if (!bot_session_listObj.isNull("bot_id")) {
                        botSession.mBotID = bot_session_listObj.getString("bot_id");
                    }
                    if (!bot_session_listObj.isNull("bot_session_id")) {
                        botSession.mBotSessionID = bot_session_listObj.getString("bot_session_id");
                    }
                    botSessionList.add(botSession);
                }
            }
            getBestAnswer(responseList, botSessionList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getBestAnswer(ArrayList<UnitResponse> responseList, ArrayList<BotSession> botSessionList) {
        String rawQuery = "";
        for (UnitResponse response : responseList) {
            if (!TextUtils.isEmpty(response.mRawQuery)) {
                rawQuery = response.mRawQuery;
                break;
            }
        }

        if (TextUtils.isEmpty(rawQuery)) {
            return;
        }
        //OK: 81459(天气), 81461(闲聊), 81469(成语问答), 81465(单位换算), 81476(智能对联), 81481(智能写诗), 81467(计算器), 81482(名词解释)
        //81485(智能问答)
        String answer;
        mBestResponse.mRawQuery = rawQuery;
        if (!TextUtils.isEmpty(mBestResponse.mBotID) && isContainKeyWord(rawQuery, QUIT_LAST_BOTID)) {
            mBestResponse.mBotID = "";
            mBestResponse.mBotIDLabel = "";
            mBestResponse.mAnswer = "好的，我明白了。请继续提问！";
            return;
        }

        if (TextUtils.isEmpty(mBestResponse.mBotID)) {
            //第一次对话，从中找关键字，区分对话类别
            mBestResponse.mBotID = getBestBotID(rawQuery);
            if (!TextUtils.isEmpty(mBestResponse.mBotID)) {
                answer = getAnswer(mBestResponse.mBotID, responseList);
                if (!TextUtils.isEmpty(answer)) {//找到预期类别的回答
                    mBestResponse.mBotIDLabel = BOTID_MAP.get(mBestResponse.mBotID);
                    mBestResponse.mBotSessionID = getSessionID(mBestResponse.mBotID, botSessionList);
                    mBestResponse.mAnswer = answer;
                    return;
                }
            }
        } else {
            //后续关联对话
            answer = getAnswer(mBestResponse.mBotID, responseList);
            if (!TextUtils.isEmpty(answer)) {//找到预期类别的回答
                mBestResponse.mBotSessionID = getSessionID(mBestResponse.mBotID, botSessionList);
                mBestResponse.mAnswer = answer;
                return;
            } else { //没找到上次对话类别的回答
                mBestResponse.mBotID = getBestBotID(rawQuery);
                if (!TextUtils.isEmpty(mBestResponse.mBotID)) {
                    answer = getAnswer(mBestResponse.mBotID, responseList);
                    if (!TextUtils.isEmpty(answer)) {
                        mBestResponse.mBotIDLabel = BOTID_MAP.get(mBestResponse.mBotID);
                        mBestResponse.mAnswer = answer;
                        return;
                    }
                }
            }
        }

        answer = getAnswer("81485", responseList);//(智能问答)
        if (!TextUtils.isEmpty(answer)) {
            mBestResponse.mBotIDLabel = BOTID_MAP.get("81485");
            mBestResponse.mBotSessionID = getSessionID(mBestResponse.mBotID, botSessionList);
            mBestResponse.mAnswer = answer;
            return;
        }

        answer = getAnswer("81461", responseList);//(闲聊)
        if (!TextUtils.isEmpty(answer)) {
            mBestResponse.mBotIDLabel = BOTID_MAP.get("81461");
            mBestResponse.mBotSessionID = getSessionID(mBestResponse.mBotID, botSessionList);
            mBestResponse.mAnswer = answer;
            return;
        }

        mBestResponse.mAnswer = "问的啥玩意？听不懂！";
    }

    private String getBestBotID(String rawQuery) {
        String botID = "";
        if (isContainKeyWord(rawQuery, WEATHER)) {
            botID = "81459";//(天气)
        } else if (isContainKeyWord(rawQuery, CALCULATOR)) {
            botID = "81467";//计算器)
        } else if (isContainKeyWord(rawQuery, UNIT_CONVERSION)) {
            botID = "81465";//(单位换算)
        } else if (isContainKeyWord(rawQuery, POEM)) {
            botID = "81481";//(智能写诗)
        } else if (isContainKeyWord(rawQuery, COUPLET)) {
            botID = "81476";//(智能对联)
        }
        return botID;
    }

    private String getSessionID(String botID, ArrayList<BotSession> botSessionList) {
        if (TextUtils.isEmpty(botID)) return "";
        for (BotSession botSession : botSessionList) {
            if (botID.equals(botSession.mBotID)) {
                return botSession.mBotSessionID;
            }
        }
        return "";
    }

    private String getAnswer(String botID, ArrayList<UnitResponse> responseList) {
        if (TextUtils.isEmpty(botID)) return "";
        for (UnitResponse response : responseList) {
            if (botID.equals(response.mBotID) && !"failure".equals(response.mType)) {
                return response.mSay;
            }
        }
        return "";
    }

    private boolean isContainKeyWord(String query, String[] strList) {
        for (String keyword : strList) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
