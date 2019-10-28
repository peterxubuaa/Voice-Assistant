package com.fih.featurephone.voiceassistant.speechaction;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

public class WebSearchAction extends BaseAction {
    private String[] KEYWORD_WEB_SEARCH;

    private Context mContext;

    public WebSearchAction(Context context) {
        mContext = context;
        KEYWORD_WEB_SEARCH = mContext.getResources().getStringArray(R.array.web_search_keyword);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        String keyword = CommonUtil.getContainKeyWord(query, KEYWORD_WEB_SEARCH);
        if (!TextUtils.isEmpty(keyword)) {
            String content = searchAction(query, keyword);
            if (!TextUtils.isEmpty(content)) {
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_web_search) + content;
            }
            return true;
        }
        return false;
    }

    private String searchAction(String command, String keyword) {
        String filterCmd = command.replaceAll(keyword, "");
        filterCmd = CommonUtil.filterPunctuation(filterCmd);

//        // 指定intent的action是ACTION_WEB_SEARCH就能调用浏览器
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_WEB_SEARCH);//ACTION_WEB_SEARCH, ACTION_SEARCH
//        // 指定搜索关键字是选中的文本
//        intent.putExtra(SearchManager.QUERY, filterCmd);
//        //这句作用是如果没有相机则该应用不会闪退，要是不加这句则当系统没有相机应用的时候该应用会闪退
//        PackageManager packageManager = mContext.getPackageManager();
//        ComponentName componentName = intent.resolveActivity(packageManager);
//        if (null != componentName && packageManager.getLaunchIntentForPackage(componentName.getPackageName()) != null) {
//            mContext.startActivity(intent);
//        }

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("http://www.baidu.com/#wd=" + filterCmd);
        intent.setData(uri);
        mContext.startActivity(intent);
        return filterCmd;
    }
}
