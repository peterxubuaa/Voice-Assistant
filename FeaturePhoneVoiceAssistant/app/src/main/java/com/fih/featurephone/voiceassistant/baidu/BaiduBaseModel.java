package com.fih.featurephone.voiceassistant.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class BaiduBaseModel<E extends BaiduParseBaseJson.BaiduParseBaseResponse> {
    static private String sAccessToken; //只获取一次
    protected final double CLASSIFY_IMAGE_THRESHOLD = 0.5;
    protected final double PUBLIC_FIGURE_THRESHOLD = 0.8;
    protected final int FACE_IDENTIFY_THRESHOLD = 80;
    protected final double GESTURE_THRESHOLD = 0.8;

    protected final int STRING_PARAM_TYPE = 1;
    protected final int MAP_PARAM_TYPE = 2;
    protected Context mContext;
    protected BaiduBaseAI.IBaiduBaseListener mBaiduBaseListener;
    protected String mImageFilePath;
    protected boolean mQuestion;
    protected String mHostURL;
    protected int mURLRequestParamType;
    protected String mURLRequestParamString;
    protected Map<String, Object> mURLRequestParamMap = new HashMap<>();

    public BaiduBaseModel(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        mContext = context;
        mBaiduBaseListener = listener;
    }

    public String getAuthToken() {
        // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
        if (!TextUtils.isEmpty(sAccessToken)) return sAccessToken;

        //https://console.bce.baidu.com/ai/?locale=zh-cn#/ai/face/app/detail~appId=1371781
//        final String ALL_APP_ID = "17937628";
        final String ALL_API_KEY = "leiywSo72wPerpsc8DejCsfR";
        final String ALL_SECRET_KEY = "sg318tLG9uebAI7FnM9PgIH7a4sSfR9F";

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

    public void request(String imageFilePath) {
        request(imageFilePath, false);
    }

    public void request(String imageFilePath, boolean question) {
        if (null == mBaiduBaseListener) return;

        if (!FileUtils.isFileExist(imageFilePath)) {
            mBaiduBaseListener.onError("申请参数不合法！");
            return;
        }
        mImageFilePath = imageFilePath;
        mQuestion = question;

        String response = requestHostUrl();
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError("向服务器请求失败！");
            return;
        }

        E result = parseJson(response);
        if (null == result) {
            mBaiduBaseListener.onError("分析Json失败！");
            return;
        }

        if (result.mErrorCode != 0) {
            mBaiduBaseListener.onError("服务器返回失败信息：" + result.mErrorMsg);
            return;
        }

        handleResult(result);
    }

    private String requestHostUrl() {
        String response = null;
        switch (mURLRequestParamType) {
            case STRING_PARAM_TYPE:
                response = requestHostUrlForm();
                break;
            case MAP_PARAM_TYPE:
                response = requestHostUrlJson();
                break;
        }

        return response;
    }

    private String requestHostUrlForm() {
        if (TextUtils.isEmpty(mHostURL)) return null;

        try {
            byte[] buf = FileUtils.readImageFile(mImageFilePath);
            if (null == buf) return null;

            String encodeString;
            if (mHostURL.equals("https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined")) {
//                encodeString = Base64Util.encode(buf); //补丁
                encodeString = Base64.encodeToString(buf, Base64.NO_WRAP);//补丁
            } else {
                encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
            }

            String imgParam = URLEncoder.encode(encodeString, "UTF-8");
            // 请求参数
            String param = "image=" + imgParam;
            if (!TextUtils.isEmpty(mURLRequestParamString)) param += mURLRequestParamString;

            return HttpUtil.post(mHostURL, getAuthToken(), "application/x-www-form-urlencoded", param);
        } catch (Exception e) {
            e.printStackTrace();
            mBaiduBaseListener.onError(mContext.getString(R.string.network_error));
        }
        return null;
    }

    private String requestHostUrlJson() {
        try {
            byte[] buf = FileUtils.readImageFile(mImageFilePath);
            String encodeString = Base64.encodeToString(buf, Base64.DEFAULT);
            mURLRequestParamMap.put("image", encodeString);
            mURLRequestParamMap.put("image_type", "BASE64");//BASE64:图片的base64值，base64编码后的图片数据，编码后的图片大小不超过2M；URL:图片的 URL地址( 可能由于网络等原因导致下载图片时间过长)；FACE_TOKEN：人脸图片的唯一标识，调用人脸检测接口时，会为每个人脸图片赋予一个唯一的FACE_TOKEN，同一张图片多次检测得到的FACE_TOKEN是同一个。

            String jsonParam = new JSONObject(mURLRequestParamMap).toString();

            return HttpUtil.post(mHostURL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected E parseJson(String json) {
        return null;
    }

    protected void handleResult(E result) {}
}
