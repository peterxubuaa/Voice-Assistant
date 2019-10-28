package com.fih.featurephone.voiceassistant.speechaction;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class BrightnessAction extends BaseAction {
    private final String[] KEYWORD_BRIGHTNESS;
    private final String[] KEYWORD_BRIGHTNESS_DOWN;
    private final String[] KEYWORD_BRIGHTNESS_UP;

    private Context mContext;

    public BrightnessAction(Context context) {
        mContext = context;
        KEYWORD_BRIGHTNESS = mContext.getResources().getStringArray(R.array.brightness_keyword);
        KEYWORD_BRIGHTNESS_DOWN = mContext.getResources().getStringArray(R.array.brightness_keyword_down);
        KEYWORD_BRIGHTNESS_UP = mContext.getResources().getStringArray(R.array.brightness_keyword_up);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        String keyword = CommonUtil.getContainKeyWord(query, KEYWORD_BRIGHTNESS);
        if (!TextUtils.isEmpty(keyword)) {
            String filterCmd = query.replaceAll(keyword, "");
            String hint;
            if (CommonUtil.isContainKeyWord(filterCmd, KEYWORD_BRIGHTNESS_DOWN)) {
                hint = adjustBrightness(false);
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_brightness_low) + hint;
                return true;
            } else if (CommonUtil.isContainKeyWord(filterCmd, KEYWORD_BRIGHTNESS_UP)) {
                hint = adjustBrightness(true);
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_brightness_high) + hint;
                return true;
            }
        }
        return false;
    }

    private String adjustBrightness(boolean up) {
        int curBrightness = getScreenBrightness(mContext);
        if (up) {
            setBrightness(mContext, curBrightness + 40);
        } else {
            setBrightness(mContext, curBrightness - 40);
        }
        curBrightness = getScreenBrightness(mContext);
        return String.valueOf(curBrightness);
    }

    private int getScreenBrightness(Context context) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    private void setBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }
}
