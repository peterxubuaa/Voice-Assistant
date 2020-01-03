package com.fih.featurephone.voiceassistant.baidu.contentapprove;

import android.content.Context;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.contentapprove.model.PublicFigure;

public class BaiduContentApproveAI extends BaiduBaseAI {
    public static final int PUBLIC_FIGURE_ACTION = 1;
    public static final int PUBLIC_FIGURE_QUESTION_ACTION = 2;

    private PublicFigure mPublicFigure;

    public BaiduContentApproveAI(Context context, IBaiduBaseListener listener) {
        super(context, listener);
        mPublicFigure = new PublicFigure(context, listener);
    }

    public void onPublicFigureThread(final String imageFilePath, final boolean question) {
        new Thread() {
            @Override
            public void run() {
                mPublicFigure.request(imageFilePath, question);
            }
        }.start();
    }
}
