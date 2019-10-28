package com.fih.featurephone.voiceassistant.speechaction;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class VoiceVolumeAction extends BaseAction {
    private String[] KEYWORD_VOICE_TYPE, KEYWORD_VOICE_VOLUME;
    private String[] KEYWORD_VOLUME_DOWN;
    private String[] KEYWORD_VOLUME_UP;

    private Context mContext;

    public VoiceVolumeAction(Context context) {
        mContext = context;
        KEYWORD_VOICE_TYPE = mContext.getResources().getStringArray(R.array.voice_type_keyword);
        KEYWORD_VOICE_VOLUME = mContext.getResources().getStringArray(R.array.voice_volume_keyword);
        KEYWORD_VOLUME_DOWN = mContext.getResources().getStringArray(R.array.voice_volume_down_keyword);
        KEYWORD_VOLUME_UP = mContext.getResources().getStringArray(R.array.voice_volume_up_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        String volumeKeyword = CommonUtil.getContainKeyWord(query, KEYWORD_VOICE_VOLUME);
        if (TextUtils.isEmpty(volumeKeyword)) return false;

        String voiceKeyword = CommonUtil.getContainKeyWord(query, KEYWORD_VOICE_TYPE);
        int voiceType = getVoiceType(voiceKeyword);

        if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_DOWN)) {
            adjustVolume(voiceType, false);
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_voice_down);
            return true;
        } else if (CommonUtil.isContainKeyWord(query, KEYWORD_VOLUME_UP)) {
            adjustVolume(voiceType, true);
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

    private void adjustVolume(int VolumeType, boolean up) {
        AudioManager audioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        if (null != audioManager) {
            audioManager.adjustStreamVolume(VolumeType, up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
            audioManager.adjustStreamVolume(VolumeType, up ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }
}
