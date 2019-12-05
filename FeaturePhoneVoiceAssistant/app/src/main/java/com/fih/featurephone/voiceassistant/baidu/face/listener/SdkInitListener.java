package com.fih.featurephone.voiceassistant.baidu.face.listener;

public interface SdkInitListener {
    void initStart();
    void initLicenseSuccess();
    void initLicenseFail(int errorCode, String msg);
    void initModelSuccess();
    void initModelFail(int errorCode, String msg);
}
