package com.fih.featurephone.voiceassistant.baidu.unit;

import android.content.Context;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduUtil;
import com.fih.featurephone.voiceassistant.speechaction.BaseAction;
import com.fih.featurephone.voiceassistant.speechaction.BrightnessAction;
import com.fih.featurephone.voiceassistant.speechaction.CoupletFixAction;
import com.fih.featurephone.voiceassistant.speechaction.FixBaseAction;
import com.fih.featurephone.voiceassistant.speechaction.LaunchAppAction;
import com.fih.featurephone.voiceassistant.speechaction.LaunchCameraAppAction;
import com.fih.featurephone.voiceassistant.speechaction.LaunchMusicAppAction;
import com.fih.featurephone.voiceassistant.speechaction.OCRAction;
import com.fih.featurephone.voiceassistant.speechaction.PhoneAction;
import com.fih.featurephone.voiceassistant.speechaction.PoemFixAction;
import com.fih.featurephone.voiceassistant.speechaction.TranslateFixAction;
import com.fih.featurephone.voiceassistant.speechaction.VoiceVolumeAction;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BaiduUnitAI {
    public static final int BAIDU_UNIT_TYPE_ASR_BOT = 1;
//    public static final int BAIDU_UNIT_TYPE_ASR_ROBOT = 2;
    public static  final int BAIDU_UNIT_TYPE_KEYBOARD_BOT = 3;
    public static  final int BAIDU_UNIT_TYPE_KEYBOARD_ROBOT = 4;

    public static final int BAIDU_UNIT_ROBOT_TYPE_WEB = 1;
    public static final int BAIDU_UNIT_ROBOT_TYPE_LOCAL = 2;
    public static final int BAIDU_UNIT_ROBOT_TYPE_ALL = 3;

    public static final int BAIDU_UNIT_SPEECH_ENGLISH = 1;
    public static final int BAIDU_UNIT_SPEECH_CHINESE = 2;
    public static final int BAIDU_UNIT_SPEECH_SICHUANESE = 3;
    public static final int BAIDU_UNIT_SPEECH_CANTONESE = 4;

    public static final String BAIDU_UNIT_BOT_TYPE_WEATHER = "81459";
    public static final String BAIDU_UNIT_BOT_TYPE_IA = "81485";
    public static final String BAIDU_UNIT_BOT_TYPE_DEFINITION = "81482";
    public static final String BAIDU_UNIT_BOT_TYPE_IDIOM = "81469";
    public static final String BAIDU_UNIT_BOT_TYPE_CALCULATOR = "81467";
    public static final String BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION = "81465";
    public static final String BAIDU_UNIT_BOT_TYPE_GREETING = "81460";
    public static final String BAIDU_UNIT_BOT_TYPE_CHAT = "81461";
    public static final String BAIDU_UNIT_BOT_TYPE_POEM = "81481";
    public static final String BAIDU_UNIT_BOT_TYPE_COUPLET = "81476";
    public static final String BAIDU_UNIT_BOT_TYPE_TRANSLATE = "84536";
    public static final String BAIDU_UNIT_BOT_TYPE_JOKE = "87833";

//    static final String BAIDU_UNIT_BOT_TYPE_PHONE = "81487";
//    static final String BAIDU_UNIT_BOT_TYPE_MESSAGE = "81473";
//    static final String BAIDU_UNIT_BOT_TYPE_SCREEN_CONTROL = "81472";
//    static final String BAIDU_UNIT_BOT_TYPE_STORY = "81479";
//    static final String BAIDU_UNIT_BOT_TYPE_MUSIC = "81483";
//    static final String BAIDU_UNIT_BOT_TYPE_ALARM = "81478";
//    static final String BAIDU_UNIT_BOT_TYPE_NOTIFY = "81475";
//    static final String BAIDU_UNIT_BOT_TYPE_MOVIE = "81462";
//    static final String BAIDU_UNIT_BOT_TYPE_TRAIN_TICKET = "81464";
//    static final String BAIDU_UNIT_BOT_TYPE_RADIO = "81470";

    //OK: 81459(天气), 81461(闲聊), 81469(成语问答), 81465(单位换算), 81476(智能对联), 81481(智能写诗), 81467(计算器), 81482(名词解释), 81485(智能问答), 81460(问候)
    private final Map<String, String> WEB_BOTID_MAP = new HashMap<>();
    //Pending: 81472(屏幕控制), 81473(发短信), 81487(打电话), 81475(提醒), 81478(闹钟), 81483(音乐), 81479(故事), 81462(电影), 81464(火车票)
//    private final Map<String, String> LOCAL_BOTID_MAP = new HashMap<String, String>();

    protected Context mContext;
    private BaiduRobotKeyboardUnit mBaiduRobotKeyboardUnit;
    private BaiduBotKeyboardUnit mBaiduBotKeyboardUnit;
    private BaiduBotASRUnit mBaiduBotASRUnit;
    private int mBaiduUnitType;
    private TranslateFixAction mTranslateFixAction;
    private ExecutorService mUnitExecutorService = Executors.newSingleThreadExecutor();
    private Future mUnitTaskFuture;

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

    public interface OnUnitListener {
        void onShowDebugInfo(String info, boolean reset);
        void onExit();
        void onFinalResult(String question, String answer, String hint);
        void onNotify(String botID, int type);
    }

    public BaiduUnitAI(Context context, OnUnitListener listener,
                       int baiduUnitType, int robotType, ArrayList<String> botTypeList) {
        mContext = context;
        mBaiduUnitType = baiduUnitType;
        initValues();

        BestResponse bestResponse = new BestResponse();
        bestResponse.reset();

        ArrayList<FixBaseAction> fixActionList = new ArrayList<>();
        mTranslateFixAction = new TranslateFixAction(mContext);
        fixActionList.add(mTranslateFixAction);
        fixActionList.add(new PoemFixAction(mContext));
        fixActionList.add(new CoupletFixAction(mContext));

        ArrayList<BaseAction> localActionList = new ArrayList<>();
        localActionList.add(new PhoneAction(mContext));
        localActionList.add(new WebSearchAction(mContext));
        localActionList.add(new VoiceVolumeAction(mContext));
        localActionList.add(new BrightnessAction(mContext));
        localActionList.add(new OCRAction(mContext));
        localActionList.add(new LaunchCameraAppAction(mContext));
        localActionList.add(new LaunchMusicAppAction(mContext));
        localActionList.add(new LaunchAppAction(mContext));

        switch (mBaiduUnitType) {
            case BAIDU_UNIT_TYPE_ASR_BOT:
                mBaiduBotASRUnit = new BaiduBotASRUnit(this, bestResponse, botTypeList, context, listener);
                break;
            case BAIDU_UNIT_TYPE_KEYBOARD_BOT:
                mBaiduBotKeyboardUnit = new BaiduBotKeyboardUnit(this, bestResponse, botTypeList, listener, fixActionList, localActionList);
                break;
            case BAIDU_UNIT_TYPE_KEYBOARD_ROBOT:
                mBaiduRobotKeyboardUnit = new BaiduRobotKeyboardUnit(this, bestResponse, robotType, listener);
                break;
        }
    }

    private void initValues() {
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_GREETING, mContext.getString(R.string.baidu_unit_bot_greet));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_CHAT, mContext.getString(R.string.baidu_unit_bot_chat));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_IDIOM, mContext.getString(R.string.baidu_unit_bot_idiom));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION, mContext.getString(R.string.baidu_unit_bot_unit_conversion));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_WEATHER, mContext.getString(R.string.baidu_unit_bot_weather));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_CALCULATOR, mContext.getString(R.string.baidu_unit_bot_calculator));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_DEFINITION, mContext.getString(R.string.baidu_unit_bot_definition));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_IA, mContext.getString(R.string.baidu_unit_bot_ia));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_TRANSLATE, mContext.getString(R.string.baidu_unit_bot_translate));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_POEM, mContext.getString(R.string.baidu_unit_bot_poem));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_COUPLET, mContext.getString(R.string.baidu_unit_bot_couplet));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_JOKE, mContext.getString(R.string.baidu_unit_bot_joke));

