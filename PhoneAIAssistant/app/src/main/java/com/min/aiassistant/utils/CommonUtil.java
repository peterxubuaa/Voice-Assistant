package com.min.aiassistant.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    private CommonUtil() {
        throw new AssertionError();
    }

    public static Point getDisplaySize(Context context) {
        final Point point = new Point();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (null != wm) {
            wm.getDefaultDisplay().getSize(point);
        }
        return point;
    }

    public static Point getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        if (null != wm) {
            wm.getDefaultDisplay().getRealMetrics(dm);
        }
        return new Point(dm.widthPixels, dm.heightPixels);
    }

    private static boolean isEnglishByREG(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
//        return str.matches("[a-zA-Z]+");
    }

    private static boolean isChineseByREG(String str) {
        if(TextUtils.isEmpty(str)) return false;
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
        return pattern.matcher(str.trim()).find();
    }

    public static String filterChineseCharacter(String str) {
        char[] chs = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char ch : chs) {
            if (isChineseByREG("" + ch)) continue;
            sb.append(ch);
        }

        return sb.toString();
    }

    public static String filterEnglishCharacter(String str) {
        char[] chs = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char ch : chs) {
            if (isEnglishByREG(ch)) continue;
            sb.append(ch);
        }

        return sb.toString();
    }

    public static String filterPunctuation(String result) {
//        return result.replaceAll("[\\p{Punct}\\s]+", "");
        return result.replaceAll("[\\p{P}&&[^-]]", "");
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * 获取导航栏高度
     */
/*    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId=resources.getIdentifier("navigation_bar_height","dimen","android");
        return resources.getDimensionPixelSize(resourceId);
    }*/

    public static boolean isContainKeyWord(String query, String[] keyWordList) {
        if (null == keyWordList || TextUtils.isEmpty(query)) return false;

        for (String keyword : keyWordList) {
            if (query.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEqualsKeyWord(String query, String[] keyWordList) {
        if (null == keyWordList || TextUtils.isEmpty(query)) return false;

        for (String keyword : keyWordList) {
            if (query.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startWithKeyWord(String query, String[] keyWordList) {
        if (null == keyWordList || TextUtils.isEmpty(query)) return false;

        for (String keyword : keyWordList) {
            if (query.startsWith(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String getContainKeyWord(String query, String[] keyWordList) {
        if (null == keyWordList || TextUtils.isEmpty(query)) return null;

        for (String keyword : keyWordList) {
            if (query.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    public static ArrayList<String> getListFromString(String str) {
        if (TextUtils.isEmpty(str)) return null;

        String[] strArray = str.split(",");
        return new ArrayList<>(Arrays.asList(strArray));
    }

    public static String getStringFromList(ArrayList<String> strList) {
        StringBuilder sbValue = new StringBuilder();

        for (String value : strList) {
            if (sbValue.length() > 0) sbValue.append(",");
            sbValue.append(value);
        }

        return sbValue.toString();
    }

    public static ArrayList<Integer> getNumListFromString(String str) {
        if (TextUtils.isEmpty(str)) return null;

        String[] strArray = str.split(",");
        ArrayList<Integer> integerList = new ArrayList<>();
        for (String num : strArray) {
            integerList.add(Integer.valueOf(num));
        }

        return integerList;
    }

    public static String getStringFromNumList(ArrayList<Integer> strList) {
        StringBuilder sbValue = new StringBuilder();

        for (Integer value : strList) {
            if (sbValue.length() > 0) sbValue.append(",");
            sbValue.append(value);
        }

        return sbValue.toString();
    }

    public static boolean checkRegexMatch(String query, String[] regex) {
        for (String re : regex) {
            Pattern pattern = Pattern.compile(re);
            Matcher matcher = pattern.matcher(query);
            if (matcher.matches()) {//matcher.find()
                return true;
            }
        }
        return false;
    }

    public static String getRegexMatch(String query, String[] regex, int valueIndex) {
        for (String re : regex) {
            Pattern pattern = Pattern.compile(re);
            Matcher matcher = pattern.matcher(query);
            if (matcher.matches() && matcher.groupCount() >= valueIndex) { //matcher.matches()完全匹配，matcher.find()局部匹配
                return matcher.group(valueIndex);//匹配正则，但""表示找到的为空
            }
        }
        return null;//表示正在都不匹配
    }

/*    public static boolean isSupportMultiTouch(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
    }*/

    public static void hideSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm && imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static String toLowerCase(String origin) {
        byte[] data = origin.getBytes();
        final int DIST = 'a' - 'A';
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= 'A' && data[i] <= 'Z') {
                data[i] += DIST;
            }
        }

        return new String(data);
    }

    /*
     *  获取版本号
     */
    public static String getVersionName(Context context) {
        if (context != null) {
            try {
                return context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), 0)
                        .versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "0.0.0";
    }

    private static Handler sMainUIHandler = new Handler(Looper.getMainLooper());
    public static void toast(final Context context, final String text) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        } else {
            sMainUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static boolean isSystemApp(Context context, String pkgName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            if (null != pi) {
                return (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0
                        || (pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
