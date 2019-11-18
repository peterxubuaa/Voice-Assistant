package com.fih.featurephone.voiceassistant.speechaction;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class VoiceVolumeAction implements BaseAction {
    private String[] KEYWORD_VOICE_VOLUME;;
    private String[] KEYWORD_VOICE_TYPE;
    private String[] REGEX_VOLUME_DOWN;
    private String[] REGEX_VOLUME_UP;
    private String[] KEYWORD_VOLUME_LIMIT;
    private String[] KEYWORD_VOLUME_SWITCH_ON;
    private String[] KEYWORD_VOLUME_SWITCH_OFF;

    private Context mContext;

    public VoiceVolumeAction(Context context) {
        mContext = context;
        KEYWORD_VOICE_TYPE = mContext.getResources().getStringArray(R.array.voice_type_keyword);
        KEYWORD_VOICE_VOLUME = mContext.getResources().getStringArray(R.array.voice_volume_keyword);
        REGEX_VOLUME_DOWN = mContext.getResources().getStringArray(R.array.voice_volume_down_regex);
        REGEX_VOLUME_UP = mContext.getResources().getStringArray(R.array.voice_volume_up_regex);
        KEYWORD_VOLUME_LIMIT = mContext.getResources().getStringArray(R.array.voice_volume_limit_keyword);
        KEYWORD_VOLUME_SWITCH_ON = mContext.getResources().getStringArray(R.array.voice_volume_switch_on_keyword);
        KEYWORD_VOLUME_SWITCH_OFF = mContext.getResources().getStringArray(R.array.voice_volume_switch_off_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!CommonUtil.isContainKeyWord(query, KEYWORD_VOICE_VOLUME)) return false;

        String voiceKeyword = CommonUtil.getContainKeyWord(query, KEYWORD_VOICE_TYPE);
        int voiceType = getVoiceType(voiceKeyword);

        if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_SWITCH_ON)) {
            adjustSwitchVolume(voiceType, true);
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_voice_on);
            return true;
        } else if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_SWITCH_OFF)) {
            adjustSwitchVolume(voiceType, false);
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_voice_off);
            return true;
        }

        if (CommonUtil.checkRegexMatch(query, REGEX_VOLUME_DOWN)) {
            if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_LIMIT)) {
                adjustSwitchVolume(voiceType, false);
            } else {
                adjustVolume(voiceType, false);
            }
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_voice_down);
            return true;
        }

        if (CommonUtil.checkRegexMatch(query, REGEX_VOLUME_UP)) {
            if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_LIMIT)) {
                adjustMaxVolume(voiceType);
            } else {
                adjustVolume(voiceType, true);
            }
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_voice_up);
            return true;
        }

        return false;
    }

    private int getVoiceType(String voiceKeyword) {
        if (TextUtils.isEmpty(voiceKeyword)) return AudioManager.STREAM_MUSIC;
        int [] AudioManager_VoiceType = new int[]{
                AudioManager.STREAM_SYSTEM,
                AudioManager.STREAM_MUSIC,
                AudioManager.STREAM_RING,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.STREAM_ALARM,
                AudioManager.STREAM_NOTIFICATION
        };

        for (int i = 0; i < KEYWORD_VOICE_TYPE.length; i++) {
            if (KEYWORD_VOICE_TYPE[i].equals(voiceKeyword)) {
                return AudioManager_VoiceType[i];
            }
        }
        return AudioManager.STREAM_MUSIC;
    }

    private void adjustVolume(int volumeType, boolean up) {
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

        if (null == audioManager) return;

        audioManager.adjustStreamVolume(volumeType, up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                AudioManager.FX_FOCUS_NAVIGATION_UP);
        audioManager.adjustStreamVolume(volumeType, up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    private void adjustMaxVolume(int volumeType) {
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if (null == audioManager) return;

        int maxVolume = audioManager.getStreamMaxVolume(volumeType);
        audioManager.setStreamVolume(volumeType, maxVolume, 0);
    }

    private void adjustSwitchVolume(int volumeType, boolean on) {
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if (null == audioManager) return;

        if (on) {
            if (audioManager.getStreamVolume(volumeType) == 0) {
                audioManager.adjustStreamVolume(volumeType, AudioManager.ADJUST_RAISE,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        } else {
            audioManager.setStreamVolume(volumeType, 0, 0);
        }
    }
}
