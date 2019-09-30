package com.min.ai.voiceassistant;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

class SpeechAction {
    private final String TAG = SpeechAction.class.getSimpleName();
    private final boolean DEBUG = false;

    private String[] KEYWORD_LAUNCH_APP = new String[]{"打开", "运行"};
    private String[] KEYWORD_EXIT_APP = new String[]{"退出", "关闭", "主界面"};
    private String[] KEYWORD_CALL = new String[]{"打电话"};
    private String[] KEYWORD_MESSAGE = new String[]{"发短信", "发消息"};
    private String[] KEYWORD_SEARCH = new String[]{"查找", "搜索"};
    private String[] KEYWORD_MUSIC = new String[]{"音乐", "唱歌"};
    private String[] KEYWORD_CAMERA = new String[]{"拍照", "照相", "录像", "摄像"};
    private String[] KEYWORD_CAMERA_IMAGE = new String[]{"拍照", "照相"};
    private String[] KEYWORD_CAMERA_VIDEO = new String[]{"录像", "摄像"};
    private String[] KEYWORD_VOLUME = new String[]{"音量", "声音"};
    private String[] KEYWORD_VOLUME_DOWN = new String[]{"低", "小"};
    private String[] KEYWORD_VOLUME_UP = new String[]{"高", "大"};

    private Context mContext;
    private Map<String, String> mInstalledAppMap;
    private Map<String, ArrayList<String>> mContactInfoMap;

    SpeechAction(Context context) {
        mContext = context;
        getAllInstalledAppList(context);
        getAllContactInfo(context);
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
//            //获取intent
//            Intent intent = packageManager.getLaunchIntentForPackage(p.packageName);
            if (!TextUtils.isEmpty(label) && !TextUtils.isEmpty(pkgName)) {
                mInstalledAppMap.put(label, pkgName);
            }
        }
    }

    private void getAllContactInfo(Context context) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        if (null == cursor) return;

        mContactInfoMap = new HashMap<>();
        //moveToNext方法返回的是一个boolean类型的数据
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) continue;

            ArrayList<String> phoneNumList = mContactInfoMap.get(name);
            if (null == phoneNumList) {
                phoneNumList = new ArrayList<>();
            }
            phoneNumList.add(number);
            mContactInfoMap.put(name, phoneNumList);
        }
        cursor.close();
    }

    boolean parseAction(String command) {
        String keyword = isContainKeyWord(command, KEYWORD_EXIT_APP);
        if (!TextUtils.isEmpty(keyword)) {
            launchHomeUI();
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_CAMERA);
        if (!TextUtils.isEmpty(keyword)) {
            cameraAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_MUSIC);
        if (!TextUtils.isEmpty(keyword)) {
            musicAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_LAUNCH_APP);
        if (!TextUtils.isEmpty(keyword)) {
            launchAppAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_CALL);
        if (!TextUtils.isEmpty(keyword)) {
            callAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_MESSAGE);
        if (!TextUtils.isEmpty(keyword)) {
            messageAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_VOLUME);
        if (!TextUtils.isEmpty(keyword)) {
            volumeAction(command, keyword);
            return true;
        }

        keyword = isContainKeyWord(command, KEYWORD_SEARCH);
        if (!TextUtils.isEmpty(keyword)) {
            searchAction(command, keyword);
            return true;
        }

        return false;
    }

    private void launchAppAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        for (String key : mInstalledAppMap.keySet()) {
            if (filterCmd.contains(key)) {
                String pkgName = mInstalledAppMap.get(key);
                launchForeground(pkgName);
                return;
            }
        }
    }

    private void callAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        for (String key : mContactInfoMap.keySet()) {
            if (filterCmd.contains(key)) {
                ArrayList<String> phoneNumList = mContactInfoMap.get(key);
                if (null != phoneNumList && phoneNumList.size() > 0) {
                    String number = phoneNumList.get(0);
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.CALL");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("tel:"+number));
                    mContext.startActivity(intent);
                    return;
                }
            }
        }
    }

    private void messageAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        for (String key : mContactInfoMap.keySet()) {
            if (filterCmd.contains(key)) {
                ArrayList<String> phoneNumList = mContactInfoMap.get(key);
                if (null != phoneNumList && phoneNumList.size() > 0) {
//                    String number = phoneNumList.get(0);
                    return;
                }
            }
        }
    }

    private void searchAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        // 指定intent的action是ACTION_WEB_SEARCH就能调用浏览器
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);//ACTION_SEARCH
        // 指定搜索关键字是选中的文本
        intent.putExtra(SearchManager.QUERY, filterCmd);
        mContext.startActivity(intent);
    }

    private void cameraAction(String command, String keyword) {
        Intent cameraIntent = null;
        if (isContainKeyWord(keyword, KEYWORD_CAMERA_IMAGE) != null) {
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        } else if (isContainKeyWord(keyword, KEYWORD_CAMERA_VIDEO) != null) {
            cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            cameraIntent.addCategory("android.intent.category.DEFAULT");
        }
        //这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
        if(null != cameraIntent) {
            ComponentName componentName = cameraIntent.resolveActivity(mContext.getPackageManager());
            if (null != componentName) {
                PackageManager packageManager = mContext.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(componentName.getPackageName());
                mContext.startActivity(intent);//启动相机
            }
        }
    }

    private void musicAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        Intent musicIntent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
        if(musicIntent.resolveActivity(mContext.getPackageManager()) != null){
            mContext.startActivity(musicIntent);
        }

        /*
        Intent musicIntent = new Intent(Intent.ACTION_VIEW);
        musicIntent.addCategory(Intent.CATEGORY_DEFAULT);
        musicIntent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        musicIntent.setDataAndType(Uri.fromFile(new File("")), "audio/*");// type:改成"video/*"表示获取视频的

        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(musicIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfoList.size() > 0) {
            Intent intent = packageManager.getLaunchIntentForPackage(resolveInfoList.get(0).activityInfo.packageName);
            mContext.startActivity(intent);
        }*/

