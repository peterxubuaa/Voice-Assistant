package com.fih.featurephone.voiceassistant.baidu.faceoffline.model;

/**
 * author : shangrong
 * date : 2019/5/23 11:23 AM
 * description :配置BaseConfig单例
 */
public class SingleBaseConfig {
    private SingleBaseConfig() {}

    private static class HolderClass {
        private static final BaseConfig instance = new BaseConfig();
    }

    public static BaseConfig getBaseConfig() {
        return SingleBaseConfig.HolderClass.instance;
    }
}
