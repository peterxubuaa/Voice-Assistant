package com.fih.featurephone.voiceassistant.baidu.face.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * 人脸检测区域View
 */
public class FaceRoundView extends View {
    public enum COVER_TYPE {
        RECT, CIRCLE, OVAL
    }
    private Paint mBGPaint;
    private Paint mFaceRoundPaint;
    private float mX;
    private float mY;
    private float mR;
    private RectF mRectF = new RectF();
    private COVER_TYPE mCoverType = COVER_TYPE.CIRCLE;

    public FaceRoundView(Context context) {
        super(context);
        init();
    }

    public FaceRoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceRoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBGPaint.setColor(Color.parseColor("#80000000")); //#FFFFFF 白色， #80000000 半透明
        mBGPaint.setStyle(Paint.Style.FILL);
        mBGPaint.setAntiAlias(true);
        mBGPaint.setDither(true);

        mFaceRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mFaceRoundPaint.setColor(Color.parseColor("#33CC83"));
        mFaceRoundPaint.setStyle(Paint.Style.FILL);
        mFaceRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mFaceRoundPaint.setAntiAlias(true);
        mFaceRoundPaint.setDither(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final float WIDTH_SPACE_RATIO = 0.4f; //必须<0.5, 圆相当于占短边的4/5
        final float HEIGHT_RATIO = 0.1f;
        final float HEIGHT_WIDTH_RADIO_OVAL = 1.2f;

        float canvasWidth = right - left;
        float canvasHeight = bottom - top;
        if (canvasWidth >= canvasHeight) {
            // 横屏显示器
            float x = canvasWidth / 2;
            float y = (canvasHeight / 2) - ((canvasHeight / 2) * HEIGHT_RATIO);
            float r = canvasHeight * WIDTH_SPACE_RATIO;

            mX = x;
            mY = y;
            mR = r;
        } else {
            // 竖屏显示器
            float x = canvasWidth / 2;
            float y = canvasHeight / 2;
            float r = canvasWidth * WIDTH_SPACE_RATIO;

            mX = x;
            mY = y;
            mR = r;
        }

        mRectF.left = mX - mR;
        mRectF.top = mY - mR * HEIGHT_WIDTH_RADIO_OVAL;
        mRectF.right = mX + mR;
        mRectF.bottom = mY + mR * HEIGHT_WIDTH_RADIO_OVAL;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 遮罩
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawPaint(mBGPaint);
        switch (mCoverType.ordinal()) {
            case 0: //rect
                canvas.drawRoundRect(mRectF, mRectF.width()/10, mRectF.height()/10, mFaceRoundPaint);
                break;
            case 1: //circle
                canvas.drawCircle(mX, mY, mR, mFaceRoundPaint);
                break;
            case 2: //oval
                canvas.drawOval(mRectF, mFaceRoundPaint);
                break;
        }
    }

    public void setCoverType(COVER_TYPE coverType) {
        mCoverType = coverType;
    }
}