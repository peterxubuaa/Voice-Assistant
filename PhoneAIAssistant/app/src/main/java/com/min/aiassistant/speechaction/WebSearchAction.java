package com.min.aiassistant.speechaction;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;

public class WebSearchAction implements BaseAction {
    private String[] KEYWORD_WEB_SEARCH;

    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static WebSearchAction sInstance;

    public static WebSearchAction getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new WebSearchAction(context);
        }
        return sInstance;
    }

    public WebSearchAction(Context context) {
        mContext = context;
        KEYWORD_WEB_SEARCH = mContext.getResources().getStringArray(R.array.web_search_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!CommonUtil.startWithKeyWord(query, KEYWORD_WEB_SEARCH)) return false;

        String keyword = CommonUtil.getContainKeyWord(query, KEYWORD_WEB_SEARCH);
        String searchContent = query.replaceAll(keyword, "");
        searchAction(searchContent);
        bestResponse.reset();
        bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_web_search) + searchContent;

        return true;
    }

    public void searchAction(String searchContent) {
        // 指定intent的action是ACTION_WEB_SEARCH就能调用浏览器
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_WEB_SEARCH);//ACTION_WEB_SEARCH, ACTION_SEARCH
        // 指定搜索关键字是选中的文本
        intent.putExtra(SearchManager.QUERY, searchContent);
        //这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
        PackageManager packageManager = mContext.getPackageManager();
        ComponentName componentName = intent.resolveActivity(packageManager);
        if (null != componentName && packageManager.getLaunchIntentForPackage(componentName.getPackageName()) != null) {
            mContext.startActivity(intent);
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("http://www.baidu.com/#wd=" + searchContent);
            intent.setData(uri);
            mContext.startActivity(intent);
        }
    }
}
