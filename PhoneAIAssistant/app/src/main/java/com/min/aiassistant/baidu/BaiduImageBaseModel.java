package com.min.aiassistant.baidu;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public abstract class BaiduImageBaseModel<E extends BaiduParseBaseJson.BaiduParseBaseResponse> extends BaiduBaseModel {

    protected final double CLASSIFY_IMAGE_THRESHOLD = 0.5;
    protected final double PUBLIC_FIGURE_THRESHOLD = 0.8;
    protected final int FACE_IDENTIFY_THRESHOLD = 80;
    protected final double GESTURE_THRESHOLD = 0.8;

    protected final int STRING_PARAM_TYPE = 1;
    protected final int MAP_PARAM_TYPE = 2;

    protected String mImageFilePath;
    protected boolean mQuestion;
    protected String mHostURL;
    protected int mURLRequestParamType;
    protected String mURLRequestParamString;
    protected Map<String, Object> mURLRequestParamMap = new HashMap<>();

    protected BaiduImageBaseModel(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    public void request(String imageFilePath) {
        request(imageFilePath, false);
    }

    public void request(String imageFilePath, boolean question) {
        if (null == mBaiduBaseListener) return;

        if (!FileUtils.isFileExist(imageFilePath)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.params_invalid));
            return;
        }
        mImageFilePath = imageFilePath;
        mQuestion = question;

        String response = requestHostUrl();
        if (TextUtils.isEmpty(response)) {
            mBaiduBaseListener.onError(mContext.getString(R.string.request_server_fail));
            return;
        }

        E result = parseJson(response);
        if (null == result) {
            mBaiduBaseListener.onError(mContext.getString(R.string.parse_json_fail));
            return;
        }

        if (result.mErrorCode != 0) {
            mBaiduBaseListener.onError(mContext.getString(R.string.server_fail_response) + result.mErrorMsg);
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

    abstract protected E parseJson(String json);

    abstract protected void handleResult(E result);
}
