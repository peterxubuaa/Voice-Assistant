package com.min.aiassistant.speechaction;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.GlobalValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

public class LaunchAppAction  implements BaseAction {
    private final String[] REGEX_LAUNCH_APP;
    private final String[] KEYWORD_EXIT_APP;

    static private Map<String, String> mInstalledAppMap;
    private Context mContext;

    public LaunchAppAction(Context context) {
        mContext = context;
        REGEX_LAUNCH_APP = mContext.getResources().getStringArray(R.array.launch_app_regex);
        KEYWORD_EXIT_APP = mContext.getResources().getStringArray(R.array.launch_exit_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        String launchApp = CommonUtil.getRegexMatch(query, REGEX_LAUNCH_APP, 1);

        if (!TextUtils.isEmpty(launchApp)) {
            String appName = launchAppAction(launchApp);
            if (!TextUtils.isEmpty(appName)) {
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_launch_app) + appName;
                return true;
            } else {
                return false;
            }
        }

        if (CommonUtil.isEqualsKeyWord(query, KEYWORD_EXIT_APP)) {
            launchHomeUI();
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_exit_app);
            return true;
        }

        return false;
    }

    private Map<String, String> getAllInstalledAppList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);

        Map<String, String> installedAppMap = new HashMap<>();
        for (int i = 0; i < pInfo.size(); i++) {
            PackageInfo p = pInfo.get(i);
            // 获取相关包的<application>中的label信息，也就是-->应用程序的名字
            String label = packageManager.getApplicationLabel(p.applicationInfo).toString();
            String pkgName = p.packageName;
            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(pkgName)) {
                installedAppMap.put(label, pkgName);
                //patch
                if (label.contains("相机")) {
                    installedAppMap.put("相机", pkgName);
                } else if (label.contains("拨号")) {
                    installedAppMap.put("电话", pkgName);
                }
            }
        }

        return installedAppMap;
    }

    private String launchAppAction(String filterCmd) {
        if (null == mInstalledAppMap) {
            if (havePermissions(mContext)) {
                mInstalledAppMap = getAllInstalledAppList(mContext);
            } else {
                Intent intent = new Intent(GlobalValue.LOCAL_BROADCAST_INSTALL_PACKAGES_PERMISSION);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                return null;
            }
        }

        for (String key : mInstalledAppMap.keySet()) {
            if (filterCmd.contains(key)) {
                String pkgName = mInstalledAppMap.get(key);
                launchForeground(pkgName);
                return key;
            }
        }
        return null;
    }

    private void launchForeground(String pkgName) {
        ActivityManager activityManager = (ActivityManager)mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = null;
        if (activityManager != null) {
            appProcessInfoList = activityManager.getRunningAppProcesses();
        }

//        getRunningTaskList();
        //must be system app
        if (null != appProcessInfoList) {
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
                if (appProcessInfo.processName.equals(pkgName) || Arrays.asList(appProcessInfo.pkgList).contains(pkgName)) {
                    if (appProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        // move to top
                        List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
                        for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                            if (taskInfo.topActivity.getPackageName().equals(pkgName)) {
                                activityManager.moveTaskToFront(taskInfo.id, 0);
                                return;
                            }
                        }
                    }
                }
            }
        }
        //

        //relaunch
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkgName);
        mContext.startActivity(intent);
    }

    private void launchHomeUI() {
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }

    // 请求权限
    private boolean havePermissions(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return context.checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
