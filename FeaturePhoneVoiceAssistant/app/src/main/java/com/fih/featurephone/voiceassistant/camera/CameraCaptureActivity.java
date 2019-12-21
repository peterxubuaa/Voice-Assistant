package com.fih.featurephone.voiceassistant.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;

import java.io.File;

public class CameraCaptureActivity extends Activity implements SurfaceHolder.Callback {
    public static final int OCR_TYPE = 1;
    public static final int CLASSIFY_IMAGE_TYPE = 2;
    public static final int IDENTIFY_FACE_TYPE = 3;
    public static final int DETECT_FACE_TYPE = 4;
    public static final int REGISTER_FACE_TYPE = 5;

    private int mCaptureType;
    private String mOcrLanguage;
    private int mClassifyType;
    private String mRegisterFaceInfo;
    private String mCaptureFilePath;

    private Bitmap mBmp = null;
    private boolean mHasSurface;
    private SurfaceView mSurfaceView;
    private ScannerFinderView mFinderView;
    private boolean mSupportTouch = false;
    private boolean mCapturing = false;
    private Point mCameraViewSize;
    private String mLastCameraFlashMode = Camera.Parameters.FLASH_MODE_AUTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);

        mCaptureFilePath = getIntent().getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
        if (TextUtils.isEmpty(mCaptureFilePath)) {
            mCaptureFilePath = getFilesDir().getAbsolutePath() + File.separator + "capture.jpg";
        }
        mCaptureType = getIntent().getIntExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, 0);
        mOcrLanguage = getIntent().getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE);
        mClassifyType = getIntent().getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, -1);
        mRegisterFaceInfo = getIntent().getStringExtra(GlobalValue.INTENT_REGISTER_FACE_INFO);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initCamera();
    }

    @Override
    protected void onPause() {
        try {
            CameraManager.getInstance().stopPreview();
            if (null != mSurfaceView && !mHasSurface) {
                mSurfaceView.getHolder().removeCallback(this);
            }
        } catch (Exception e) {
            // 关闭摄像头失败的情况下,最好退出该Activity,否则下次初始化的时候会显示摄像头已占用.
            finish();
        }

        if (null != mFinderView) mFinderView.setVisibility(View.GONE);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mHasSurface = false;
        if (isNotFullScreen()) {
            mCameraViewSize = CommonUtil.getDisplaySize(this);
            int statusBarHeight = CommonUtil.getStatusBarHeight(this);
            mCameraViewSize.y -= statusBarHeight;
        } else {
            mCameraViewSize = CommonUtil.getScreenSize(this);
        }

        switch (mCaptureType) {
            case IDENTIFY_FACE_TYPE:
                findViewById(R.id.face_detect_image_view).setVisibility(View.VISIBLE);
            case DETECT_FACE_TYPE:
            case OCR_TYPE:
            case CLASSIFY_IMAGE_TYPE:
                findViewById(R.id.recognize_hint).setVisibility(View.VISIBLE);
                break;
            case REGISTER_FACE_TYPE:
                findViewById(R.id.info_edit_text).setVisibility(View.VISIBLE);
                ((EditText)findViewById(R.id.info_edit_text)).setHint(mRegisterFaceInfo);
                break;
        }

        if (mSupportTouch) {
            initTouchScreenView();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTouchScreenView() {
        mFinderView = findViewById(R.id.view_finder);
        mFinderView.init(mCameraViewSize, 0, mCameraViewSize.y, false);
        mFinderView.setVisibility(View.VISIBLE);

        findViewById(R.id.camera_flash_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_flash_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCameraFLash();
            }
        });

        final ImageView recognizeImageView = findViewById(R.id.detect_reg_image_view);
        recognizeImageView.setVisibility(View.VISIBLE);
        recognizeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCapture(false, null);
            }
        });
        recognizeImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startCapture(true, null);
                return false;
            }
        });

        findViewById(R.id.camera_switch_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_switch_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        mFinderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraManager.getInstance().requestAutoFocus();
            }
        });

        findViewById(R.id.face_detect_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCapture(false, GlobalValue.EXTRA_FUN_FACE_DETECT);
            }
        });
    }

    private void startCapture(final boolean question, final String extraFun) {
        if (mCapturing) return;
        mCapturing = true;

        CameraManager.getInstance().takeShot(
                new Camera.PictureCallback() {//only support jpeg format
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (null != mBmp) mBmp.recycle();
                        mBmp = null;
                        if (null == data) return;

                        CameraManager.getInstance().stopPreview();
                        Point pictureSize = CameraManager.getInstance().getCameraPictureSize();
                        int orientation = CameraManager.getInstance().getCapturePictureRotation();
                        mBmp = BitmapUtils.getFocusedBitmap(data, pictureSize, mCameraViewSize, getCropRect(), orientation);

                        FileUtils.deleteFile(mCaptureFilePath);//删除上一次临时保存的拍照文件
                        BitmapUtils.saveBitmapToJpeg(mBmp, mCaptureFilePath);

                        Intent intent = new Intent();
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH, mCaptureFilePath);
                        intent.putExtra(GlobalValue.INTENT_UNIT_QUESTION, question);

                        if (!TextUtils.isEmpty(mOcrLanguage)) {
                            intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, mOcrLanguage);
                        }
                        if (mClassifyType > 0) {
                            intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, mClassifyType);
                        }
                        if (!TextUtils.isEmpty(mRegisterFaceInfo)) {
                            intent.putExtra(GlobalValue.INTENT_REGISTER_FACE_INFO, ((EditText)findViewById(R.id.info_edit_text)).getText().toString());
                        }
                        if (!TextUtils.isEmpty(extraFun)) intent.putExtra(GlobalValue.INTENT_EXTRA_FUN, extraFun);
                        setResult(Activity.RESULT_OK, intent);

                        finish();
                    }
                });
    }

    private void switchCamera() {
        if (CameraManager.getInstance().isPreviewRunning()) {
            CameraManager.getInstance().stopPreview();
        }

        if (CameraManager.getInstance().getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            CameraManager.getInstance().setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            CameraManager.getInstance().setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        CameraManager.getInstance().startPreview(mSurfaceView.getHolder(), mCameraViewSize.x, mCameraViewSize.y);
    }

    private void setCameraFLash() {
        if (mLastCameraFlashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)) {
            if (CameraManager.getInstance().setFlashLight(Camera.Parameters.FLASH_MODE_TORCH)) {
                ((ImageView) findViewById(R.id.camera_flash_image_view)).setImageResource(R.drawable.baseline_flash_on_white_48dp);
                mLastCameraFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
            } else if (CameraManager.getInstance().setFlashLight(Camera.Parameters.FLASH_MODE_ON)) {//增加冗余操作
                ((ImageView) findViewById(R.id.camera_flash_image_view)).setImageResource(R.drawable.baseline_flash_on_white_48dp);
                mLastCameraFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
            }
        } else if (mLastCameraFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
            if (CameraManager.getInstance().setFlashLight(Camera.Parameters.FLASH_MODE_OFF)) {
                ((ImageView) findViewById(R.id.camera_flash_image_view)).setImageResource(R.drawable.baseline_flash_off_white_48dp);
                mLastCameraFlashMode = Camera.Parameters.FLASH_MODE_OFF;
            }
        } else {
            if (CameraManager.getInstance().setFlashLight(Camera.Parameters.FLASH_MODE_AUTO)) {
                ((ImageView) findViewById(R.id.camera_flash_image_view)).setImageResource(R.drawable.baseline_flash_auto_white_48dp);
                mLastCameraFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
            }
        }
    }

    Rect getCropRect() {
        if (mSupportTouch && null != mFinderView) {
            return new Rect(mFinderView.getRect());
        }
        return new Rect(0, 0, mCameraViewSize.x, mCameraViewSize.y);
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
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.getInstance().startPreview(surfaceHolder, mCameraViewSize.x, mCameraViewSize.y);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }

        if (null != mFinderView) mFinderView.setVisibility(View.VISIBLE);
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
                startCapture(true, null);
                break;
            case KeyEvent.KEYCODE_3:
                startCapture(false, null);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
