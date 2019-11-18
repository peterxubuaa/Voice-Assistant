package com.fih.featurephone.voiceassistant.speechaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class LaunchCameraAppAction implements BaseAction {
    private final String[] REGEX_CAMERA_IMAGE;
    private final String[] REGEX_CAMERA_VIDEO;

    private Context mContext;

    public LaunchCameraAppAction(Context context) {
        mContext = context;
        REGEX_CAMERA_IMAGE = mContext.getResources().getStringArray(R.array.launch_camera_image_regex);
        REGEX_CAMERA_VIDEO = mContext.getResources().getStringArray(R.array.launch_camera_video_regex);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        String resultHint = mContext.getString(R.string.baidu_unit_hint_launch_camera_fail);
        if (CommonUtil.checkRegexMatch(query, REGEX_CAMERA_IMAGE)) {
            if (cameraAction(true)) {
                resultHint = mContext.getString(R.string.baidu_unit_hint_launch_image);
            }
        } else if (CommonUtil.checkRegexMatch(query, REGEX_CAMERA_VIDEO)) {
            if (cameraAction(false)) {
                resultHint = mContext.getString(R.string.baidu_unit_hint_launch_video);
            }
        } else {
            return false;
        }

        bestResponse.reset();
        bestResponse.mAnswer = resultHint;
        return true;
    }

    private boolean cameraAction(boolean image) {
        Intent cameraIntent;
        if (image) {
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        } else {
            cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        }

        //这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
        PackageManager packageManager = mContext.getPackageManager();
        ComponentName componentName = cameraIntent.resolveActivity(packageManager);
        if (null != componentName) {
            Intent intent = packageManager.getLaunchIntentForPackage(componentName.getPackageName());
            if (null != intent) {
                mContext.startActivity(intent);//启动相机
                return true;
            }
        }

        return false;
    }
}
