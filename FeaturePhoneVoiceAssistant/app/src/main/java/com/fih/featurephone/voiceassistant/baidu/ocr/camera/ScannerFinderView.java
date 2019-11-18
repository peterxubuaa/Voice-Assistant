package com.fih.featurephone.voiceassistant.baidu.ocr.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.fih.featurephone.voiceassistant.R;

import java.lang.ref.WeakReference;

public final class ScannerFinderView extends RelativeLayout {

    private final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };

    private final int MIN_FOCUS_BOX_WIDTH = 50;
    private final int MIN_FOCUS_BOX_HEIGHT = 50;
//    private final int MIN_FOCUS_BOX_PAD = 10;
    private int MIN_FOCUS_BOX_TOP = 200;
    private int MAX_FOCUS_BOX_BOTTOM = 600;

    private Point mScreenResolution;
    private int mTop;

    private Paint mPaint;
    private int mScannerAlpha;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
//    private int mTextColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect
    private Handler mViewHandler;
//    private String mDrawText;
    private int mLaserPos;

    //记录连续点击次数
    private int mClickCount = 0;
    private Handler mClickHandler;
    private IClickCallBack mClickCallBack = null;
    public interface IClickCallBack {
        void onOneClick();//点击一次的回调
        void onDoubleClick();//连续点击两次的回调
    }

    public ScannerFinderView(Context context) {
        this(context, null);
    }

    public ScannerFinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewHandler = new ViewHandler(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMaskColor = context.getResources().getColor(R.color.finder_mask);
        mFrameColor = context.getResources().getColor(R.color.finder_frame);
        mLaserColor = context.getResources().getColor(R.color.finder_laser);
//        mTextColor = context.getResources().getColor(R.color.white);

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;
        mScannerAlpha = 0;
        mLaserPos = 0;
//        init();

        mClickHandler = new Handler();
        setOnTouchListener(getTouchListener());
    }

/*
    public void setClickCallBack(IClickCallBack clickCallBack) {
        mClickCallBack = clickCallBack;
    }

    public void setDrawText(String text) {
        mDrawText = text;
        invalidate();
    }
*/

    public void init(Point screenResolution, int topLimit, int bottomLimit, boolean bMaxRect) {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);

        mScreenResolution = screenResolution;
        MIN_FOCUS_BOX_TOP = topLimit;
        MAX_FOCUS_BOX_BOTTOM = bottomLimit;

        resetFrameRect(bMaxRect);
    }

    public void resetFrameRect(boolean bMaxRect) {
        int width = mScreenResolution.x;
        int height = bMaxRect? (MAX_FOCUS_BOX_BOTTOM - MIN_FOCUS_BOX_TOP) : (MAX_FOCUS_BOX_BOTTOM - MIN_FOCUS_BOX_TOP) / 2;

        width = width == 0
                ? MIN_FOCUS_BOX_WIDTH
                : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;

        height = height == 0
                ? MIN_FOCUS_BOX_HEIGHT
                : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

        int left = (mScreenResolution.x - width) / 2;
        int top = bMaxRect? MIN_FOCUS_BOX_TOP : (MAX_FOCUS_BOX_BOTTOM + MIN_FOCUS_BOX_TOP)/2 -  height / 2;
        mTop = top; //记录初始距离上方距离
        mLaserPos = top;

        mFrameRect = new Rect(left, top, left + width, top + height);
    }

    public Rect getRect() {
        return mFrameRect;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        Rect frame = mFrameRect;
        if (frame == null) {
            return;
        }
        int width = getWidth();
        int height = getHeight();

        // 绘制焦点框外边的暗色背景
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, frame.top, mPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, mPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, mPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, mPaint);

        drawFocusRect(canvas, frame);
        drawAngle(canvas, frame);
