package com.fih.featurephone.voiceassistant.ui;

public class Msg {
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SEND = 2;

    private String mContent;
    private int mType;

    public Msg(String content, int type) {
        mContent = content;
        mType = type;
    }

    public String getContent() {
        return mContent;
    }

    int getType() {
        return mType;
    }
}
