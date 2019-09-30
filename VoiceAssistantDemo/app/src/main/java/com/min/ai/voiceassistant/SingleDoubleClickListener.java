package com.min.ai.voiceassistant;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class SingleDoubleClickListener implements View.OnTouchListener {
    //记录连续点击次数
    private int mClickCount = 0;
    private Handler mHandler;
    private ClickCallBack mClickCallBack;

    public interface ClickCallBack {
        void onOneClick(View v);//点击一次的回调
        void onDoubleClick(View v);//连续点击两次的回调
    }

    SingleDoubleClickListener(ClickCallBack mClickCallBack) {
        this.mClickCallBack = mClickCallBack;
        mHandler = new Handler();
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        //双击间四百毫秒延时
        final int TIMEOUT = 400;

        v.performClick();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mClickCount++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mClickCount == 1) {
                        mClickCallBack.onOneClick(v);
                    } else if (mClickCount == 2) {
                        mClickCallBack.onDoubleClick(v);
                    }

                    mHandler.removeCallbacksAndMessages(null);
                    //清空handler延时，并防内存泄漏
                    //计数清零
                    mClickCount = 0;
                }
                //延时timeout后执行run方法中的代码
            }, TIMEOUT);
        }
        //让点击事件继续传播，方便再给View添加其他事件监听
        return true;
    }
}