//        drawText(canvas, frame);
//        drawLaser(canvas, frame);
    }

    /**
     * 画聚焦框，白色的
     *
     */
    private void drawFocusRect(Canvas canvas, Rect rect) {
        // 绘制焦点框（黑色）
        mPaint.setColor(mFrameColor);
        // 上
        canvas.drawRect(rect.left + mAngleLength, rect.top, rect.right - mAngleLength, rect.top + mFocusThick, mPaint);
        // 左
        canvas.drawRect(rect.left, rect.top + mAngleLength, rect.left + mFocusThick, rect.bottom - mAngleLength,
                mPaint);
        // 右
        canvas.drawRect(rect.right - mFocusThick, rect.top + mAngleLength, rect.right, rect.bottom - mAngleLength,
                mPaint);
        // 下
        canvas.drawRect(rect.left + mAngleLength, rect.bottom - mFocusThick, rect.right - mAngleLength, rect.bottom,
                mPaint);
    }

    /**
     * 画四个角
     *
     */
    private void drawAngle(Canvas canvas, Rect rect) {
        final int OPAQUE = 0xFF;

        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(OPAQUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mAngleThick);
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        // 左上角
        canvas.drawRect(left, top, left + mAngleLength, top + mAngleThick, mPaint);
        canvas.drawRect(left, top, left + mAngleThick, top + mAngleLength, mPaint);
        // 右上角
        canvas.drawRect(right - mAngleLength, top, right, top + mAngleThick, mPaint);
        canvas.drawRect(right - mAngleThick, top, right, top + mAngleLength, mPaint);
        // 左下角
        canvas.drawRect(left, bottom - mAngleLength, left + mAngleThick, bottom, mPaint);
        canvas.drawRect(left, bottom - mAngleThick, left + mAngleLength, bottom, mPaint);
        // 右下角
        canvas.drawRect(right - mAngleLength, bottom - mAngleThick, right, bottom, mPaint);
        canvas.drawRect(right - mAngleThick, bottom - mAngleLength, right, bottom, mPaint);
    }

