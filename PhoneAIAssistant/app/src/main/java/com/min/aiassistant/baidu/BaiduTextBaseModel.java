package com.min.aiassistant.baidu;

import android.content.Context;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.HttpUtil;

import org.json.JSONObject;

import java.util.Map;

public abstract class BaiduTextBaseModel<E extends BaiduParseBaseJson.BaiduParseBaseResponse> extends BaiduBaseModel {
    protected String mHostURL;

    protected BaiduTextBaseModel(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
    }

    public void request(Map<String, Object> requestParamMap) {
        if (null == mBaiduBaseListener) return;

        if (null == requestParamMap || requestParamMap.size() == 0) {
            mBaiduBaseListener.onError(mContext.getString(R.string.params_invalid));
            return;
        }

        String response = requestHostUrl(requestParamMap);
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

    private String requestHostUrl(Map<String, Object> requestParamMap) {
        try {
            String jsonParam = new JSONObject(requestParamMap).toString();

            return HttpUtil.post(mHostURL, getAuthToken(), "application/json", jsonParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    abstract protected E parseJson(String json);

    abstract protected void handleResult(E result);
}
