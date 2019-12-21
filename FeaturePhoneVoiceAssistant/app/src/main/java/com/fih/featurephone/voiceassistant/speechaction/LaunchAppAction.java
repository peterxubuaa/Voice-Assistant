package com.fih.featurephone.voiceassistant.speechaction;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

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

        if (null == mInstalledAppMap) {
            getAllInstalledAppList(mContext);
        }
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        String launchApp = CommonUtil.getRegexMatch(query, REGEX_LAUNCH_APP, 1);
        if (!TextUtils.isEmpty(launchApp)) {
            String appName = launchAppAction(launchApp);
            if (!TextUtils.isEmpty(appName)) {
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_launch_app_high) + appName;
                return true;
            } else {
                return false;
            }
        }

        if (CommonUtil.isEqualsKeyWord(query, KEYWORD_EXIT_APP)) {
            launchHomeUI();
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_exit_app_high);
            return true;
        }

        return false;
    }

    private void getAllInstalledAppList(Context context) {
        mInstalledAppMap = new HashMap<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);

        for (int i = 0; i < pInfo.size(); i++) {
            PackageInfo p = pInfo.get(i);
            // 获取相关包的<application>中的label信息，也就是-->应用程序的名字
            String label = packageManager.getApplicationLabel(p.applicationInfo).toString();
            String pkgName = p.packageName;
            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(pkgName)) {
                mInstalledAppMap.put(label, pkgName);
                //patch
                if (label.contains("相机")) {
                    mInstalledAppMap.put("相机", pkgName);
                } else if (label.contains("拨号")) {
                    mInstalledAppMap.put("电话", pkgName);
                }
            }
        }
    }

    private String launchAppAction(String filterCmd) {
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

    /*private void getRunningTaskList() {
        Calendar calendar= Calendar.getInstance();
        calendar.setTime(new Date());
        long endTime = calendar.getTimeInMillis();//结束时间
        calendar.add(Calendar.HOUR_OF_DAY, -1);//时间间隔为一小时
        long startTime = calendar.getTimeInMillis();//开始时间

        UsageStatsManager manager = (UsageStatsManager) mContext.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStatsList = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime);
        if (usageStatsList == null || usageStatsList.size() == 0) {// 没有权限，获取不到数据
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.getApplicationContext().startActivity(intent);
            return;
        }

        List<UsageStats> livingUsageStatesList = new ArrayList<>();
        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getLastTimeUsed() > 0) {
                livingUsageStatesList.add(usageStats);
            }
        }

        // 按照使用时间对应用进行排序
        Collections.sort(livingUsageStatesList, new Comparator<UsageStats>() {
            @Override
            public int compare(UsageStats o1, UsageStats o2) {
                if (o1.getLastTimeUsed() > o2.getLastTimeUsed()) {
                    return -1;
                } else if (o1.getLastTimeUsed() < o2.getLastTimeUsed()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        UsageStats usageStatsResult = livingUsageStatesList.get(1);
    }*/

    private void launchHomeUI() {
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }
}
