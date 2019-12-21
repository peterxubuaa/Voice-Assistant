package com.fih.featurephone.voiceassistant.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.fih.featurephone.voiceassistant.R;

import java.lang.ref.WeakReference;

public final class ScannerFinderView extends RelativeLayout {
    private final int MIN_FOCUS_BOX_WIDTH = 50;
    private final int MIN_FOCUS_BOX_HEIGHT = 50;
    private int MIN_FOCUS_BOX_TOP = 200;
    private int MAX_FOCUS_BOX_BOTTOM = 600;

    private double MIN_ZOOM_DISTANCE = 100.0;
    private Point mScreenResolution;
    private int mTop;

    private Paint mPaint;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect
    private Handler mViewHandler;

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

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;

        mClickHandler = new Handler();
        setOnTouchListener(getTouchListener());
    }

    public void init(Point screenResolution, int topLimit, int bottomLimit, boolean bMaxRect) {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);

        mScreenResolution = screenResolution;
        MIN_FOCUS_BOX_TOP = topLimit;
        MAX_FOCUS_BOX_BOTTOM = bottomLimit;
        MIN_ZOOM_DISTANCE = Math.sqrt((double)mScreenResolution.x * mScreenResolution.x
                    + (double)mScreenResolution.y * mScreenResolution.y) / 20;

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
    }

    // 画聚焦框，白色的
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

    // 画四个角
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

    static class ViewHandler extends Handler {
        WeakReference<ScannerFinderView> mTheView;

        ViewHandler(ScannerFinderView view) {
            mTheView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTheView.get().invalidate();
        }
    }

    private OnTouchListener getTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                performClick();//???
                if (event.getPointerCount() == 1) {
                    return onSingleTouchPointer(v, event);
                } else if (event.getPointerCount() == 2) {
                    return onDoubleTouchPointer(event);
                }
                return false;
            }
        };
    }

    private double mStartDistance;
    private int mLastZoomValue = 0;
    public boolean onDoubleTouchPointer(MotionEvent event) {
        int action  = event.getAction();// 获取触屏动作。比如：按下、移动和抬起等手势动作
        // 手势按下且屏幕上是两个手指数量时
        if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN) {
            // 获取按下时候两个坐标的x轴的水平距离，取绝对值
            int widthDownLen = Math.abs((int) event.getX(0) - (int) event.getX(1));
            // 获取按下时候两个坐标的y轴的水平距离，取绝对值
            int heightDownLen = Math.abs((int) event.getY(0) - (int) event.getY(1));
            // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指刚按下时的直线距离
            mStartDistance = Math.sqrt((double)widthDownLen * widthDownLen + (double)heightDownLen * heightDownLen);
            mLastZoomValue = 0;
        } else if ((action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP
                    || action == MotionEvent.ACTION_MOVE) {
            // 获取抬起时候两个坐标的x轴的水平距离，取绝对值
            int widthMoveLen = Math.abs((int)event.getX(0) - (int)event.getX(1));
            // 获取抬起时候两个坐标的y轴的水平距离，取绝对值
            int heightMoveLen = Math.abs((int)event.getY(0) - (int)event.getY(1));
            // 根据x轴和y轴的水平距离，求平方和后再开方获取两个点之间的直线距离。此时就获取到了两个手指抬起时的直线距离
            double endDistance = Math.sqrt((double)widthMoveLen * widthMoveLen + (double)heightMoveLen * heightMoveLen);
            // 根据手势按下时两个手指触点之间的直线距离A和手势抬起时两个手指触点之间的直线距离B。比较A和B的大小，得出用户是手势放大还是手势缩小
            Log.i("Zoom", "start = " + mStartDistance + ", end = " + endDistance + ", MIN_ZOOM_DISTANCE = " + MIN_ZOOM_DISTANCE);

            if (Math.abs(mStartDistance - endDistance) > MIN_ZOOM_DISTANCE) {//减少误碰
                int zoomValue = (int)((endDistance - mStartDistance) / MIN_ZOOM_DISTANCE);
                if (zoomValue != mLastZoomValue) {
                    boolean zoomIn = zoomValue > mLastZoomValue;
                    CameraManager.getInstance().setZoom(zoomIn);
                    mLastZoomValue = zoomValue;
                    Log.i("Zoom", "zoom camera " + zoomIn);
                }
            }
        }

        return  true;
    }

    final int TIMEOUT = 400; ////双击间四百毫秒延时ms
    final int CLICK_OFFSET = 100;
    int mSingleTouchLastX = -1, mSingleTouchLastY = -1;
    int mSingleTouchLastDownX = -1, mSingleTouchLastUpX = -1, mSingleTouchLastDownY = -1, mSingleTouchLastUpY = -1;
    private boolean onSingleTouchPointer(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (null != mClickCallBack) {
                    mClickCount++;
                    mSingleTouchLastDownX = (int) event.getX();
                    mSingleTouchLastDownY = (int) event.getY();
                    mClickHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (1 == mClickCount) {
                                if (Math.abs(mSingleTouchLastDownX - mSingleTouchLastUpX) < CLICK_OFFSET && Math.abs(mSingleTouchLastDownY - mSingleTouchLastUpY) < CLICK_OFFSET) {
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

                mSingleTouchLastX = -1;
                mSingleTouchLastY = -1;
                return true;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
                try {
                    Rect rect = mFrameRect;
                    final int BUFFER = 60;
                    if (mSingleTouchLastX >= 0) {

                        boolean currentXLeft = currentX >= rect.left - BUFFER && currentX <= rect.left + BUFFER;
                        boolean currentXRight = currentX >= rect.right - BUFFER && currentX <= rect.right + BUFFER;
                        boolean lastXLeft = mSingleTouchLastX >= rect.left - BUFFER && mSingleTouchLastX <= rect.left + BUFFER;
                        boolean lastXRight = mSingleTouchLastX >= rect.right - BUFFER && mSingleTouchLastX <= rect.right + BUFFER;

                        boolean currentYTop = currentY <= rect.top + BUFFER && currentY >= rect.top - BUFFER;
                        boolean currentYBottom = currentY <= rect.bottom + BUFFER && currentY >= rect.bottom - BUFFER;
                        boolean lastYTop = mSingleTouchLastY <= rect.top + BUFFER && mSingleTouchLastY >= rect.top - BUFFER;
                        boolean lastYBottom = mSingleTouchLastY <= rect.bottom + BUFFER && mSingleTouchLastY >= rect.bottom - BUFFER;

                        boolean XLeft = currentXLeft || lastXLeft;
                        boolean XRight = currentXRight || lastXRight;
                        boolean YTop = currentYTop || lastYTop;
                        boolean YBottom = currentYBottom || lastYBottom;

                        boolean YTopBottom = (currentY <= rect.bottom && currentY >= rect.top)
                                || (mSingleTouchLastY <= rect.bottom && mSingleTouchLastY >= rect.top);

                        boolean XLeftRight = (currentX <= rect.right && currentX >= rect.left)
                                || (mSingleTouchLastX <= rect.right && mSingleTouchLastX >= rect.left);

                        //右上角
                        if (XLeft && YTop) {
                            updateBoxRect(2 * (mSingleTouchLastX - currentX), (mSingleTouchLastY - currentY), true);
                            //左上角
                        } else if (XRight && YTop) {
                            updateBoxRect(2 * (currentX - mSingleTouchLastX), (mSingleTouchLastY - currentY), true);
                            //右下角
                        } else if (XLeft && YBottom) {
                            updateBoxRect(2 * (mSingleTouchLastX - currentX), (currentY - mSingleTouchLastY), false);
                            //左下角
                        } else if (XRight && YBottom) {
                            updateBoxRect(2 * (currentX - mSingleTouchLastX), (currentY - mSingleTouchLastY), false);
                            //左侧
                        } else if (XLeft && YTopBottom) {
                            updateBoxRect(2 * (mSingleTouchLastX - currentX), 0, false);
                            //右侧
                        } else if (XRight && YTopBottom) {
                            updateBoxRect(2 * (currentX - mSingleTouchLastX), 0, false);
                            //上方
                        } else if (YTop && XLeftRight) {
                            updateBoxRect(0, (mSingleTouchLastY - currentY), true);
                            //下方
                        } else if (YBottom && XLeftRight) {
                            updateBoxRect(0, (currentY - mSingleTouchLastY), false);
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                v.invalidate();
                mSingleTouchLastX = currentX;
                mSingleTouchLastY = currentY;
                return true;
            case MotionEvent.ACTION_UP:
                if (null != mClickCallBack) {
                    mSingleTouchLastUpX = (int) event.getX();
                    mSingleTouchLastUpY = (int) event.getY();
                }

                //移除之前的刷新
                mViewHandler.removeMessages(1);
                //松手时对外更新
                mSingleTouchLastX = -1;
                mSingleTouchLastY = -1;
                return true;
            default:
        }
        return false;
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
