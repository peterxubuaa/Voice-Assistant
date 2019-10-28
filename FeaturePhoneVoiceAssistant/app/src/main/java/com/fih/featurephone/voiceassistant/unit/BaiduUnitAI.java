package com.fih.featurephone.voiceassistant.unit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fih.featurephone.voiceassistant.speechaction.BrightnessAction;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.speechaction.BaseAction;
import com.fih.featurephone.voiceassistant.speechaction.LaunchAppAction;
import com.fih.featurephone.voiceassistant.speechaction.PhoneAction;
import com.fih.featurephone.voiceassistant.speechaction.VoiceVolumeAction;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaiduUnitAI {
    private final static String TAG = BaiduUnitAI.class.getSimpleName();

    static final int BAIDU_UNIT_TYPE_ASR_BOT = 1;
//    public static final int BAIDU_UNIT_TYPE_ASR_ROBOT = 2;
    public static  final int BAIDU_UNIT_TYPE_KEYBOARD_BOT = 3;
    public static  final int BAIDU_UNIT_TYPE_KEYBOARD_ROBOT = 4;

    static final int BAIDU_UNIT_ROBOT_TYPE_WEB = 1;
    static final int BAIDU_UNIT_ROBOT_TYPE_LOCAL = 2;
    static final int BAIDU_UNIT_ROBOT_TYPE_ALL = 3;

    static final String BAIDU_UNIT_BOT_TYPE_WEATHER = "81459";
    static final String BAIDU_UNIT_BOT_TYPE_IA = "81485";
    static final String BAIDU_UNIT_BOT_TYPE_DEFINITION = "81482";
    static final String BAIDU_UNIT_BOT_TYPE_IDIOM = "81469";
    static final String BAIDU_UNIT_BOT_TYPE_CALCULATOR = "81467";
    static final String BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION = "81465";
    static final String BAIDU_UNIT_BOT_TYPE_GREETING = "81460";
    static final String BAIDU_UNIT_BOT_TYPE_CHAT = "81461";
    static final String BAIDU_UNIT_BOT_TYPE_POEM = "81481";
    static final String BAIDU_UNIT_BOT_TYPE_COUPLET = "81476";
    static final String BAIDU_UNIT_BOT_TYPE_TRANSLATE = "84536";

    static final String BAIDU_UNIT_BOT_TYPE_PHONE = "81487";
    static final String BAIDU_UNIT_BOT_TYPE_MESSAGE = "81473";
    static final String BAIDU_UNIT_BOT_TYPE_SCREEN_CONTROL = "81472";
    static final String BAIDU_UNIT_BOT_TYPE_STORY = "81479";
    static final String BAIDU_UNIT_BOT_TYPE_MUSIC = "81483";
    static final String BAIDU_UNIT_BOT_TYPE_ALARM = "81478";
    static final String BAIDU_UNIT_BOT_TYPE_NOTIFY = "81475";
    static final String BAIDU_UNIT_BOT_TYPE_MOVIE = "81462";
    static final String BAIDU_UNIT_BOT_TYPE_TRAIN_TICKET = "81464";
    static final String BAIDU_UNIT_BOT_TYPE_RADIO = "81470";

    //OK: 81459(天气), 81461(闲聊), 81469(成语问答), 81465(单位换算), 81476(智能对联), 81481(智能写诗), 81467(计算器), 81482(名词解释), 81485(智能问答), 81460(问候)
    private final Map<String, String> WEB_BOTID_MAP = new HashMap<String, String>() {
            {
                put(BAIDU_UNIT_BOT_TYPE_GREETING, "问候");
                put(BAIDU_UNIT_BOT_TYPE_CHAT, "闲聊");
                put(BAIDU_UNIT_BOT_TYPE_IDIOM, "成语问答");
                put(BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION, "单位换算");
                put(BAIDU_UNIT_BOT_TYPE_COUPLET, "智能对联");
                put(BAIDU_UNIT_BOT_TYPE_WEATHER, "天气");
                put(BAIDU_UNIT_BOT_TYPE_POEM, "智能写诗");
                put(BAIDU_UNIT_BOT_TYPE_CALCULATOR, "计算器");
                put(BAIDU_UNIT_BOT_TYPE_DEFINITION, "名词解释");
                put(BAIDU_UNIT_BOT_TYPE_IA, "智能问答");
                put(BAIDU_UNIT_BOT_TYPE_TRANSLATE, "智能翻译");
            }
        };
    //Pending: 81472(屏幕控制), 81473(发短信), 81487(打电话), 81475(提醒), 81478(闹钟), 81483(音乐), 81479(故事), 81462(电影), 81464(火车票)
    private final Map<String, String> LOCAL_BOTID_MAP = new HashMap<String, String>() {
            {
                put(BAIDU_UNIT_BOT_TYPE_SCREEN_CONTROL, "屏幕控制");
                put(BAIDU_UNIT_BOT_TYPE_MESSAGE, "发短信");
                put(BAIDU_UNIT_BOT_TYPE_PHONE, "打电话");
                put(BAIDU_UNIT_BOT_TYPE_NOTIFY, "提醒");
                put(BAIDU_UNIT_BOT_TYPE_ALARM, "闹钟");
                put(BAIDU_UNIT_BOT_TYPE_MOVIE, "电影");
                put(BAIDU_UNIT_BOT_TYPE_MUSIC, "音乐");
                put(BAIDU_UNIT_BOT_TYPE_STORY, "故事");
                put(BAIDU_UNIT_BOT_TYPE_TRAIN_TICKET, "火车票");
                put(BAIDU_UNIT_BOT_TYPE_RADIO, "电台控制");
            }
    };

    protected Context mContext;
    private BaiduRobotKeyboardUnit mBaiduRobotKeyboardUnit;
    private BaiduBotKeyboardUnit mBaiduBotKeyboardUnit;
    private BaiduBotASRUnit mBaiduBotASRUnit;
    private int mBaiduUnitType;

    static public class BestResponse {
        String mBotID;
        String mIntent;
        String mBotIDLabel;
        String mBotSessionID;
        String mBotSession;
        String mSession;
        String mSessionID;
        String mRawQuery;
        public String mAnswer;
        public String mHint;

        public void reset() {
            mBotID = "";
            mIntent = "";
            mBotIDLabel = "";
            mBotSessionID = "";
            mBotSession = "";
            mSession = "";
            mSessionID = "";
            mRawQuery = "";
            mAnswer = "";
            mHint = "";
        }

        @Override
        public String toString() {
            return "BestResponse{" +
                    "mBotID='" + mBotID + '\'' +
                    ", mIntent='" + mIntent + '\'' +
                    ", mBotIDLabel='" + mBotIDLabel + '\'' +
                    ", mBotSessionID='" + mBotSessionID + '\'' +
                    ", mBotSession='" + mBotSession + '\'' +
                    ", mSession='" + mSession + '\'' +
                    ", mSessionID='" + mSessionID + '\'' +
                    ", mRawQuery='" + mRawQuery + '\'' +
                    ", mAnswer='" + mAnswer + '\'' +
                    ", mHint='" + mHint + '\'' +
                    '}';
        }
    }

    public interface onUnitListener {
        void onShowDebugInfo(String info, boolean reset);
        void onExit();
        void onFinalResult(String question, String answer, String hint);
    }

    public BaiduUnitAI(Context context, onUnitListener listener,
                       int baiduUnitType, int robotType, ArrayList<String> botTypeList) {
        mContext = context;
        mBaiduUnitType = baiduUnitType;

        BestResponse bestResponse = new BestResponse();
        bestResponse.reset();

        ArrayList<BaseAction> localActionList = new ArrayList<BaseAction>();
        localActionList.add(new PhoneAction(mContext));
        localActionList.add(new WebSearchAction(mContext));
        localActionList.add(new VoiceVolumeAction(mContext));
        localActionList.add(new BrightnessAction(mContext));
        localActionList.add(new LaunchAppAction(mContext));

        switch (mBaiduUnitType) {
            case BAIDU_UNIT_TYPE_ASR_BOT:
                mBaiduBotASRUnit = new BaiduBotASRUnit(this, bestResponse, botTypeList, context, listener);
                break;
            case BAIDU_UNIT_TYPE_KEYBOARD_BOT:
                mBaiduBotKeyboardUnit = new BaiduBotKeyboardUnit(this, bestResponse, botTypeList, listener, localActionList);
                break;
            case BAIDU_UNIT_TYPE_KEYBOARD_ROBOT:
                mBaiduRobotKeyboardUnit = new BaiduRobotKeyboardUnit(this, bestResponse, robotType, listener);
                break;
        }
    }

    public void initBaiduUnit() {
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.initBaiduASRUnit();
        }
    }

    public void releaseBaiduUnit(){
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.releaseBaiduASRUnit();
        }
    }

    public void startBaiduASRUnit() {
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.startBaiduASRUnit();
        }
    }

    public void stopBaiduASRUnit() {
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.stopBaiduASRUnit();
        }
    }

    String[] getBestBotID(String rawQuery) {
        final String[] UNIT_CONVERSION = new String[]{"等于"};
        final String[] CALCULATOR = new String[]{"+", "-", "÷", "×", "开方", "平方", "根号", "次方","计算"};
//    private String[] IAQ = new String[]{"多大", "多高", "多远", "多深", "是啥", "是什么", "解释", "意思是", "是啥"};
        final String[] TRANSLATE = new String[]{"翻译"};

        String[] result = new String[2];
        if (CommonUtil.isContainKeyWord(rawQuery, TRANSLATE)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_TRANSLATE;//(智能翻译)
        } else if (checkWeather(rawQuery)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_WEATHER;//(天气)
        } else if (CommonUtil.isContainKeyWord(rawQuery, CALCULATOR)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_CALCULATOR;//计算器)
        } else if (CommonUtil.isContainKeyWord(rawQuery, UNIT_CONVERSION)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION;//(单位换算)
        } else if (checkPoem(rawQuery, result)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_POEM;//(智能写诗)
        } else if (checkCouplet(rawQuery, result)) {
            result[0] = BAIDU_UNIT_BOT_TYPE_COUPLET;//(智能对联)
        }
        return result;
    }

    private boolean checkWeather(String query) {
        final String[] WEATHER_BIG = new String[]{"天气", "气候", "温度", "气温",
                "下雨", "下雪", "刮风", "下霜", "起雾", "雨下", "雪下", "风刮", "霜下", "雾起"};
        if (CommonUtil.isContainKeyWord(query, WEATHER_BIG)) return true;

        final String[] WEATHER_SMALL = new String[]{"雨", "雪", "风", "霜", "雾"};//, "冷", "热"};
        String keyWord = CommonUtil.getContainKeyWord(query, WEATHER_SMALL);
        if (!TextUtils.isEmpty(keyWord)) {
            ArrayList<String> REGEX = new ArrayList<String>();
            REGEX.add(".*?有.*?" + keyWord + ".*?");
            REGEX.add(".*?下.*?" + keyWord + ".*?");
            REGEX.add(keyWord + ".*?有.*?");
            if (CommonUtil.checkRegexMatch(query, REGEX.toArray(new String[1]))) return true;
        }

        final String[] WEATHER_TEMPERATURE = new String[]{"冷", "热"};
        keyWord = CommonUtil.getContainKeyWord(query, WEATHER_TEMPERATURE);
        if (!TextUtils.isEmpty(keyWord)) {
            ArrayList<String> REGEX = new ArrayList<String>();
            REGEX.add(".*?有.*?" + keyWord + ".*?");
            REGEX.add(".*?" + keyWord + "不" + keyWord + ".*?");
            if (CommonUtil.checkRegexMatch(query, REGEX.toArray(new String[1]))) return true;
        }

        return false;
    }

    private boolean checkCouplet(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] COUPLET = new String[]{"对联", "春联", "门联"};//"对子"
        if (!CommonUtil.isContainKeyWord(query, COUPLET)) return false;

        final String[] REGEX = {".*?用(.*?)写.*?联", ".*?根据(.*?)写.*?联", ".*?写.*?关于(.*?)联",
                                    ".*?用(.*?)做.*?联", ".*?根据(.*?)做.*?联", ".*?做.*?关于(.*?)联"};
        String keyWord = CommonUtil.getRegexMatch(query, REGEX);
        if (TextUtils.isEmpty(keyWord)) return false;

        result[1] = keyWord;
        return true;
    }

    private boolean checkPoem(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] POEM = new String[]{"诗"};
        if (!CommonUtil.isContainKeyWord(query, POEM)) return false;

        final String[] REGEX = {".*?用(.*?)写.*?诗", ".*?根据(.*?)写.*?诗", ".*?写.*?关于(.*?)诗",
                ".*?用(.*?)做.*?诗", ".*?根据(.*?)做.*?诗", "做.*?关于(.*?)诗"};
        String keyWord = CommonUtil.getRegexMatch(query, REGEX);
        if (TextUtils.isEmpty(keyWord)) return false;

        result[1] = keyWord;
        return true;
    }

    boolean isQuitSession(String query) {
        final String[] QUIT_LAST_BOTID = new String[]{"不是", "错了", "搞没搞错", "说点别的", "换个回答", "不对", "瞎说", "瞎回答", "回答错误"};
        return CommonUtil.isContainKeyWord(query, QUIT_LAST_BOTID);
    }

    String getBotIDLabel(String botID) {
        String label = WEB_BOTID_MAP.get(botID);
        if (TextUtils.isEmpty(label)) {
            label = LOCAL_BOTID_MAP.get(botID);
        }
        return label;
    }

