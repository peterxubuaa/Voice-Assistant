package com.min.aiassistant.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.CommonUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BaiduBaseModel {
    //https://console.bce.baidu.com/ai/?locale=zh-cn#/ai/face/app/detail~appId=1371781
    public static final String ALL_APP_ID = "17937628";
    public static final String ALL_API_KEY = "leiywSo72wPerpsc8DejCsfR";
    public static final String ALL_SECRET_KEY = "sg318tLG9uebAI7FnM9PgIH7a4sSfR9F";

    static private String sAccessToken; //只获取一次
    protected Context mContext;
    protected BaiduBaseAI.IBaiduBaseListener mBaiduBaseListener;

    protected BaiduBaseModel(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        mContext = context;
        mBaiduBaseListener = listener;
    }

    protected String getAuthToken() {
        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
        if (!TextUtils.isEmpty(sAccessToken)) return sAccessToken;

        // 获取token地址
        final String AUTH_HOST = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = AUTH_HOST
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ALL_API_KEY
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + ALL_SECRET_KEY;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sbResult = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sbResult.append(line);
            }
            /*
             * 返回结果示例
             */
            JSONObject jsonObject = new JSONObject(sbResult.toString());
            sAccessToken = jsonObject.getString("access_token");
            return sAccessToken;
        } catch (Exception e) {
            e.printStackTrace(System.err);
            CommonUtil.toast(mContext, mContext.getString(R.string.baidu_token_fail));
        }
        return null;
    }
}
