package com.min.ai.voiceassistant.tts;

import android.content.Context;
import android.content.res.AssetManager;

import com.min.ai.voiceassistant.utils.CommonTools;

import java.io.IOException;

public class OfflineResource {

    public static final String VOICE_FEMALE = "F";
    public static final String VOICE_MALE = "M";
    public static final String VOICE_DUYY = "Y";
    public static final String VOICE_DUXY = "X";

    private static final String SAMPLE_DIR = "baiduTTS";

    private AssetManager mAssets;
    private String mDestPath;

    private String mTextFilename;
    private String mModelFilename;

    public OfflineResource(Context context, String voiceType) throws IOException {
        mAssets = context.getAssets();
        mDestPath = CommonTools.createDirInAppFileDir(context, SAMPLE_DIR);
        setOfflineVoiceType(voiceType);
    }

    public String getModelFilename() {
        return mModelFilename;
    }

    public String getTextFilename() {
        return mTextFilename;
    }

    private void setOfflineVoiceType(String voiceType) throws IOException {
        String text = "baiduTTS/bd_etts_text.dat";
        String model;
        if (VOICE_MALE.equals(voiceType)) {
            model = "baiduTTS/bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";
        } else if (VOICE_FEMALE.equals(voiceType)) {
            model = "baiduTTS/bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUXY.equals(voiceType)) {
            model = "baiduTTS/bd_etts_common_speech_yyjw_mand_eng_high_am-mix_v3.0.0_20170512.dat";
        } else if (VOICE_DUYY.equals(voiceType)) {
            model = "baiduTTS/bd_etts_common_speech_as_mand_eng_high_am_v3.0.0_20170516.dat";
        } else {
            throw new RuntimeException("voice type is not in list");
        }

        mTextFilename = copyAssetsFile(text);
        mModelFilename = copyAssetsFile(model);
    }

    private String copyAssetsFile(String sourceFilename) throws IOException {
        String destFilename = mDestPath + "/" + sourceFilename;
        int pos = sourceFilename.lastIndexOf("/");
        if (pos >= 0) {
            destFilename = mDestPath + "/" + sourceFilename.substring(pos + 1);
        }

        CommonTools.copyFromAssets(mAssets, sourceFilename, destFilename);
        return destFilename;
    }
}
