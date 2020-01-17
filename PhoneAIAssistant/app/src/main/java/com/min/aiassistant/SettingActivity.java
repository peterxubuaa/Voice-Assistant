package com.min.aiassistant;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.min.aiassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.min.aiassistant.baidu.speech.BaiduSpeechAI;
import com.min.aiassistant.baidu.tts.BaiduTTSAI;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.GlobalValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingActivity extends Activity {
    static final String PREF_SETTINGS = "pref_settings";
    static final String PREF_TTS = "pref_tts";
    static final String PREF_REQUEST_IMAGE = "perf_request_image";
    static final String PREF_ENABLE_EXTRA_FUN = "pref_enable_extra_fun";
    static final String PREF_FACE_IDENTIFY_WORK_MODE = "pref_face_identify_work_mode";
    static final String PREF_UNIT_BOT_TYPE = "pref_unit_bot_type";
    static final String PREF_SPEECH_TYPE = "pref_speech_type";
    static final String PREF_TTS_TYPE = "pref_tts_type";
    static final String PREF_CLASSIFY_IMAGE_TYPE = "pref_classify_image_type";

    static final int NONE = 0;

    private SettingResult mOldSettingResult;

    public static class SettingResult implements Cloneable {
        boolean mTTS;
        boolean mRequestImage;
        boolean mEnableExtraFun;
        int mFaceIdentifyMode;
        int mSpeechType;
        int mTTSVoiceType;
        ArrayList<String> mBotTypeList;
        ArrayList<Integer> mClassifyImageTypeList;

        SettingResult() {
            mTTS = true;
            mRequestImage = false;
            mEnableExtraFun = false;
            mFaceIdentifyMode = NONE;
            mSpeechType = NONE;
            mTTSVoiceType = NONE;
            mBotTypeList = new ArrayList<>();
            mClassifyImageTypeList = new ArrayList<>();
        }

        @Override
        public Object clone() {
            SettingResult settingResult = null;
            try{
                settingResult = (SettingResult)super.clone();
            }catch(CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return settingResult;
        }

         @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SettingResult that = (SettingResult) o;
            return mTTS == that.mTTS &&
                    mRequestImage == that.mRequestImage &&
                    mEnableExtraFun == that.mEnableExtraFun &&
                    mFaceIdentifyMode == that.mFaceIdentifyMode &&
                    mSpeechType == that.mSpeechType &&
                    mTTSVoiceType == that.mTTSVoiceType &&
                    mBotTypeList.equals(that.mBotTypeList) &&
                    mClassifyImageTypeList.equals(that.mClassifyImageTypeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mFaceIdentifyMode, mSpeechType, mTTSVoiceType, mBotTypeList, mClassifyImageTypeList);
        }
    }

    private final Map<Integer, String> WEB_BOTID_RID_MAP = new HashMap<Integer, String>() {
        {
            put(R.id.baidu_unit_bot_weather, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_WEATHER);
            put(R.id.baidu_unit_bot_ia, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA);
            put(R.id.baidu_unit_bot_word_definition, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_WORD_DEFINITION);
            put(R.id.baidu_unit_bot_idiom, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IDIOM);
            put(R.id.baidu_unit_bot_calculator, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CALCULATOR);
            put(R.id.baidu_unit_bot_unit_conversion, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_UNIT_CONVERSION);
            put(R.id.baidu_unit_bot_greet, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_GREETING);
            put(R.id.baidu_unit_bot_chat, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_CHAT);
            put(R.id.baidu_unit_bot_poem, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_POEM);
            put(R.id.baidu_unit_bot_couplet, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_COUPLET);
            put(R.id.baidu_unit_bot_translate, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRANSLATE);
            put(R.id.baidu_unit_bot_joke, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_JOKE);
            put(R.id.baidu_unit_bot_garbage, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_GARBAGE);
        }
    };

    private final Map<Integer, Integer> CLASSIFY_IMAGE_RID_MAP = new HashMap<Integer, Integer>() {
        {
            put(R.id.baidu_classify_image_general_type, BaiduClassifyImageAI.CLASSIFY_TYPE_ADVANCED_GENERAL);
            put(R.id.baidu_classify_image_plant_type, BaiduClassifyImageAI.CLASSIFY_TYPE_PLANT);
            put(R.id.baidu_classify_image_car_type, BaiduClassifyImageAI.CLASSIFY_TYPE_CAR);
            put(R.id.baidu_classify_image_dish_type, BaiduClassifyImageAI.CLASSIFY_TYPE_DISH);
            put(R.id.baidu_classify_image_red_wine_type, BaiduClassifyImageAI.CLASSIFY_TYPE_RED_WINE);
            put(R.id.baidu_classify_image_logo_type, BaiduClassifyImageAI.CLASSIFY_TYPE_LOGO);
            put(R.id.baidu_classify_image_animal_type, BaiduClassifyImageAI.CLASSIFY_TYPE_ANIMAL);
            put(R.id.baidu_classify_image_ingredient_type, BaiduClassifyImageAI.CLASSIFY_TYPE_INGREDIENT);
            put(R.id.baidu_classify_image_landmark_type, BaiduClassifyImageAI.CLASSIFY_TYPE_LANDMARK);
            put(R.id.baidu_classify_image_currency_type, BaiduClassifyImageAI.CLASSIFY_TYPE_CURRENCY);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mOldSettingResult = getSavedSettingResults(this);
        setUIValue(mOldSettingResult);

        findViewById(R.id.setting_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingResult newSettingResult = getUIValue();
                setSettingResults(newSettingResult);

                if (mOldSettingResult.equals(newSettingResult)) {
                    setResult(Activity.RESULT_CANCELED);
                } else {
                    setResult(Activity.RESULT_OK);
                }
                finish();
            }
        });

        findViewById(R.id.setting_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    public static SettingResult getSavedSettingResults(Context context) {
        SettingResult settingResult = new SettingResult();

        SharedPreferences settingPrefs = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        settingResult.mTTS = settingPrefs.getBoolean(PREF_TTS, true);
        settingResult.mRequestImage = settingPrefs.getBoolean(PREF_REQUEST_IMAGE, false);
        settingResult.mEnableExtraFun = settingPrefs.getBoolean(PREF_ENABLE_EXTRA_FUN, false);
        settingResult.mFaceIdentifyMode = settingPrefs.getInt(PREF_FACE_IDENTIFY_WORK_MODE, GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE);
        settingResult.mSpeechType = settingPrefs.getInt(PREF_SPEECH_TYPE, BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
        settingResult.mTTSVoiceType = settingPrefs.getInt(PREF_TTS_TYPE, BaiduTTSAI.BAIDU_TTS_LADY);
        settingResult.mBotTypeList = CommonUtil.getListFromString(settingPrefs.getString(PREF_UNIT_BOT_TYPE,
                "81459,81485,81469,81467,81465,84536,87833,81481,81482,81476,1010930"));
        settingResult.mClassifyImageTypeList = CommonUtil.getNumListFromString(settingPrefs.getString(PREF_CLASSIFY_IMAGE_TYPE,
                "0,1,2,3"));//CLASSIFY_TYPE_ADVANCED_GENERAL, CLASSIFY_TYPE_PLANT, CLASSIFY_TYPE_CAR, CLASSIFY_TYPE_DISH
        return settingResult;
    }

    private void setSettingResults(SettingResult settingResult) {
        SharedPreferences settingPrefs = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();

        settingEditor.putBoolean(PREF_TTS, settingResult.mTTS);
        settingEditor.putBoolean(PREF_REQUEST_IMAGE, settingResult.mRequestImage);
        settingEditor.putBoolean(PREF_ENABLE_EXTRA_FUN, settingResult.mEnableExtraFun);
        settingEditor.putInt(PREF_FACE_IDENTIFY_WORK_MODE, settingResult.mFaceIdentifyMode);
        settingEditor.putInt(PREF_SPEECH_TYPE, settingResult.mSpeechType);
        settingEditor.putInt(PREF_TTS_TYPE, settingResult.mTTSVoiceType);
        settingEditor.putString(PREF_UNIT_BOT_TYPE, CommonUtil.getStringFromList(settingResult.mBotTypeList));

        Collections.sort(settingResult.mClassifyImageTypeList);//从小到大排列的
        settingEditor.putString(PREF_CLASSIFY_IMAGE_TYPE, CommonUtil.getStringFromNumList(settingResult.mClassifyImageTypeList));

        settingEditor.apply();
    }

    private void setUIValue(SettingResult settingResult) {
        ((CheckBox)findViewById(R.id.baidu_tts)).setChecked(settingResult.mTTS);
        ((CheckBox)findViewById(R.id.baidu_request_image)).setChecked(settingResult.mRequestImage);
        ((CheckBox)findViewById(R.id.baidu_enable_extra_fun)).setChecked(settingResult.mEnableExtraFun);
        setFaceIdentifyWorkMode(settingResult.mFaceIdentifyMode);
        setUnitSpeechType(settingResult.mSpeechType);
        setUnitTTSVoiceType(settingResult.mTTSVoiceType);
        setUnitBotTypeList(settingResult.mBotTypeList);
        setClassifyImageTypeList(settingResult.mClassifyImageTypeList);
    }

    private SettingResult getUIValue() {
        SettingResult settingResult = new SettingResult();
        settingResult.mTTS = ((CheckBox)findViewById(R.id.baidu_tts)).isChecked();
        settingResult.mRequestImage = ((CheckBox)findViewById(R.id.baidu_request_image)).isChecked();
        settingResult.mEnableExtraFun = ((CheckBox)findViewById(R.id.baidu_enable_extra_fun)).isChecked();
        settingResult.mFaceIdentifyMode = getFaceIdentifyWorkMode();
        settingResult.mSpeechType = getUnitSpeechType();
        settingResult.mTTSVoiceType = getUnitTTSVoiceType();
        settingResult.mBotTypeList = getUnitBotTypeList();
        settingResult.mClassifyImageTypeList = getClassifyImageTypeList();

        return settingResult;
    }

    private int getFaceIdentifyWorkMode() {
        RadioGroup radioGroup = findViewById(R.id.baidu_face_identify_work_mode);
        int selID = radioGroup.getCheckedRadioButtonId();
        int workMode = NONE;
        switch (selID) {
            case R.id.baidu_online_1N_face_identify:
                workMode = GlobalValue.FACE_IDENTIFY_ONLINE_1N;
                break;
            case R.id.baidu_online_MN_face_identify:
                workMode = GlobalValue.FACE_IDENTIFY_ONLINE_MN;
                break;
            case R.id.baidu_content_approve_face_identify:
                workMode = GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE;
                break;
        }
        return workMode;
    }

    private void setFaceIdentifyWorkMode(int workMode) {
        RadioGroup radioGroup = findViewById(R.id.baidu_face_identify_work_mode);
        if (GlobalValue.FACE_IDENTIFY_ONLINE_1N == workMode) {
            radioGroup.check(R.id.baidu_online_1N_face_identify);
        } else if (GlobalValue.FACE_IDENTIFY_ONLINE_MN == workMode) {
            radioGroup.check(R.id.baidu_online_MN_face_identify);
        } else if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == workMode) {
            radioGroup.check(R.id.baidu_content_approve_face_identify);
        }
    }

    private void setUnitSpeechType(int speechType) {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_speech_language_type);
        if (BaiduSpeechAI.BAIDU_SPEECH_CHINESE == speechType) {
            radioGroup.check(R.id.baidu_unit_speech_language_chinese);
        } else if (BaiduSpeechAI.BAIDU_SPEECH_ENGLISH == speechType) {
            radioGroup.check(R.id.baidu_unit_speech_language_english);
        } else if (BaiduSpeechAI.BAIDU_SPEECH_SICHUANESE == speechType) {
            radioGroup.check(R.id.baidu_unit_speech_language_sichuanese);
        } else if (BaiduSpeechAI.BAIDU_SPEECH_CANTONESE == speechType) {
            radioGroup.check(R.id.baidu_unit_speech_language_cantonese);
        }
    }

    private int getUnitSpeechType() {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_speech_language_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int speechType = NONE;
        switch (selID) {
            case R.id.baidu_unit_speech_language_chinese:
                speechType = BaiduSpeechAI.BAIDU_SPEECH_CHINESE;
                break;
            case R.id.baidu_unit_speech_language_english:
                speechType = BaiduSpeechAI.BAIDU_SPEECH_ENGLISH;
                break;
            case R.id.baidu_unit_speech_language_sichuanese:
                speechType = BaiduSpeechAI.BAIDU_SPEECH_SICHUANESE;
                break;
            case R.id.baidu_unit_speech_language_cantonese:
                speechType = BaiduSpeechAI.BAIDU_SPEECH_CANTONESE;
                break;
            default:
                break;
        }
        return speechType;
    }

    private void setUnitTTSVoiceType(int ttsVoiceType) {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_tts_voice_type);
        if (BaiduTTSAI.BAIDU_TTS_LADY == ttsVoiceType) {
            radioGroup.check(R.id.baidu_unit_tts_voice_lady);
        } else if (BaiduTTSAI.BAIDU_TTS_GENTLEMAN == ttsVoiceType) {
            radioGroup.check(R.id.baidu_unit_tts_voice_gentleman);
        } else if (BaiduTTSAI.BAIDU_TTS_GIRL == ttsVoiceType) {
            radioGroup.check(R.id.baidu_unit_tts_voice_girl);
        } else if (BaiduTTSAI.BAIDU_TTS_BOY == ttsVoiceType) {
            radioGroup.check(R.id.baidu_unit_tts_voice_boy);
        }
    }

    private int getUnitTTSVoiceType() {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_tts_voice_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int ttsVoiceType = NONE;
        switch (selID) {
            case R.id.baidu_unit_tts_voice_lady:
                ttsVoiceType = BaiduTTSAI.BAIDU_TTS_LADY;
                break;
            case R.id.baidu_unit_tts_voice_gentleman:
                ttsVoiceType = BaiduTTSAI.BAIDU_TTS_GENTLEMAN;
                break;
            case R.id.baidu_unit_tts_voice_girl:
                ttsVoiceType = BaiduTTSAI.BAIDU_TTS_GIRL;
                break;
            case R.id.baidu_unit_tts_voice_boy:
                ttsVoiceType = BaiduTTSAI.BAIDU_TTS_BOY;
                break;
            default:
                break;
        }
        return ttsVoiceType;
    }

    private ArrayList<String> getUnitBotTypeList() {
        ArrayList<String> botTypeList = new ArrayList<>();
        //web bot
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            if (((CheckBox)findViewById(r_id)).isChecked()) {
                botTypeList.add(WEB_BOTID_RID_MAP.get(r_id));
            }
        }

        return botTypeList;
    }

    private void setUnitBotTypeList(ArrayList<String> botTypeList) {
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }

        if (null == botTypeList) return;
        for (String botType : botTypeList) {
            //web bot
            for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
                if (botType.equals(WEB_BOTID_RID_MAP.get(r_id))) {
                    ((CheckBox)findViewById(r_id)).setChecked(true);
                    break;
                }
            }
        }
    }

    private ArrayList<Integer> getClassifyImageTypeList() {
        ArrayList<Integer> classifyImageTypeList = new ArrayList<>();
        for (int r_id : CLASSIFY_IMAGE_RID_MAP.keySet()) {
            if (((CheckBox) findViewById(r_id)).isChecked()) {
                classifyImageTypeList.add(CLASSIFY_IMAGE_RID_MAP.get(r_id));
            }
        }
        return classifyImageTypeList;
    }

    private void setClassifyImageTypeList(ArrayList<Integer> classifyImageTypeList) {
        for (int r_id : CLASSIFY_IMAGE_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }

        if (null == classifyImageTypeList) return;
        for (Integer classifyImageType : classifyImageTypeList) {
            for (int r_id : CLASSIFY_IMAGE_RID_MAP.keySet()) {
                if (classifyImageType.equals(CLASSIFY_IMAGE_RID_MAP.get(r_id))) {
                    ((CheckBox) findViewById(r_id)).setChecked(true);
                    break;
                }
            }
        }
    }
}