package com.min.aiassistant.baidu.unit;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.unit.model.BaiduBotKeyboardUnit;
import com.min.aiassistant.speechaction.BaseAction;
import com.min.aiassistant.speechaction.BrightnessAction;
import com.min.aiassistant.speechaction.CoupletFixAction;
import com.min.aiassistant.speechaction.FixBaseAction;
import com.min.aiassistant.speechaction.LaunchAppAction;
import com.min.aiassistant.speechaction.LaunchCameraAppAction;
import com.min.aiassistant.speechaction.LaunchMusicAppAction;
import com.min.aiassistant.speechaction.PhoneAction;
import com.min.aiassistant.speechaction.PoemFixAction;
import com.min.aiassistant.speechaction.RecognizeAction;
import com.min.aiassistant.speechaction.TranslateFixAction;
import com.min.aiassistant.speechaction.VoiceVolumeAction;
import com.min.aiassistant.speechaction.WebSearchAction;
import com.min.aiassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaiduUnitAI extends BaiduBaseAI {
    public static final int UNIT_NOTIFY = 1;
    public static final int UNIT_RESULT = 2;

    public static final String BAIDU_UNIT_BOT_TYPE_WEATHER = "81459";
    public static final String BAIDU_UNIT_BOT_TYPE_IA = "81485";
    public static final String BAIDU_UNIT_BOT_TYPE_WORD_DEFINITION = "81482";
    public static final String BAIDU_UNIT_BOT_TYPE_IDIOM = "81469";
    public static final String BAIDU_UNIT_BOT_TYPE_CALCULATOR = "81467";
    public static final String BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION = "81465";
    public static final String BAIDU_UNIT_BOT_TYPE_GREETING = "81460";
    public static final String BAIDU_UNIT_BOT_TYPE_CHAT = "81461";
    public static final String BAIDU_UNIT_BOT_TYPE_POEM = "81481";
    public static final String BAIDU_UNIT_BOT_TYPE_COUPLET = "81476";
    public static final String BAIDU_UNIT_BOT_TYPE_TRANSLATE = "84536";
    public static final String BAIDU_UNIT_BOT_TYPE_JOKE = "87833";
    public static final String BAIDU_UNIT_BOT_TYPE_GARBAGE = "1010930";

    //OK: 81459(天气), 81461(闲聊), 81469(成语问答), 81465(单位换算), 81476(智能对联), 81481(智能写诗), 81467(计算器), 81482(名词解释), 81485(智能问答), 81460(问候)
    private final Map<String, String> WEB_BOTID_MAP = new HashMap<>();

    public Context mContext;
    private BaiduBotKeyboardUnit mBaiduBotKeyboardUnit;
    private TranslateFixAction mTranslateFixAction;
    private FixBaseAction mPoemFixAction, mCoupletFixAction;
    private ExecutorService mUnitExecutorService;

    static public class BestResponse {
        public String mBotID;
        String mIntent;
        public String mBotIDLabel;
        public String mBotSessionID;
        public String mBotSession;
        String mSession;
        String mSessionID;
        public String mRawQuery;
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
    }

    public BaiduUnitAI(Context context, IBaiduBaseListener listener, ArrayList<String> botTypeList) {
        mUnitExecutorService = Executors.newSingleThreadExecutor();
//        mUnitExecutorService = Executors.newFixedThreadPool(10);

        mContext = context;
        initValues();

        ArrayList<FixBaseAction> fixActionList = new ArrayList<>();
        mTranslateFixAction = new TranslateFixAction(mContext);
        fixActionList.add(mTranslateFixAction);
        mPoemFixAction = new PoemFixAction(mContext);
        fixActionList.add(mPoemFixAction);
        mCoupletFixAction = new CoupletFixAction(mContext);
        fixActionList.add(mCoupletFixAction);

        ArrayList<BaseAction> localActionList = new ArrayList<>();
        localActionList.add(new PhoneAction(mContext));
        localActionList.add(new WebSearchAction(mContext));
        localActionList.add(new VoiceVolumeAction(mContext));
        localActionList.add(new RecognizeAction(mContext));
        localActionList.add(new LaunchCameraAppAction(mContext));
        localActionList.add(new LaunchMusicAppAction(mContext));
        localActionList.add(new LaunchAppAction(mContext));
        if (CommonUtil.isSystemApp(mContext, mContext.getPackageName())) {
            localActionList.add(new BrightnessAction(mContext));
        }

        BestResponse bestResponse = new BestResponse();
        bestResponse.reset();

        mBaiduBotKeyboardUnit = new BaiduBotKeyboardUnit(this, listener, bestResponse, botTypeList, fixActionList, localActionList);
    }

    private void initValues() {
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_GREETING, mContext.getString(R.string.baidu_unit_bot_greet));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_CHAT, mContext.getString(R.string.baidu_unit_bot_chat));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_IDIOM, mContext.getString(R.string.baidu_unit_bot_idiom));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION, mContext.getString(R.string.baidu_unit_bot_unit_conversion));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_WEATHER, mContext.getString(R.string.baidu_unit_bot_weather));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_CALCULATOR, mContext.getString(R.string.baidu_unit_bot_calculator));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_WORD_DEFINITION, mContext.getString(R.string.baidu_unit_bot_word_definition));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_IA, mContext.getString(R.string.baidu_unit_bot_ia));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_TRANSLATE, mContext.getString(R.string.baidu_unit_bot_translate));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_POEM, mContext.getString(R.string.baidu_unit_bot_poem));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_COUPLET, mContext.getString(R.string.baidu_unit_bot_couplet));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_JOKE, mContext.getString(R.string.baidu_unit_bot_joke));
        WEB_BOTID_MAP.put(BAIDU_UNIT_BOT_TYPE_GARBAGE, mContext.getString(R.string.baidu_unit_bot_garbage));
    }

    public TranslateFixAction getTranslateFixAction() {
        return mTranslateFixAction;
    }

    public FixBaseAction getPoemFixAction() {
        return mPoemFixAction;
    }

    public FixBaseAction getCoupletFixAction() {
        return mCoupletFixAction;
    }

    public class BestBotID {
        public String mBotID;
        public String mNewQuery;

        BestBotID(String botID, String newQuery) {
            mBotID = botID;
            mNewQuery = newQuery;
        }
    }

    public ArrayList<BestBotID> getBestBotID(String rawQuery) {
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

        if (checkGarbage(rawQuery)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_GARBAGE, null));//(垃圾分类)
        }

        String[] result = new String[2];
        if (checkPoem(rawQuery, result)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_POEM, result[1]));//(智能写诗)
        } else if (checkCouplet(rawQuery, result)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_COUPLET, result[1]));//(智能对联)
        } else if (checkWordDefinition(rawQuery, result)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_WORD_DEFINITION, result[1]));//(词语解释)
        } else if (checkIdiom(rawQuery, result)) {
            bestBotIDList.add(new BestBotID(BAIDU_UNIT_BOT_TYPE_IDIOM, result[1]));//(成语解释)
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

    private boolean checkGarbage(String query) {
        query = CommonUtil.filterPunctuation(query);
        final String[] GARBAGE = mContext.getResources().getStringArray(R.array.garbage_keyword);
        if (!CommonUtil.isContainKeyWord(query, GARBAGE)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.garbage_regex);
        return CommonUtil.checkRegexMatch(query, REGEX);
    }

    private boolean checkWordDefinition(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] WORD_DEFINITION = mContext.getResources().getStringArray(R.array.word_definition_keyword);
        if (!CommonUtil.isContainKeyWord(query, WORD_DEFINITION)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.word_definition_regex);
        String word = CommonUtil.getRegexMatch(query, REGEX, 1);
        if (TextUtils.isEmpty(word)) return false;

        result[1] = word;
        return true;
    }

    private boolean checkIdiom(String query, String[] result) {
        query = CommonUtil.filterPunctuation(query);
        final String[] WORD_DEFINITION = mContext.getResources().getStringArray(R.array.idiom_keyword);
        if (!CommonUtil.isContainKeyWord(query, WORD_DEFINITION)) return false;

        final String[] REGEX = mContext.getResources().getStringArray(R.array.idiom_regex);
        String word = CommonUtil.getRegexMatch(query, REGEX, 1);
        if (TextUtils.isEmpty(word)) return false;

        result[1] = "什么是" + word;
        return true;
    }

    public boolean isQuitSession(String query) {
        final String[] QUIT_LAST_BOTID = mContext.getResources().getStringArray(R.array.quit_session);
        return CommonUtil.isEqualsKeyWord(query, QUIT_LAST_BOTID);
    }

    public String getBotIDLabel(String botID) {
        return WEB_BOTID_MAP.get(botID);
    }

    /*
    Baidu Keyboard UNIT
     */
    public void action(final String query) {
        //保证逐个执行
        mUnitExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mBaiduBotKeyboardUnit.getBaiduKeyBoardBotUnit(query);
            }
        });
    }
}