//        Intent intent = new Intent("android.intent.action.MUSIC_PLAYER");//Intent.CATEGORY_APP_MUSIC);
//        mContext.startActivity(intent);
    }

    private void volumeAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        if (DEBUG) Log.i(TAG, keyword + " -> " + filterCmd);

        if (isContainKeyWord(filterCmd, KEYWORD_VOLUME_DOWN) != null) {
            adjustVolume(AudioManager.STREAM_MUSIC, false);
        } else if (isContainKeyWord(filterCmd, KEYWORD_VOLUME_UP) != null) {
            adjustVolume(AudioManager.STREAM_MUSIC, true);
        }
    }

    private String isContainKeyWord(String command, String[] strList) {
        for (String keyword : strList) {
            if (command.startsWith(keyword) || command.endsWith(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    private void launchHomeUI() {
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }

    private void adjustVolume(int VolumeType, boolean up) {
//        AudioSystem.STREAM_VOICE_CALL;
//        AudioSystem.STREAM_SYSTEM;
//        AudioSystem.STREAM_RING;
//        AudioSystem.STREAM_MUSIC;
//        AudioSystem.STREAM_ALARM;
//        AudioSystem.STREAM_NOTIFICATION;

        AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, up? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                AudioManager.FX_FOCUS_NAVIGATION_UP);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, up? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                AudioManager.FX_FOCUS_NAVIGATION_UP);
    }


    private void launchForeground(String pkgName) {
        ActivityManager activityManager = (ActivityManager)mContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();

        getRunningTaskList();
        //must be system app
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
        //

        //relaunch
        PackageManager packageManager = mContext.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkgName);
        mContext.startActivity(intent);
    }

    private void getRunningTaskList() {
        Calendar calendar=Calendar.getInstance();
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
    }
}