/*    private void drawText(Canvas canvas, Rect rect) {
        int margin = -30;//40
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(getResources().getDimension(R.dimen.text_size_20sp));
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        float newY = rect.bottom + margin + offY;
        float left = (mScreenResolution.x - mPaint.getTextSize() * mDrawText.length()) / 2;
        canvas.drawText(mDrawText, left, newY, mPaint);
    }*/

    private void drawLaser(Canvas canvas, Rect rect) {
        final long ANIMATION_DELAY = 25L;//100L;

        // 绘制焦点框内固定的一条扫描线
        mPaint.setColor(mLaserColor);
        mPaint.setAlpha(SCANNER_ALPHA[mScannerAlpha]);
        mScannerAlpha = (mScannerAlpha + 1) % SCANNER_ALPHA.length;
        if (mLaserPos > rect.bottom) {
            mLaserPos = rect.top;
        } else {
            mLaserPos += 6;
        }
        canvas.drawRect(rect.left + 2, mLaserPos - 2, rect.right - 1, mLaserPos + 2, mPaint);

        mViewHandler.sendEmptyMessageDelayed(1, ANIMATION_DELAY);
    }

    static class ViewHandler extends Handler {
        WeakReference<ScannerFinderView> mTheView;

        ViewHandler(ScannerFinderView view) {
            mTheView = new WeakReference<ScannerFinderView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTheView.get().invalidate();
        }
    }

    private OnTouchListener getTouchListener() {
        return new OnTouchListener() {
            int lastX = -1;
            int lastY = -1;
            //双击间四百毫秒延时
            final int TIMEOUT = 400; //ms
            final int CLICK_OFFSET = 100;
            int lastDownX = -1, lastUpX = -1;
            int lastDownY = -1, lastUpY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                performClick();//???
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (null != mClickCallBack) {
                            mClickCount++;
                            lastDownX = (int) event.getX();
                            lastDownY = (int) event.getY();
                            mClickHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (1 == mClickCount) {
                                        if (Math.abs(lastDownX - lastUpX) < CLICK_OFFSET && Math.abs(lastDownY - lastUpY) < CLICK_OFFSET) {
                                            mClickCallBack.onOneClick();
                                        }
                                    } else if (2 == mClickCount) {
                                        mClickCallBack.onDoubleClick();
                                    }
                                    //清空handler延时，并防内存泄漏
                                    mClickHandler.removeCallbacksAndMessages(null);
                                    //计数清零
                                    mClickCount = 0;
                                }
                                //延时timeout后执行run方法中的代码
                            }, TIMEOUT);
                        }

                        lastX = -1;
                        lastY = -1;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int currentX = (int) event.getX();
                        int currentY = (int) event.getY();
                        try {
                            Rect rect = mFrameRect;
                            final int BUFFER = 60;
                            if (lastX >= 0) {

                                boolean currentXLeft = currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER;
                                boolean currentXRight = currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER;
                                boolean lastXLeft = lastX >= rect.left - BUFFER && lastX <= rect.left + BUFFER;
                                boolean lastXRight = lastX >= rect.right - BUFFER && lastX <= rect.right + BUFFER;

                                boolean currentYTop = currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER;
                                boolean currentYBottom = currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER;
                                boolean lastYTop = lastY <= rect.top + BUFFER && lastY >= rect.top - BUFFER;
                                boolean lastYBottom = lastY <= rect.bottom + BUFFER && lastY >= rect.bottom - BUFFER;

                                boolean XLeft = currentXLeft || lastXLeft;
                                boolean XRight = currentXRight || lastXRight;
                                boolean YTop = currentYTop || lastYTop;
                                boolean YBottom = currentYBottom || lastYBottom;

                                boolean YTopBottom = (currentY <= rect.bottom && currentY >= rect.top)
                                        || (lastY <= rect.bottom && lastY >= rect.top);

                                boolean XLeftRight = (currentX <= rect.right && currentX >= rect.left)
                                        || (lastX <= rect.right && lastX >= rect.left);

                                    //右上角
                                if (XLeft && YTop) {
                                    updateBoxRect(2 * (lastX - currentX), (lastY - currentY), true);
                                    //左上角
                                } else if (XRight && YTop) {
                                    updateBoxRect(2 * (currentX - lastX), (lastY - currentY), true);
                                    //右下角
                                } else if (XLeft && YBottom) {
                                    updateBoxRect(2 * (lastX - currentX), (currentY - lastY), false);
                                    //左下角
                                } else if (XRight && YBottom) {
                                    updateBoxRect(2 * (currentX - lastX), (currentY - lastY), false);
                                    //左侧
                                } else if (XLeft && YTopBottom) {
                                    updateBoxRect(2 * (lastX - currentX), 0, false);
                                    //右侧
                                } else if (XRight && YTopBottom) {
                                    updateBoxRect(2 * (currentX - lastX), 0, false);
                                    //上方
                                } else if (YTop && XLeftRight) {
                                    updateBoxRect(0, (lastY - currentY), true);
                                    //下方
                                } else if (YBottom && XLeftRight) {
                                    updateBoxRect(0, (currentY - lastY), false);
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        v.invalidate();
                        lastX = currentX;
                        lastY = currentY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (null != mClickCallBack) {
                            lastUpX = (int) event.getX();
                            lastUpY = (int) event.getY();
                        }

                        //移除之前的刷新
                        mViewHandler.removeMessages(1);
                        //松手时对外更新
                        lastX = -1;
                        lastY = -1;
                        return true;
                    default:

                }
                return false;
            }
        };
    }

    private void updateBoxRect(int dW, int dH, boolean isUpward) {

        int newWidth = (mFrameRect.width() + dW > mScreenResolution.x || mFrameRect.width() + dW < MIN_FOCUS_BOX_WIDTH)
                ? 0 : mFrameRect.width() + dW;

        //限制扫描框最大高度不超过屏幕宽度
        int newHeight = (mFrameRect.height() + dH > mScreenResolution.y || mFrameRect.height() + dH < MIN_FOCUS_BOX_HEIGHT)
                ? 0 : mFrameRect.height() + dH;

        if (newWidth < MIN_FOCUS_BOX_WIDTH || newHeight < MIN_FOCUS_BOX_HEIGHT){
            return;
        }

        int leftOffset = (mScreenResolution.x - newWidth) / 2;

        if (isUpward){
            mTop -= dH;
        }

        int topOffset = mTop;

        if (topOffset < MIN_FOCUS_BOX_TOP){
            mTop = MIN_FOCUS_BOX_TOP;
            return;
        }

        if (topOffset + newHeight > MAX_FOCUS_BOX_BOTTOM){
            return;
        }

        mFrameRect = new Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViewHandler.removeMessages(1);
    }
}
