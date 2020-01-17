package com.min.aiassistant.baidu.contentapprove;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.contentapprove.model.PublicFigure;

public class BaiduContentApproveAI extends BaiduBaseAI {
    public static final int PUBLIC_FIGURE_ACTION = 1;
    public static final int PUBLIC_FIGURE_QUESTION_ACTION = 2;

    private PublicFigure mPublicFigure;

    public BaiduContentApproveAI(Context context, IBaiduBaseListener listener) {
        mPublicFigure = new PublicFigure(context, listener);
    }

    public void action(final String imageFilePath, final boolean question) {
        new Thread() {
            @Override
            public void run() {
                mPublicFigure.request(imageFilePath, question);
            }
        }.start();
    }
}
