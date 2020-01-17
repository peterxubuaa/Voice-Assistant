package com.min.aiassistant.speechaction;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;

public class BrightnessAction implements BaseAction {
    private final int MIN_BRIGHTNESS = 0;
    private final int MAX_BRIGHTNESS = 255;
    private final String[] KEYWORD_BRIGHTNESS;
    private final String[] REGEX_BRIGHTNESS_DOWN;
    private final String[] REGEX_BRIGHTNESS_UP;
    private String[] KEYWORD_BRIGHTNESS_LIMIT;

    private Context mContext;

    public BrightnessAction(Context context) {
        mContext = context;
        KEYWORD_BRIGHTNESS = mContext.getResources().getStringArray(R.array.brightness_keyword);
        REGEX_BRIGHTNESS_DOWN = mContext.getResources().getStringArray(R.array.brightness_down_regex);
        REGEX_BRIGHTNESS_UP = mContext.getResources().getStringArray(R.array.brightness_up_regex);
        KEYWORD_BRIGHTNESS_LIMIT = mContext.getResources().getStringArray(R.array.brightness_limit_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        String keyword = CommonUtil.getContainKeyWord(query, KEYWORD_BRIGHTNESS);
        if (TextUtils.isEmpty(keyword)) return false;

        String filterCmd = query.replaceAll(keyword, "");
        if (CommonUtil.checkRegexMatch(filterCmd, REGEX_BRIGHTNESS_DOWN)){
            if (CommonUtil.isContainKeyWord(query, KEYWORD_BRIGHTNESS_LIMIT)) {
                setBrightness(MIN_BRIGHTNESS);
            } else {
                adjustBrightness(false);
            }
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_brightness_low) + getScreenBrightness();
            return true;
        }

        if (CommonUtil.checkRegexMatch(filterCmd, REGEX_BRIGHTNESS_UP)) {
            if (CommonUtil.isContainKeyWord(query, KEYWORD_BRIGHTNESS_LIMIT)) {
                setBrightness(MAX_BRIGHTNESS);
            } else {
                adjustBrightness(true);
            }
            adjustBrightness(true);
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_brightness_high) + getScreenBrightness();
            return true;
        }

        return false;
    }

    private void adjustBrightness(boolean up) {
        int curBrightness = getScreenBrightness();
        if (up) {
            setBrightness(curBrightness + 40);
        } else {
            setBrightness(curBrightness - 40);
        }
    }

    private int getScreenBrightness() {
        int nowBrightnessValue = 0;
        ContentResolver resolver = mContext.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    private void setBrightness(int brightness) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
            if (brightness < MIN_BRIGHTNESS) {
                brightness = MIN_BRIGHTNESS;
            } else if (brightness > MAX_BRIGHTNESS) {
                brightness = MAX_BRIGHTNESS;
            }
            android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            resolver.notifyChange(uri, null);
        } catch (Exception e) {
            Log.w("BrightnessAction", e.toString());
        }
    }
}
