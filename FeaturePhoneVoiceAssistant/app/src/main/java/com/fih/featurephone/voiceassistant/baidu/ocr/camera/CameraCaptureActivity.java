package com.fih.featurephone.voiceassistant.baidu.ocr.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraCaptureActivity extends Activity implements SurfaceHolder.Callback {
    private boolean mFlash;
    private String mOcrLanguage;
    private Bitmap mBmp = null;
    private CaptureActivityHandler mCaptureActivityHandler;
    private int mOrientation = 0;
    private boolean mHasSurface;
    private SurfaceView mSurfaceView;
    private ScannerFinderView mFinderView;
    boolean mSupportTouch = false;
    boolean mCapturing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_scanner);

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);
        mFlash = getIntent().getBooleanExtra("OCR_CAMERA_FLASH", false);
        mOcrLanguage = getIntent().getStringExtra("OCR_LANGUAGE");

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        CameraManager.init();
        initCamera();
    }

    @Override
    protected void onPause() {
        if (mCaptureActivityHandler != null) {
            try {
                mCaptureActivityHandler.quitSynchronously();
                mCaptureActivityHandler = null;
                if (null != mSurfaceView && !mHasSurface) {
                    mSurfaceView.getHolder().removeCallback(this);
                }
                if (!CameraManager.get().closeDriver()) {
                    finish();
                }
            } catch (Exception e) {
                // 关闭摄像头失败的情况下,最好退出该Activity,否则下次初始化的时候会显示摄像头已占用.
                finish();
            }
        }

        if (null != mFinderView) mFinderView.setVisibility(View.GONE);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        //获取当前Activity的屏幕方向
        int orientation = getRequestedOrientation();//????
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mOrientation = 90;
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mOrientation = 0;
        } else {
            mOrientation = mSupportTouch? 90 : 270;
        }

        if (mSupportTouch) {
            mFinderView = findViewById(R.id.view_finder);
            findViewById(R.id.recognize_hint).setVisibility(View.VISIBLE);
            final Button recognizeButton = findViewById(R.id.recognize_bt);
            recognizeButton.setVisibility(View.VISIBLE);
            recognizeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recognizeButton.setEnabled(false);
                    startBaiduOCR(false);
                }
            });
            recognizeButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    recognizeButton.setEnabled(false);
                    startBaiduOCR(true);
                    return false;
                }
            });
        }

        mHasSurface = false;
    }

    private void startBaiduOCR(final boolean question) {
        if (mCapturing) return;
        mCapturing = true;

        CameraManager.get().takeShot(null, //设置为空关闭拍照提示音
                null,
                new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (null != mBmp) mBmp.recycle();
                        mBmp = null;
                        if (null == data) return;

                        mCaptureActivityHandler.onPause();
                        Point ScrRes = CommonUtil.getDisplaySize(CameraCaptureActivity.this);
                        mBmp = BmpTools.getFocusedBitmap(data, ScrRes, getCropRect(), mOrientation);

                        try {
                            Bitmap newBmp = mBmp.copy(mBmp.getConfig(), false);
                            File outputFile = new File(getFilesDir(), "pic.jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                            newBmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                            newBmp.recycle();
                            fileOutputStream.close();

                            Intent intent = new Intent();
                            intent.putExtra("OCR_FILEPATH", outputFile.getAbsolutePath());
                            intent.putExtra("OCR_QUESTION", question);
                            intent.putExtra("OCR_LANGUAGE", mOcrLanguage);
                            setResult(Activity.RESULT_OK, intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        finish();
                    }
                });
    }

    Rect getCropRect() {
        if (mSupportTouch && null != mFinderView) {
            return new Rect(mFinderView.getRect());
        }

        Point pt = CommonUtil.getDisplaySize(this);
        return new Rect(0, 0, pt.x, pt.y);
    }

    private boolean isNotFullScreen() {
        int uiFlags = getWindow().getDecorView().getSystemUiVisibility();
        return ((uiFlags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0);
    }

    private void initCamera() {
        if (null == mSurfaceView) {
            ViewStub viewStub = findViewById(R.id.view_stub);
            viewStub.setLayoutResource(R.layout.layout_surface_view);
            mSurfaceView = (SurfaceView) viewStub.inflate();
        }

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        if (mHasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }

        if (mSupportTouch) {
            int recognizeBtnHeight = findViewById(R.id.recognize_bt).isShown() ? findViewById(R.id.recognize_bt).getHeight() : 0;
            int statusBarHeight = 0;
            Point screenResolution;
            if (isNotFullScreen()) {
                screenResolution = CommonUtil.getDisplaySize(this);
                statusBarHeight = CommonUtil.getStatusBarHeight(this);
            } else {
                screenResolution = CommonUtil.getScreenSize(this);
            }

            if (null != mFinderView) {
                mFinderView.init(screenResolution, 0, screenResolution.y - (statusBarHeight + recognizeBtnHeight),
                        false);
            }
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            if (!CameraManager.get().openDriver(this, surfaceHolder, mOrientation)) {
                finish();
                return;
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        if (null != mFinderView) mFinderView.setVisibility(View.VISIBLE);
        if (mCaptureActivityHandler == null) {
            mCaptureActivityHandler = new CaptureActivityHandler(mFlash);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHasSurface) {
            mHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHasSurface = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                startBaiduOCR(true);
                break;
            case KeyEvent.KEYCODE_3:
                startBaiduOCR(false);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
