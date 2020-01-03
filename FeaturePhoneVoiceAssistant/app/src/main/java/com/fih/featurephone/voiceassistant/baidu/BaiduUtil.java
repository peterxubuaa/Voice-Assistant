package com.fih.featurephone.voiceassistant.baidu;

import android.content.Context;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BaiduUtil {
//    https://console.bce.baidu.com/ai/#/ai/ocr/app/detail~appId=1249908
//    private final String ALL_APP_ID = "17380810";
//    private final String ALL_API_KEY = "kA0qDiR7zLN5Pqf6zmt9uH7o";
//    private final String ALL_SECRET_KEY = "rwQ1TN64IPqCAGr5xoAqxPtRu009tWfZ";

    //    https://console.bce.baidu.com/ai/?locale=zh-cn#/ai/face/app/detail~appId=1371781
    private final String ALL_APP_ID = "17937628";
    private final String ALL_API_KEY = "leiywSo72wPerpsc8DejCsfR";
    private final String ALL_SECRET_KEY = "sg318tLG9uebAI7FnM9PgIH7a4sSfR9F";

//    private final String UNIT_API_KEY = "VoGOQkYvLcjWoYOfpmlh5Eps";
//    private final String UNIT_SECRET_KEY = "P1SMk24HIORpsxfcm2jNFXgaLvYV4inI";
//
//    private final String CLASSIFY_IMAGE_API_KEY = "YaB7LWAjPDHiRx2pFK0wwSR4";
//    private final String CLASSIFY_IMAGE_SECRET_KEY = "GyebraoclthiLcG9isGB2tvBkKxejGmY";
//
//    private final String FACE_API_KEY = "leiywSo72wPerpsc8DejCsfR";
//    private final String FACE_SECRET_KEY = "sg318tLG9uebAI7FnM9PgIH7a4sSfR9F";
//
//    //https://console.bce.baidu.com/ai/?_=1576910506103&fromai=1&locale=zh-cn#/ai/body/app/list
//    private final String HUMAN_BODY_API_KEY = "HRkbmXEY68VX7V32MTKW8tCy";
//    private final String HUMAN_BODY_SECRET_KEY = "0QGKM3fcLo6qzGOMkDlG4381mmmfdVSF";
//
//    private final String CONTENT_APPROVE_API_KEY = "6wGxqok2XNXO5fcibdAwiEoj";
//    private final String CONTENT_APPROVE_SECRET_KEY = "fvQ8DLsW6XT5XYcV2orritFvl7f1F3ma";

    public static final String OCRTTS_APP_ID = "17380810";
    public static final String OCRTTS_API_KEY = "kA0qDiR7zLN5Pqf6zmt9uH7o";
    public static final String OCRTTS_SECRET_KEY = "rwQ1TN64IPqCAGr5xoAqxPtRu009tWfZ";

    public String getBaseToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getOCRToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_ocr_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getUnitToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_unit_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getClassifyImageToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_classify_image_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getFaceToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_face_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getHumanBodyToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_human_body_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    public String getContentApproveToken(Context context) {
        String errorMsg = context.getString(R.string.baidu_content_approve_token_fail);
        return getAuthToken(context, ALL_API_KEY, ALL_SECRET_KEY, errorMsg);
    }

    private String getAuthToken(Context context, String apiKey, String secretKey, String errMsg) {
        // 获取token地址
        final String AUTH_HOST = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = AUTH_HOST
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + apiKey
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + secretKey;
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
            return jsonObject.getString("access_token");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            CommonUtil.toast(context, errMsg);
        }
        return null;
    }
}
