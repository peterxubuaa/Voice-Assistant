package com.fih.featurephone.voiceassistant.ui;

import android.graphics.Bitmap;

public class Msg {
    public static final int TYPE_RECEIVED_TEXT = 1;
    public static final int TYPE_SEND_TEXT = 2;
    public static final int TYPE_RECEIVED_IMAGE = 3;
    public static final int TYPE_SEND_IMAGE = 4;

    private String mContent;
    private int mType;
    private Bitmap mImageBitmap;

    public Msg(Object input, int type) {
        switch (type) {
            case TYPE_RECEIVED_TEXT:
            case TYPE_SEND_TEXT:
                mContent = (String)input;
                break;
            case TYPE_RECEIVED_IMAGE:
            case TYPE_SEND_IMAGE:
                mImageBitmap = (Bitmap)input;
                break;
        }
        mType = type;
    }

    public String getContent() {
        return mContent;
    }

    int getType() {
        return mType;
    }

    public Bitmap getImageBitmap() {
        return mImageBitmap;
    }
}
