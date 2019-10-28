package com.fih.featurephone.voiceassistant.speechaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class LaunchCameraAppAction extends BaseAction {
    private final String[] KEYWORD_CAMERA_IMAGE;
    private final String[] KEYWORD_CAMERA_VIDEO;

    private Context mContext;

    LaunchCameraAppAction(Context context) {
        mContext = context;
        KEYWORD_CAMERA_IMAGE = mContext.getResources().getStringArray(R.array.launch_camera_image_keyword);
        KEYWORD_CAMERA_VIDEO = mContext.getResources().getStringArray(R.array.launch_camera_video_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        String keyword = CommonUtil.getContainKeyWord(query, KEYWORD_CAMERA_IMAGE);
        if (TextUtils.isEmpty(keyword)) {
            keyword = CommonUtil.getContainKeyWord(query, KEYWORD_CAMERA_VIDEO);
        }
        if (TextUtils.isEmpty(keyword)) return false;

        String hint = cameraAction(keyword);

        bestResponse.reset();
        if (TextUtils.isEmpty(hint)) {
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_launch_camera_fail);
        } else {
            bestResponse.mAnswer = hint;
        }

        return true;
    }

    private String cameraAction(String keyword) {
        String resultHint = null;
        Intent cameraIntent = null;
        if (CommonUtil.isContainKeyWord(keyword, KEYWORD_CAMERA_IMAGE)) {
            resultHint = mContext.getString(R.string.baidu_unit_hint_launch_image);
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        } else if (CommonUtil.isContainKeyWord(keyword, KEYWORD_CAMERA_VIDEO)) {
            resultHint = mContext.getString(R.string.baidu_unit_hint_launch_video);
            cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        }

        if(null != cameraIntent) {
            //这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
            PackageManager packageManager = mContext.getPackageManager();
            ComponentName componentName = cameraIntent.resolveActivity(packageManager);
            if (null != componentName) {
                Intent intent = packageManager.getLaunchIntentForPackage(componentName.getPackageName());
                if (null != intent) {
                    mContext.startActivity(intent);//启动相机
                    return resultHint;
                }
            }
        }

        return null;
    }
}
