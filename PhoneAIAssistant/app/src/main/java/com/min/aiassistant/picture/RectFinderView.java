package com.min.aiassistant.picture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.min.aiassistant.R;

public final class RectFinderView extends RelativeLayout {
    private final int MIN_FOCUS_BOX_WIDTH = 50;
    private final int MIN_FOCUS_BOX_HEIGHT = 50;
    private int MIN_FOCUS_BOX_LEFT = 0;
    private int MAX_FOCUS_BOX_RIGHT = 0;
    private int MIN_FOCUS_BOX_TOP = 0;
    private int MAX_FOCUS_BOX_BOTTOM = 0;

    private double MIN_ZOOM_DISTANCE = 100.0;
    private Point mScreenResolution;
    private int mLeft, mTop;

    private Paint mPaint;
    private int mMaskColor;
    private int mFrameColor;
    private int mLaserColor;
    private int mFocusThick;
    private int mAngleThick;
    private int mAngleLength;

    private Rect mFrameRect; //绘制的Rect

    private IClickCallBack mClickCallBack = null;
    public interface IClickCallBack {
        void onGestureZoom(boolean zoomIn);//
    }

    public RectFinderView(Context context) {
        this(context, null);
    }

    public RectFinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RectFinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mMaskColor = context.getResources().getColor(R.color.finder_mask);
        mFrameColor = context.getResources().getColor(R.color.finder_frame);
        mLaserColor = context.getResources().getColor(R.color.finder_laser);

        mFocusThick = 1;
        mAngleThick = 8;
        mAngleLength = 40;

