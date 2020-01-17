package com.min.aiassistant.speechaction;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;

import java.util.ArrayList;

public class TranslateFixAction extends FixBaseAction {
    public static final int TRANSLATE = 1;
    public static final int REVERSE_TRANSLATE = 2;
    private static final String DEFAULT_TRANSLATE_TARGET_LANGUAGE = "英语";
    private final String DEFAULT_TRANSLATE_ORIGINAL_LANGUAGE = "汉语";
    private String[] REGEX_TRANSLATE_START;
    private String[] KEYWORD_TRANSLATE_STOP;
    private String[] KEYWORD_TRANSLATE_LANGUAGE_TYPE;

    private Context mContext;
    final private String mOriginalLanguage = DEFAULT_TRANSLATE_ORIGINAL_LANGUAGE;
    private String mTargetLanguage = DEFAULT_TRANSLATE_TARGET_LANGUAGE;
    private int mCurTranslateType = TRANSLATE;

    public TranslateFixAction(Context context) {
        mBotID = BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE;
        mContext = context;
        REGEX_TRANSLATE_START = mContext.getResources().getStringArray(R.array.translate_action_start_regex);
        KEYWORD_TRANSLATE_STOP = mContext.getResources().getStringArray(R.array.translate_action_stop_keyword);
        KEYWORD_TRANSLATE_LANGUAGE_TYPE = mContext.getResources().getStringArray(R.array.translate_action_language_keyword);
    }

    @Override
    public int checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!mActive) {
            String language = CommonUtil.getRegexMatch(query, REGEX_TRANSLATE_START, 1);
            if (null == language) return NONE_FIX_ACTION;

            if (language.equals("")) {
                language = DEFAULT_TRANSLATE_TARGET_LANGUAGE;
            } else {
                if (!CommonUtil.isEqualsKeyWord(language, KEYWORD_TRANSLATE_LANGUAGE_TYPE)) {
                    bestResponse.reset();
                    bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_fix_translate_language_error), language);
                    return NONE_FIX_ACTION;
                }
            }

            mActive = true;
            mTargetLanguage = language;
            bestResponse.reset();
            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_fix_translate_start), mTargetLanguage);
            return START_FIX_ACTION;
        }

        if (CommonUtil.isEqualsKeyWord(query, KEYWORD_TRANSLATE_STOP)) {
            mActive = false;
            bestResponse.reset();
            bestResponse.mAnswer = String.format(mContext.getString(R.string.baidu_unit_fix_translate_stop), mTargetLanguage);
            bestResponse.mHint = "";
            return STOP_FIX_ACTION;
        } else {
            bestResponse.mHint = "";
            return RUNNING_FIX_ACTION;
        }
    }

    @Override
    public String getAdjustQuery(String query) {
//        翻译我爱你英语
//        目前只支持中文翻译成外文，需要添加关键字"翻译下"
        if (TRANSLATE == mCurTranslateType) {
            return mTargetLanguage + "翻译下" + query;
        } else if (REVERSE_TRANSLATE == mCurTranslateType) {
            return mOriginalLanguage + "翻译下" + query;
        } else {
            return "翻译下" + query;
        }
    }

    @Override
    public String[] getAllQuestions(String query) {
        String[] PUNCS = new String[] {",", ".", "?", "!", ";", ":", "，", "。", "？", "！", "；", "："};
//        return query.split("\\,|\\.|\\?|\\!|\\;|\\，|\\。|\\？|\\！|\\；");
        ArrayList<String> queryList = new ArrayList<>();
        int startPos = 0;
        for (int i = 0; i < query.length(); i++) {
            if (CommonUtil.isContainKeyWord(query.substring(i, i+1), PUNCS)) {
                String shortSentence = query.substring(startPos, i+1).trim();
                if (TRANSLATE == mCurTranslateType) {
                    if (shortSentence.length() > 5) {
                        queryList.add(shortSentence);
                        startPos = i + 1;
                    }
                } else {
                    String[] words = shortSentence.split(" +");
                    if (words.length > 3) {
                        queryList.add(shortSentence);
                        startPos = i + 1;
                    }
                }
            } else if (i == query.length() - 1 ) {
                String shortSentence = query.substring(startPos).trim();
                if (CommonUtil.filterPunctuation(shortSentence).length() > 0) {
                    queryList.add(shortSentence);
                }
            }
        }

        return queryList.toArray(new String[0]);
    }

    public void setTargetLanguage(String language) {
        if (TextUtils.isEmpty(language)) {
            mTargetLanguage = DEFAULT_TRANSLATE_TARGET_LANGUAGE;//"英语" 目前双向翻译只支持英语
        } else {
            mTargetLanguage = language;
        }
    }

    public String getTargetLanguage() {
        return mTargetLanguage;
    }

    public String getOriginalLanguage() {
        return mOriginalLanguage;
    }

    public void setTranslateType(int type) {
        mCurTranslateType = type;
    }

    public int getTranslateType() {
        return mCurTranslateType;
    }
}
