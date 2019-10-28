package com.fih.featurephone.voiceassistant.unit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingActivity extends Activity {
    static final String PREF_SETTINGS = "pref_settings";
    static final String PREF_DEBUG = "pref_debug";
    static final String PREF_WORK_MODE = "pref_work_mode";
    static final String PREF_ROBOT_TYPE = "pref_robot_type";
    static final String PREF_BOT_TYPE = "pref_bot_type";

    static final int NONE = 0;
    static final int ROBOT_TYPE_WEB = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_WEB;
    static final int ROBOT_TYPE_LOCAL = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL;
    static final int ROBOT_TYPE_ALL = BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_ALL;

    private SettingResult mOldSettingResult;

    public static class SettingResult implements Cloneable {
        public boolean mDebug;
        public int mUnitType;
        public int mRobotType;
        public ArrayList<String> mBotTypeList;

        SettingResult() {
            mDebug = false;
            mUnitType = NONE;
            mRobotType = NONE;
            mBotTypeList = new ArrayList<String>();
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
                    mUnitType == that.mUnitType &&
                    mRobotType == that.mRobotType &&
                    mBotTypeList.equals(that.mBotTypeList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mUnitType, mRobotType, mBotTypeList);
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
        }
    };

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
        settingResult.mUnitType = settingPrefs.getInt(PREF_WORK_MODE, BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT);
        settingResult.mRobotType = settingPrefs.getInt(PREF_ROBOT_TYPE, BaiduUnitAI.BAIDU_UNIT_ROBOT_TYPE_LOCAL);
        settingResult.mBotTypeList = CommonUtil.getListFromString(settingPrefs.getString(PREF_BOT_TYPE,
                "81459,81485,81469,81467,81465,81461,84536"));

        return settingResult;
    }

    private void enableRobotTypeSelect(boolean enable) {
        findViewById(R.id.baidu_unit_robot_web).setEnabled(enable);
        findViewById(R.id.baidu_unit_robot_local).setEnabled(enable);
        findViewById(R.id.baidu_unit_robot_all).setEnabled(enable);
    }

    private void enableBotTypeSelect(boolean enable) {
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            findViewById(r_id).setEnabled(enable);
        }

        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            findViewById(r_id).setEnabled(enable);
        }
    }

    private void setSettingResults(SettingResult settingResult) {
        SharedPreferences settingPrefs = getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor settingEditor = settingPrefs.edit();

        settingEditor.putBoolean(PREF_DEBUG, settingResult.mDebug);
        settingEditor.putInt(PREF_WORK_MODE, settingResult.mUnitType);
        settingEditor.putInt(PREF_ROBOT_TYPE, settingResult.mRobotType);
        settingEditor.putString(PREF_BOT_TYPE, CommonUtil.getStringFromList(settingResult.mBotTypeList));

        settingEditor.apply();
    }

    private void setUIValue(SettingResult settingResult) {
        ((CheckBox)findViewById(R.id.baidu_unit_debug)).setChecked(settingResult.mDebug);
        setUnitWorkMode(settingResult.mUnitType);
        setUnitRobotType(settingResult.mRobotType);
        setUnitBotTypeList(settingResult.mBotTypeList);

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
        settingResult.mDebug = ((CheckBox)findViewById(R.id.baidu_unit_debug)).isChecked();
        settingResult.mUnitType = getUnitWorkMode();
        settingResult.mRobotType = getUnitRobotType();
        settingResult.mBotTypeList = getUnitBotTypeList();
        return settingResult;
    }

    private int getUnitWorkMode() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.baidu_unit_work_mode);
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

    private void setUnitWorkMode(int workMode) {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.baidu_unit_work_mode);
        if (BaiduUnitAI.BAIDU_UNIT_TYPE_ASR_BOT == workMode) {
            radioGroup.check(R.id.baidu_unit_asr_bot);
        } else if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_BOT == workMode) {
            radioGroup.check(R.id.baidu_unit_keyboard_bot);
        } else if (BaiduUnitAI.BAIDU_UNIT_TYPE_KEYBOARD_ROBOT == workMode) {
            radioGroup.check(R.id.baidu_unit_keyboard_robot);
        }
    }

    private int getUnitRobotType() {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.baidu_unit_robot_type);
        int selID = radioGroup.getCheckedRadioButtonId();
        int robotType = NONE;
        switch (selID) {
            case R.id.baidu_unit_robot_web:
                robotType = ROBOT_TYPE_WEB;
                break;
            case R.id.baidu_unit_robot_local:
                robotType = ROBOT_TYPE_LOCAL;
                break;
            case R.id.baidu_unit_robot_all:
                robotType = ROBOT_TYPE_ALL;
                break;
            default:
                break;
        }
        return robotType;
    }

    private void setUnitRobotType(int robotType) {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.baidu_unit_robot_type);
        if (ROBOT_TYPE_WEB == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_web);
        } else if (ROBOT_TYPE_LOCAL == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_local);
        } else if (ROBOT_TYPE_ALL == robotType) {
            radioGroup.check(R.id.baidu_unit_robot_all);
        }
    }

    private ArrayList<String> getUnitBotTypeList() {
        ArrayList<String> botTypeList = new ArrayList<String>();
        //web bot
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            if (((CheckBox)findViewById(r_id)).isChecked()) {
                botTypeList.add(WEB_BOTID_RID_MAP.get(r_id));
            }
        }

        //local bot
        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            if (((CheckBox)findViewById(r_id)).isChecked()) {
                botTypeList.add(LOCAL_BOTID_RID_MAP.get(r_id));
            }
        }

        return botTypeList;
    }

    private void setUnitBotTypeList(ArrayList<String> botTypeList) {
        for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }

        for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
            ((CheckBox)findViewById(r_id)).setChecked(false);
        }

        if (null == botTypeList) return;

        boolean bSetDone;
        for (String botType : botTypeList) {
            bSetDone = false;
            //web bot
            for (int r_id : WEB_BOTID_RID_MAP.keySet()) {
                if (botType.equals(WEB_BOTID_RID_MAP.get(r_id))) {
                    ((CheckBox)findViewById(r_id)).setChecked(true);
                    bSetDone = true;
                    break;
                }
            }
            if (bSetDone) continue;

            //local bot
            for (int r_id : LOCAL_BOTID_RID_MAP.keySet()) {
                if (botType.equals(LOCAL_BOTID_RID_MAP.get(r_id))) {
                    ((CheckBox)findViewById(r_id)).setChecked(true);
                    break;
                }
            }
        }
    }
}