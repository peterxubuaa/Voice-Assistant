package com.min.aiassistant.baidu.imageprocess;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.imageprocess.model.Colourize;
import com.min.aiassistant.baidu.imageprocess.model.SelfieAnime;
import com.min.aiassistant.baidu.imageprocess.model.StyleTrans;

public class BaiduImageProcessAI extends BaiduBaseAI {
    public static final int SELFIE_ANIME_TYPE = 1;
    public static final int COLOURIZE_TYPE = 2;
    public static final int STYLE_TRANS_TYPE = 3;

    public static final int SELFIE_ANIME_ACTION = 100;
    public static final int COLOURIZE_ACTION = 200;

    private SelfieAnime mSelfieAnime;
    private Colourize mColourize;
    private StyleTrans mStyleTrans;

    public BaiduImageProcessAI(Context context, IBaiduBaseListener listener) {
        mSelfieAnime = new SelfieAnime(context, listener);
        mColourize = new Colourize(context, listener);
        mStyleTrans = new StyleTrans(context, listener);
    }

    public void action(final int type, final String imageFilePath) {
        new Thread() {
            @Override
            public void run() {
                switch (type) {
                    case SELFIE_ANIME_TYPE:
                        mSelfieAnime.request(imageFilePath);
                        break;
                    case COLOURIZE_TYPE:
                        mColourize.request(imageFilePath);
                        break;
                    case STYLE_TRANS_TYPE:
                        mStyleTrans.request(imageFilePath);
                        break;
                }
            }
        }.start();
    }
}