        setOnTouchListener(getTouchListener());
    }

    public void init(Point screenResolution, Rect limit, boolean bFullShow) {
        if (isInEditMode()) {
            return;
        }
        // 需要调用下面的方法才会执行onDraw方法
        setWillNotDraw(false);

        mScreenResolution = screenResolution;
        MIN_FOCUS_BOX_LEFT = limit.left;
        MIN_FOCUS_BOX_TOP = limit.top;
        MAX_FOCUS_BOX_RIGHT = limit.right;
        MAX_FOCUS_BOX_BOTTOM = limit.bottom;
        MIN_ZOOM_DISTANCE = Math.sqrt((double)mScreenResolution.x * mScreenResolution.x
                    + (double)mScreenResolution.y * mScreenResolution.y) / 20;

        resetFrameRect(bFullShow);
    }

    public void resetFrameRect(boolean bFullShow) {
        int width = MAX_FOCUS_BOX_RIGHT - MIN_FOCUS_BOX_LEFT;//mScreenResolution.x;
        int height = bFullShow? (MAX_FOCUS_BOX_BOTTOM - MIN_FOCUS_BOX_TOP) : (MAX_FOCUS_BOX_BOTTOM - MIN_FOCUS_BOX_TOP) / 2;

        width = width == 0
                ? MIN_FOCUS_BOX_WIDTH
                : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;

        height = height == 0
                ? MIN_FOCUS_BOX_HEIGHT
                : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

        int left = (mScreenResolution.x - width) / 2;
        int top = bFullShow? MIN_FOCUS_BOX_TOP : (MAX_FOCUS_BOX_BOTTOM + MIN_FOCUS_BOX_TOP)/2 - height / 2;
        mLeft = left; //记录初始距离左方距离
        mTop = top; //记录初始距离上方距离

        mFrameRect = new Rect(left, top, left + width, top + height);
    }

    public Rect getRect() {
        return mFrameRect;
    }

    public void setClickCallBack (IClickCallBack clickCallBack) {
        mClickCallBack = clickCallBack;
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
                    if (null != mClickCallBack) mClickCallBack.onGestureZoom(zoomIn);
                    mLastZoomValue = zoomValue;
                    Log.i("Zoom", "zoom camera " + zoomIn);
                }
            }
        }

        return  true;
    }

    int mSingleTouchLastX = -1, mSingleTouchLastY = -1;
    private boolean onSingleTouchPointer(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
                try {
                    Rect rect = mFrameRect;
                    final int OFFSET = 60;
                    if (mSingleTouchLastX >= 0) {
                        boolean currentXLeft = currentX >= rect.left - OFFSET && currentX <= rect.left + OFFSET;
                        boolean currentXRight = currentX >= rect.right - OFFSET && currentX <= rect.right + OFFSET;
                        boolean lastXLeft = mSingleTouchLastX >= rect.left - OFFSET && mSingleTouchLastX <= rect.left + OFFSET;
                        boolean lastXRight = mSingleTouchLastX >= rect.right - OFFSET && mSingleTouchLastX <= rect.right + OFFSET;

                        boolean currentYTop = currentY <= rect.top + OFFSET && currentY >= rect.top - OFFSET;
                        boolean currentYBottom = currentY <= rect.bottom + OFFSET && currentY >= rect.bottom - OFFSET;
                        boolean lastYTop = mSingleTouchLastY <= rect.top + OFFSET && mSingleTouchLastY >= rect.top - OFFSET;
                        boolean lastYBottom = mSingleTouchLastY <= rect.bottom + OFFSET && mSingleTouchLastY >= rect.bottom - OFFSET;

                        boolean XLeft = currentXLeft || lastXLeft;
                        boolean XRight = currentXRight || lastXRight;
                        boolean YTop = currentYTop || lastYTop;
                        boolean YBottom = currentYBottom || lastYBottom;

                        boolean YTopBottom = (currentY <= rect.bottom && currentY >= rect.top)
                                || (mSingleTouchLastY <= rect.bottom && mSingleTouchLastY >= rect.top);
                        boolean XLeftRight = (currentX <= rect.right && currentX >= rect.left)
                                || (mSingleTouchLastX <= rect.right && mSingleTouchLastX >= rect.left);

                        if (XLeft && YTop) {//左上角
                            updateBoxRect((mSingleTouchLastX - currentX), (mSingleTouchLastY - currentY), true, true);
                        } else if (XRight && YTop) {//右上角
                            updateBoxRect((currentX - mSingleTouchLastX), (mSingleTouchLastY - currentY), true, false);
                        } else if (XLeft && YBottom) {//左下角
                            updateBoxRect((mSingleTouchLastX - currentX), (currentY - mSingleTouchLastY), false, true);
                        } else if (XRight && YBottom) {//右下角
                            updateBoxRect((currentX - mSingleTouchLastX), (currentY - mSingleTouchLastY), false, false);
                        } else if (XLeft && YTopBottom) {//左侧
                            updateBoxRect((mSingleTouchLastX - currentX), 0, false, true);
                        } else if (XRight && YTopBottom) {//右侧
                            updateBoxRect((currentX - mSingleTouchLastX), 0, false, false);
                        } else if (YTop && XLeftRight) {//上方
                            updateBoxRect(0, (mSingleTouchLastY - currentY), true, false);
                        } else if (YBottom && XLeftRight) {//下方
                            updateBoxRect(0, (currentY - mSingleTouchLastY), false, false);
                        } else if (currentX > rect.left + OFFSET && currentX < rect.right - OFFSET
                                        && currentY > rect.top + OFFSET && currentY < rect.bottom - OFFSET) { //在矩形框内
                            moveBoxRect(currentX - mSingleTouchLastX, currentY - mSingleTouchLastY);
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
            case MotionEvent.ACTION_DOWN:
                mSingleTouchLastX = -1;
                mSingleTouchLastY = -1;
                return true;
            default:
        }
        return false;
    }

    private void updateBoxRect(int dW, int dH, boolean isUpward, boolean isLeftward) {
        int newWidth = (mFrameRect.width() + dW > mScreenResolution.x || mFrameRect.width() + dW < MIN_FOCUS_BOX_WIDTH)
                ? 0 : mFrameRect.width() + dW;

        //限制扫描框最大高度不超过屏幕宽度
        int newHeight = (mFrameRect.height() + dH > mScreenResolution.y || mFrameRect.height() + dH < MIN_FOCUS_BOX_HEIGHT)
                ? 0 : mFrameRect.height() + dH;

        if (newWidth < MIN_FOCUS_BOX_WIDTH || newHeight < MIN_FOCUS_BOX_HEIGHT){
            return;
        }
//        int leftOffset = (mScreenResolution.x - newWidth) / 2;
        if (isLeftward) mLeft -= dW;
        int leftOffset = mLeft;
        if (leftOffset < MIN_FOCUS_BOX_LEFT) {
            mLeft = MIN_FOCUS_BOX_LEFT;
            return;
        }
        if (leftOffset + newWidth > MAX_FOCUS_BOX_RIGHT) return;

        if (isUpward) mTop -= dH;
        int topOffset = mTop;
        if (topOffset < MIN_FOCUS_BOX_TOP){
            mTop = MIN_FOCUS_BOX_TOP;
            return;
        }
        if (topOffset + newHeight > MAX_FOCUS_BOX_BOTTOM) return;

//        mFrameRect = new Rect(leftOffset, topOffset, leftOffset + newWidth, topOffset + newHeight);
        mFrameRect.left = leftOffset;
        mFrameRect.top = topOffset;
        mFrameRect.right = leftOffset + newWidth;
        mFrameRect.bottom = topOffset + newHeight;
    }

    private void moveBoxRect(int moveX, int moveY) {
        mFrameRect.left = mFrameRect.left + moveX < MIN_FOCUS_BOX_LEFT? MIN_FOCUS_BOX_LEFT : mFrameRect.left + moveX ;
        mFrameRect.top = mFrameRect.top + moveY < MIN_FOCUS_BOX_TOP? MIN_FOCUS_BOX_TOP : mFrameRect.top + moveY;
        mFrameRect.right = mFrameRect.right + moveX > MAX_FOCUS_BOX_RIGHT? MAX_FOCUS_BOX_RIGHT : mFrameRect.right + moveX;
        mFrameRect.bottom = mFrameRect.bottom + moveY > MAX_FOCUS_BOX_BOTTOM? MAX_FOCUS_BOX_BOTTOM : mFrameRect.bottom + moveY;

        mLeft = mFrameRect.left;
        mTop = mFrameRect.top;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
