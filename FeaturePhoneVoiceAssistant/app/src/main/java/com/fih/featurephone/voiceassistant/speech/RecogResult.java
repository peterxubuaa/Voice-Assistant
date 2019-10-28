package com.fih.featurephone.voiceassistant.speech;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fujiayi on 2017/6/24.
 */
public class RecogResult {
    private static final int ERROR_NONE = 0;

    private String mOrigalJson;
    private String[] mResultsRecognition;
    private String mOrigalResult;
    private String mSN; // 日志id， 请求有问题请提问带上sn
    private String mDesc;
    private String mResultType;
    private int mError = -1;
    private int mSubError = -1;

    public static RecogResult parseJson(String jsonStr) {
        RecogResult result = new RecogResult();
        result.setOrigalJson(jsonStr);
        try {
            JSONObject json = new JSONObject(jsonStr);
            int error = json.optInt("error");
            int subError = json.optInt("sub_error");
            result.setError(error);
            result.setDesc(json.optString("desc"));
            result.setResultType(json.optString("result_type"));
            result.setSubError(subError);
            if (error == ERROR_NONE) {
                result.setOrigalResult(json.getString("origin_result"));
                JSONArray arr = json.optJSONArray("results_recognition");
                if (arr != null) {
                    int size = arr.length();
                    String[] recogs = new String[size];
                    for (int i = 0; i < size; i++) {
                        recogs[i] = arr.getString(i);
                    }
                    result.setResultsRecognition(recogs);
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean hasError() {
        return mError != ERROR_NONE;
    }

    public boolean isFinalResult() {
        return "final_result".equals(mResultType);
    }


    public boolean isPartialResult() {
        return "partial_result".equals(mResultType);
    }

    public boolean isNluResult() {
        return "nlu_result".equals(mResultType);
    }

    public String getOrigalJson() {
        return mOrigalJson;
    }

    public void setOrigalJson(String origalJson) {
        mOrigalJson = origalJson;
    }

    public String[] getResultsRecognition() {
        return mResultsRecognition;
    }

    public void setResultsRecognition(String[] resultsRecognition) {
        mResultsRecognition = resultsRecognition;
    }

    public String getSn() {
        return mSN;
    }

    public void setSn(String sn) {
        mSN = sn;
    }

    public int getError() {
        return mError;
    }

    public void setError(int error) {
        mError = error;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }

    public String getOrigalResult() {
        return mOrigalResult;
    }

    public void setOrigalResult(String origalResult) {
        mOrigalResult = origalResult;
    }

    public String getResultType() {
        return mResultType;
    }

    public void setResultType(String resultType) {
        mResultType = resultType;
    }

    public int getSubError() {
        return mSubError;
    }

    public void setSubError(int subError) {
        mSubError = subError;
    }
}