//    boolean isLocalEvent(String botID) {
//        ArrayList<String> botIDList = new ArrayList<String>(LOCAL_BOTID_MAP.keySet());
////        String[] botIDs = botIDList.toArray(new String[0]);
////        return CommonUtil.isContainKeyWord(botID, botIDs);
//        return botIDList.contains(botID);
//    }

    /*
    Keyboard(TEXT) UNIT
    */
    String getAuth() {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=VoGOQkYvLcjWoYOfpmlh5Eps"
                // 3. 官网获取的 Secret Key
                + "&client_secret=P1SMk24HIORpsxfcm2jNFXgaLvYV4inI";
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sbResult = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sbResult.append(line);
            }
            /*
             * 返回结果示例
             */
            JSONObject jsonObject = new JSONObject(sbResult.toString());
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            Log.w(TAG, "获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    /*
    Baidu Keyboard UNIT
     */
    public BaiduUnitAI.BestResponse getBaiduKeyboardUnit(String query) {
        switch (mBaiduUnitType) {
            case BAIDU_UNIT_TYPE_KEYBOARD_BOT:
                return getBaiduKeyBoardBotUnit(query);
            case BAIDU_UNIT_TYPE_KEYBOARD_ROBOT:
                return getBaiduKeyBoardRobotUnit(query);
        }

        return null;
    }

    //baidu Bot Unit for keyboard
    private BaiduUnitAI.BestResponse getBaiduKeyBoardBotUnit(String query) {
        if (null != mBaiduBotKeyboardUnit) {
            return mBaiduBotKeyboardUnit.getBaiduKeyBoardBotUnit(query);
        } else {
            return null;
        }
    }

    //baidu robot unit for keyboard
    private BaiduUnitAI.BestResponse getBaiduKeyBoardRobotUnit(String query) {
        if (null != mBaiduRobotKeyboardUnit) {
            return mBaiduRobotKeyboardUnit.getBaiduKeyBoardRobotUnit(query);
        } else {
            return null;
        }
    }

    //local action
//    void triggerLocalEvent(ParseJson.SimpleResponseAction action,
//                           ArrayList<ParseJson.Base_Response_Schema_Slots> slotList,
//                           BestResponse bestResponse) {
//        if (BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_PHONE.equals(action.mBotID)) { //打电话
//            bestResponse.mBotSessionID = "";
//            PhoneAction phoneAction = new PhoneAction(mContext);
////            phoneAction.mAction = action.mIntent;
//            String event = action.mIntent, targetName = "", targetPhoneNumber = "";
//            for (ParseJson.Base_Response_Schema_Slots slot : slotList) {
//                if ("user_call_target".equals(slot.mName)) {
//                    targetName = slot.mOriginalWord;
//                }
//                if ("user_phone_number".equals(slot.mName)) {
//                    targetPhoneNumber = slot.mOriginalWord;
//                }
//            }
//            phoneAction.action(event, targetName, targetPhoneNumber);
//        }
//    }
}
