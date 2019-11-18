package com.fih.featurephone.voiceassistant.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    /*
    // 获取屏幕宽度
    public static int getScreenWidth(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    // 获取屏幕高度
    public static int getScreenHeight(Context ctx) {
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static synchronized void playVoice(Context context, String assertName) {
        try {
            MediaPlayer player = new MediaPlayer();
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd(assertName);
            player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getStartOffset());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void playRingtone(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }
*/

    private static boolean isEnglishByREG(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
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

/*    public static String leftChineseCharacter(String str) {
        StringBuilder chineseCharacter = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            String bb = str.substring(i, i + 1);
            // 生成一个Pattern,同时编译一个正则表达式,其中的u4E00("一"的unicode编码)-\u9FA5("龥"的unicode编码)
            if (java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb)) {
                chineseCharacter.append(bb);
            }
        }
        return chineseCharacter.toString();
    }*/

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

    public static String createDirInAppFileDir(Context context, String dir) {
        String tmpDir = context.getFilesDir().toString() + "/" + dir;
        if (makeDirs(tmpDir)) return tmpDir;

        tmpDir = context.getFilesDir().getAbsolutePath() + "/" + dir;
        if (makeDirs(dir)) {
            return tmpDir;
        } else {
            throw new RuntimeException("create model resources dir failed :" + tmpDir);
        }
    }

    public static void copyFromAssets(AssetManager assets, String source, String dest)
            throws IOException {
        File file = new File(dest);
        if (!file.exists()) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = assets.open(source);
                fos = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int size;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } finally {
                        is.close();
                    }
                }
            }
        }
    }

    private static boolean makeDirs(String dirPath) {
        File file = new File(dirPath);
        if (file.exists()) {
            return true;
        }
        return file.mkdirs();
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
        return new ArrayList<String>(Arrays.asList(strArray));
    }

    public static String getStringFromList(ArrayList<String> strList) {
        StringBuilder sbValue = new StringBuilder();

        for (String value : strList) {
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

    public static boolean isSupportMultiTouch(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
    }

    public static void showSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm && !imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void hideSoftKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != imm && imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

/*    public static String toUpperCase(String origin) {
        byte[] data = origin.getBytes();
        final int DIST = 'A' - 'a';
        for (int i = 0; i < data.length; i++) {
            if (data[i] >= 'a' && data[i] <= 'z') {
                data[i] += DIST;
            }
        }

        return new String(data);
    }*/

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
}
