package com.fih.featurephone.voiceassistant;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.fih.featurephone.voiceassistant.baidu.imageclassify.BaiduClassifyImageAI;
import com.fih.featurephone.voiceassistant.baidu.speech.BaiduSpeechAI;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingActivity extends Activity {
    static final String PREF_SETTINGS = "pref_settings";
    static final String PREF_DEBUG = "pref_debug";
    static final String PREF_TTS = "pref_tts";
    static final String PREF_REQUEST_IMAGE = "perf_request_image";
    static final String PREF_ENABLE_EXTRA_FUN = "pref_enable_extra_fun";
    static final String PREF_FACE_IDENTIFY_WORK_MODE = "pref_face_identify_work_mode";
    static final String PREF_UNIT_WORK_MODE = "pref_unit_work_mode";
    static final String PREF_UNIT_ROBOT_TYPE = "pref_unit_robot_type";
    static final String PREF_UNIT_BOT_TYPE = "pref_unit_bot_type";
    static final String PREF_SPEECH_TYPE = "pref_speech_type";
    static final String PREF_CLASSIFY_IMAGE_TYPE = "pref_classify_image_type";

    static final int NONE = 0;

    private SettingResult mOldSettingResult;

    public static class SettingResult implements Cloneable {
        boolean mDebug;
        boolean mTTS;
        boolean mRequestImage;
        boolean mEnableExtraFun;
        int mFaceIdentifyMode;
        int mUnitType;
        int mRobotType;
        int mSpeechType;
        ArrayList<String> mBotTypeList;
        ArrayList<Integer> mClassifyImageTypeList;

        SettingResult() {
            mDebug = false;
            mTTS = true;
            mRequestImage = false;
            mEnableExtraFun = false;
            mFaceIdentifyMode = NONE;
            mUnitType = NONE;
            mRobotType = NONE;
            mSpeechType = NONE;
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
            return mDebug == that.mDebug &&
                    mTTS == that.mTTS &&
                    mRequestImage == that.mRequestImage &&
                    mEnableExtraFun == that.mEnableExtraFun &&
                    mFaceIdentifyMode == that.mFaceIdentifyMode &&
                    mUnitType == that.mUnitType &&
                    mRobotType == that.mRobotType &&
                    mSpeechType == that.mRobotType &&
                    mBotTypeList.equals(that.mBotTypeList) &&
                    mClassifyImageTypeList.equals(that.mClassifyImageTypeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mFaceIdentifyMode, mUnitType, mRobotType, mSpeechType, mBotTypeList, mClassifyImageTypeList);
        }
    }

    private final Map<Integer, String> WEB_BOTID_RID_MAP = new HashMap<Integer, String>() {
        {
            put(R.id.baidu_unit_bot_weather, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_WEATHER);
            put(R.id.baidu_unit_bot_ia, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_IA);
            put(R.id.baidu_unit_bot_definition, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_DEFINITION);
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

/*
    private final Map<Integer, String> LOCAL_BOTID_RID_MAP = new HashMap<Integer, String>() {
        {
            put(R.id.baidu_unit_bot_phone, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_PHONE);
            put(R.id.baidu_unit_bot_message, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_MESSAGE);
            put(R.id.baidu_unit_bot_screen_control, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_SCREEN_CONTROL);
            put(R.id.baidu_unit_bot_story, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_STORY);
            put(R.id.baidu_unit_bot_music, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_MUSIC);
            put(R.id.baidu_unit_bot_alarm, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_ALARM);
            put(R.id.baidu_unit_bot_notify, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_NOTIFY);
            put(R.id.baidu_unit_bot_movie, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_MOVIE);
            put(R.id.baidu_unit_bot_train_ticket, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_TRAIN_TICKET);
            put(R.id.baidu_unit_bot_radio, BaiduUnitAI.BAIDU_UNIT_BOT_TYPE_RADIO);
        }
    };
*/

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
//                moveTaskToBack(true); //key point
            }
        });

        ((RadioGroup)findViewById(R.id.baidu_unit_work_mode)).setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        switch (checkedId) {
                            case R.id.baidu_unit_asr_bot:
                            case R.id.baidu_unit_keyboard_bot:
                                enableRobotTypeSelect(false);
                                enableBotTypeSelect(true);
                                break;
                            case R.id.baidu_unit_keyboard_robot:
                                enableRobotTypeSelect(true);
                                enableBotTypeSelect(false);
                                break;
                        }
                    }
                });
    }

    public static SettingResult getSavedSettingResults(Context context) {
        SettingResult settingResult = new SettingResult();

        SharedPreferences settingPrefs = context.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        settingResult.mDebug = settingPrefs.getBoolean(PREF_DEBUG, false);
        settingResult.mTTS = settingPrefs.getBoolean(PREF_TTS, true);
        settingResult.mRequestImage = settingPrefs.getBoolean(PREF_REQUEST_IMAGE, false);
        settingResult.mEnableExtraFun = settingPrefs.getBoolean(PREF_ENABLE_EXTRA_FUN, false);
        settingResult.mFaceIdentifyMode = settingPrefs.getInt(PREF_FACE_IDENTIFY_WORK_MODE, GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE);
        settingResult.mUnitType = settingPrefs.getInt(PREF_UNIT_WORK_MODE, BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT);
        settingResult.mRobotType = settingPrefs.getInt(PREF_UNIT_ROBOT_TYPE, BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL);
        settingResult.mSpeechType = settingPrefs.getInt(PREF_SPEECH_TYPE, BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
        settingResult.mBotTypeList = CommonUtil.getListFromString(settingPrefs.getString(PREF_UNIT_BOT_TYPE,
                "81459,81485,81469,81467,81465,81461,84536,87833,81481,81476,1010930"));
        settingResult.mClassifyImageTypeList = CommonUtil.getNumListFromString(settingPrefs.getString(PREF_CLASSIFY_IMAGE_TYPE,
                "0,1,2,3"));//CLASSIFY_TYPE_ADVANCED_GENERAL, CLASSIFY_TYPE_PLANT, CLASSIFY_TYPE_CAR, CLASSIFY_TYPE_DISH
        return settingResult;
    }

    private void enableRobotTypeSelect(boolean enable) {
        findViewById(R.id.baidu_unit_robot_web).setEnabled(enable);
        findViewById(R.id.baidu_unit_robot_local).setEnabled(enable);
        findViewById(R.id.baidu_unit_robot_all).setEnabled(enable);

        findViewById(R.id.baidu_unit_robot_web).setVisibility(enable? View.VISIBLE : View.GONE);
        findViewById(R.id.baidu_unit_robot_local).setVisibility(enable? View.VISIBLE : View.GONE);
        findViewById(R.id.baidu_unit_robot_all).setVisibility(enable? View.VISIBLE : View.GONE);

        findViewById(R.id.baidu_unit_robot_type_tv).setVisibility(enable? View.VISIBLE : View.GONE);
    }

    private void enableBotTypeSelect(boolean enable) {
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            findViewById(r_id).setEnabled(enable);
            findViewById(r_id).setVisibility(enable? View.VISIBLE : View.GONE);
        }

/*
        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            findViewById(r_id).setEnabled(enable);
            findViewById(r_id).setVisibility(enable? View.VISIBLE : View.GONE);
        }
*/

        findViewById(R.id.baidu_unit_bot_type_tv).setVisibility(enable? View.VISIBLE : View.GONE);
    }

    private void setSettingResults(SettingResult settingResult) {
        SharedPreferences settingPrefs = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();

        settingEditor.putBoolean(PREF_DEBUG, settingResult.mDebug);
        settingEditor.putBoolean(PREF_TTS, settingResult.mTTS);
        settingEditor.putBoolean(PREF_REQUEST_IMAGE, settingResult.mRequestImage);
        settingEditor.putBoolean(PREF_ENABLE_EXTRA_FUN, settingResult.mEnableExtraFun);
        settingEditor.putInt(PREF_FACE_IDENTIFY_WORK_MODE, settingResult.mFaceIdentifyMode);
        settingEditor.putInt(PREF_UNIT_WORK_MODE, settingResult.mUnitType);
        settingEditor.putInt(PREF_UNIT_ROBOT_TYPE, settingResult.mRobotType);
        settingEditor.putInt(PREF_SPEECH_TYPE, settingResult.mSpeechType);
        settingEditor.putString(PREF_UNIT_BOT_TYPE, CommonUtil.getStringFromList(settingResult.mBotTypeList));

        Collections.sort(settingResult.mClassifyImageTypeList);//从小到大排列的
        settingEditor.putString(PREF_CLASSIFY_IMAGE_TYPE, CommonUtil.getStringFromNumList(settingResult.mClassifyImageTypeList));

        settingEditor.apply();
    }

    private void setUIValue(SettingResult settingResult) {
        ((CheckBox)findViewById(R.id.baidu_debug)).setChecked(settingResult.mDebug);
        ((CheckBox)findViewById(R.id.baidu_tts)).setChecked(settingResult.mTTS);
        ((CheckBox)findViewById(R.id.baidu_request_image)).setChecked(settingResult.mRequestImage);
        ((CheckBox)findViewById(R.id.baidu_enable_extra_fun)).setChecked(settingResult.mEnableExtraFun);
        setFaceIdentifyWorkMode(settingResult.mFaceIdentifyMode);
        setUnitWorkMode(settingResult.mUnitType);
        setUnitRobotType(settingResult.mRobotType);
        setUnitSpeechType(settingResult.mSpeechType);
        setUnitBotTypeList(settingResult.mBotTypeList);
        setClassifyImageTypeList(settingResult.mClassifyImageTypeList);

        switch (settingResult.mUnitType) {
            case BaiduUnitAI.BAIDU_UNIT_TYPE_ASR_BOT:
            case BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT:
                enableRobotTypeSelect(false);
                enableBotTypeSelect(true);
                break;
            case BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT:
                enableRobotTypeSelect(true);
                enableBotTypeSelect(false);
                break;
        }
    }

    private SettingResult getUIValue() {
        SettingResult settingResult = new SettingResult();
        settingResult.mDebug = ((CheckBox)findViewById(R.id.baidu_debug)).isChecked();
        settingResult.mTTS = ((CheckBox)findViewById(R.id.baidu_tts)).isChecked();
        settingResult.mRequestImage = ((CheckBox)findViewById(R.id.baidu_request_image)).isChecked();
        settingResult.mEnableExtraFun = ((CheckBox)findViewById(R.id.baidu_enable_extra_fun)).isChecked();
        settingResult.mFaceIdentifyMode = getFaceIdentifyWorkMode();
        settingResult.mUnitType = getUnitWorkMode();
        settingResult.mRobotType = getUnitRobotType();
        settingResult.mSpeechType = getUnitSpeechType();
        settingResult.mBotTypeList = getUnitBotTypeList();
        settingResult.mClassifyImageTypeList = getClassifyImageTypeList();

        return settingResult;
    }

    private int getFaceIdentifyWorkMode() {
        RadioGroup radioGroup = findViewById(R.id.baidu_face_identify_work_mode);
        int selID = radioGroup.getCheckedRadioButtonId();
        int workMode = NONE;
        switch (selID) {
            case R.id.baidu_offline_face_identify:
                workMode = GlobalValue.FACE_IDENTIFY_OFFLINE;
                break;
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

    private int getUnitWorkMode() {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_work_mode);
        int selID = radioGroup.getCheckedRadioButtonId();
        int workMode = NONE;
        switch (selID) {
            case R.id.baidu_unit_asr_bot:
                workMode = BaiduUnitAI.BAIDU_UNIT_TYPE_ASR_BOT;
                break;
            case R.id.baidu_unit_keyboard_bot:
                workMode = BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT;
                break;
            case R.id.baidu_unit_keyboard_robot:
                workMode = BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT;
                break;
            default:
//                workMode = WORK_MODE_ASR_ROBOT;
                break;
        }
        return workMode;
    }

    private void setFaceIdentifyWorkMode(int workMode) {
        RadioGroup radioGroup = findViewById(R.id.baidu_face_identify_work_mode);
        if (GlobalValue.FACE_IDENTIFY_OFFLINE == workMode) {
            radioGroup.check(R.id.baidu_offline_face_identify);
        } else if (GlobalValue.FACE_IDENTIFY_ONLINE_1N == workMode) {
            radioGroup.check(R.id.baidu_online_1N_face_identify);
        } else if (GlobalValue.FACE_IDENTIFY_ONLINE_MN == workMode) {
            radioGroup.check(R.id.baidu_online_MN_face_identify);
        } else if (GlobalValue.FACE_IDENTIFY_CONTENT_APPROVE == workMode) {
            radioGroup.check(R.id.baidu_content_approve_face_identify);
        }
    }

    private void setUnitWorkMode(int workMode) {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_work_mode);
        if (BaiduUnitAI.BAIDU_UNIT_TYPE_ASR_BOT == workMode) {
            radioGroup.check(R.id.baidu_unit_asr_bot);
        } else if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT == workMode) {
            radioGroup.check(R.id.baidu_unit_keyboard_bot);
        } else if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT == workMode) {
            radioGroup.check(R.id.baidu_unit_keyboard_robot);
        }
    }

    private int getUnitRobotType() {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_robot_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int robotType = NONE;
        switch (selID) {
            case R.id.baidu_unit_robot_web:
                robotType = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_WEB;
                break;
            case R.id.baidu_unit_robot_local:
                robotType = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL;
                break;
            case R.id.baidu_unit_robot_all:
                robotType = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_ALL;
                break;
            default:
                break;
        }
        return robotType;
    }

    private void setUnitRobotType(int robotType) {
        RadioGroup radioGroup = findViewById(R.id.baidu_unit_robot_type);
        if (BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_WEB == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_web);
        } else if (BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_local);
        } else if (BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_ALL == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_all);
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

    private ArrayList<String> getUnitBotTypeList() {
        ArrayList<String> botTypeList = new ArrayList<>();
        //web bot
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            if (((CheckBox)findViewById(r_id)).isChecked()) {
                botTypeList.add(WEB_BOTID_RID_MAP.get(r_id));
            }
        }

        //local bot
/*
        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            if (((CheckBox)findViewById(r_id)).isChecked()) {
                botTypeList.add(LOCAL_BOTID_RID_MAP.get(r_id));
            }
        }
*/

        return botTypeList;
    }

    private void setUnitBotTypeList(ArrayList<String> botTypeList) {
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }

/*
        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }
*/

        if (null == botTypeList) return;
//        boolean bSetDone;
        for (String botType : botTypeList) {
//            bSetDone = false;
            //web bot
            for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
                if (botType.equals(WEB_BOTID_RID_MAP.get(r_id))) {
                    ((CheckBox)findViewById(r_id)).setChecked(true);
//                    bSetDone = true;
                    break;
                }
            }
//            if (bSetDone) continue;

            //local bot
/*
            for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
                if (botType.equals(LOCAL_BOTID_RID_MAP.get(r_id))) {
                    ((CheckBox)findViewById(r_id)).setChecked(true);
                    break;
                }
            }
*/
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