//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_SCREEN_CONTROL, mContext.getString(R.string.baidu_unit_bot_screen_control));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_MESSAGE, mContext.getString(R.string.baidu_unit_bot_message));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_PHONE, mContext.getString(R.string.baidu_unit_bot_phone));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_NOTIFY, mContext.getString(R.string.baidu_unit_bot_notify));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_ALARM, mContext.getString(R.string.baidu_unit_bot_alarm));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_MOVIE, mContext.getString(R.string.baidu_unit_bot_movie));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_MUSIC, mContext.getString(R.string.baidu_unit_bot_music));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_STORY, mContext.getString(R.string.baidu_unit_bot_story));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_TRAIN_TICKET, mContext.getString(R.string.baidu_unit_bot_train_ticket));
//        LOCAL_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_RADIO, mContext.getString(R.string.baidu_unit_bot_radio));
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

/*    public void stopBaiduASRUnit() {
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.stopBaiduASRUnit();
        }
    }*/

    public void cancelBaiduASRUnit() {
        if (null != mBaiduBotASRUnit) {
            mBaiduBotASRUnit.cancelBaiduASRUnit();
        }
    }

    public TranslateFixAction getTranslateFixAction() {
        return mTranslateFixAction;
    }

    class BestBotID {
        String mBotID;
        String mNewQuery;

        BestBotID(String botID, String newQuery) {
            mBotID = botID;
            mNewQuery = newQuery;
        }
    }

    ArrayList<BestBotID> getBestBotID(String rawQuery) {
//    private String[] IAQ = new String[]{"多大", "多高", "多远", "多深", "是啥", "是什么", "解释", "意思是", "是啥"};
        ArrayList<BestBotID> bestBotIDList = new ArrayList<>();

        rawQuery = CommonUtil.filterPunctuation(rawQuery);//去掉符号等干扰

        if (checkTranslate(rawQuery)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_TRANSLATE, null));//(智能翻译)
        }

        final String[] CALCULATOR_REGEX = mContext.getResources().getStringArray(R.array.calculator_regex);
        if (CommonUtil.checkRegexMatch(rawQuery, CALCULATOR_REGEX)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_CALCULATOR, null));//(计算器)
        } else {
            final String[] UNIT_CONVERSION_REGEX = mContext.getResources().getStringArray(R.array.unit_conversion_regex);
            if (CommonUtil.checkRegexMatch(rawQuery, UNIT_CONVERSION_REGEX)) {
                bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION, null));//(单位换算)
            }
        }

        if (checkWeather(rawQuery)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_WEATHER, null));//(天气)
        }

        if (checkJoke(rawQuery)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_JOKE, null));//(笑话)
        }

        String[] result = new String[2];
        if (checkPoem(rawQuery, result)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_POEM, result[1]));//(智能写诗)
        } else {
            if (checkCouplet(rawQuery, result)) {
                bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_COUPLET, result[1]));//(智能对联)
            }
        }
        return bestBotIDList;
    }

    private boolean checkTranslate(String query) {
        final String[] TRANSLATE_REGEX = mContext.getResources().getStringArray(R.array.translate_regex);

        String language = CommonUtil.getRegexMatch(query, TRANSLATE_REGEX, 1);
        if (null == language) return false;
        if (language.equals("")) return true;

        final String[] TRANSLATE_LANGUAGE = mContext.getResources().getStringArray(R.array.translate_action_language_keyword);
        return CommonUtil.isEqualsKeyWord(language, TRANSLATE_LANGUAGE);
    }

    private boolean checkWeather(String query) {
        final String[] WEATHER_KEYWORD = mContext.getResources().getStringArray(R.array.weather_keyword);
        if (CommonUtil.isContainKeyWord(query, WEATHER_KEYWORD)) return true;

        final String[] WEATHER_REGEX = mContext.getResources().getStringArray(R.array.weather_regex);
        return CommonUtil.checkRegexMatch(query, WEATHER_REGEX);
    }

    private boolean checkCouplet(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] COUPLET = mContext.getResources().getStringArray(R.array.couplet_keyword);
        if (!CommonUtil.isContainKeyWord(query, COUPLET)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.couplet_regex);
        String keyWord = CommonUtil.getRegexMatch(query, REGEX, 1);
        if (TextUtils.isEmpty(keyWord)) return false;

        result[1] = keyWord;
        return true;
    }

    private boolean checkPoem(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] POEM = mContext.getResources().getStringArray(R.array.poem_keyword);
        if (!CommonUtil.isContainKeyWord(query, POEM)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.poem_regex);
        String keyWord = CommonUtil.getRegexMatch(query, REGEX, 1);
        if (TextUtils.isEmpty(keyWord)) return false;

        result[1] = keyWord;
        return true;
    }

    private boolean checkJoke(String query) {
        query = CommonUtil.filterPunctuation(query);
        final String[] JOKE = mContext.getResources().getStringArray(R.array.joke_keyword);
        if (!CommonUtil.isContainKeyWord(query, JOKE)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.joke_regex);
        return CommonUtil.checkRegexMatch(query, REGEX);
    }

    boolean isQuitSession(String query) {
        final String[] QUIT_LAST_BOTID = mContext.getResources().getStringArray(R.array.quit_session);
        return CommonUtil.isEqualsKeyWord(query, QUIT_LAST_BOTID);
    }

    String getBotIDLabel(String botID) {
        return WEB_BOTID_MAP.get(botID);
    }

    String getAuthToken() {
        return new BaiduUtil().getUnitToken(mContext);
    }

    /*
    Baidu Keyboard UNIT
     */
    public void getBaiduKeyboardUnitThread(final String query) {
        if (mUnitTaskFuture != null && !mUnitTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mUnitTaskFuture = mUnitExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                getBaiduKeyboardUnit(query);
            }
        });
    }

    private void getBaiduKeyboardUnit(String query) {
        switch (mBaiduUnitType) {
            case BAIDU_UNIT_TYPE_KEYBOARD_BOT://baidu Bot Unit for keyboard
                if (null != mBaiduBotKeyboardUnit) {
                    mBaiduBotKeyboardUnit.getBaiduKeyBoardBotUnit(query);
                }
                break;
            case BAIDU_UNIT_TYPE_KEYBOARD_ROBOT://baidu robot unit for keyboard
                if (null != mBaiduRobotKeyboardUnit) {
                    mBaiduRobotKeyboardUnit.getBaiduKeyBoardRobotUnit(query);
                }
                break;
        }
    }
}
