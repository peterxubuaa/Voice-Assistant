package com.min.aiassistant.picture;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

import java.util.List;

public final class CameraManager {
    private static CameraManager sCameraManager;
    private Camera mCamera;
    private int mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Point mCameraPictureSize = new Point(0,0);
    private int mCameraRotation = 0;

    private CameraManager() {
    }

    public static CameraManager getInstance() {
        if (sCameraManager == null) {
            sCameraManager = new CameraManager();
        }
        return sCameraManager;
    }

    boolean isPreviewRunning() {
        return (null != mCamera);
    }

    int getCameraFacing() {
        return mCameraFacing;
    }

    void setCameraFacing(int cameraFacing) {
        mCameraFacing = cameraFacing;
    }

    Point getCameraPictureSize() {
        return mCameraPictureSize;
    }

    int getCapturePictureRotation() {
        if (Camera.CameraInfo.CAMERA_FACING_BACK == mCameraFacing) {
            return mCameraRotation;
        } else {
            return mCameraRotation + 180;//compensate the mirror
        }
    }

    private void openDriver(SurfaceHolder holder, int width, int height) {
        try {
            int cameraId = -1;
            if (null == mCamera) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == mCameraFacing) {
                        cameraId = i;
                        break;
                    }
                }
                if (cameraId < 0) return;

                mCamera = Camera.open(cameraId);
            }

            // 摄像头图像预览角度
            mCameraRotation = getCameraDisplayOrientation(cameraId);//90 , 270

            Camera.Parameters params = mCamera.getParameters();
            //1. 设置preview显示的旋转角度
            mCamera.setDisplayOrientation(mCameraRotation);
            //2. 设置拍照的尺寸
            Point optionPictureSize = findBestPictureSize(params, new Point(width, height), mCameraRotation);
            params.setPictureSize(optionPictureSize.x, optionPictureSize.y);
            mCameraPictureSize = optionPictureSize;
            //3. 设置preview的大小
            Point optionPreviewSize = findBestPreviewSizeValue(params, new Point(width, height), mCameraRotation); // 获取一个最为适配的camera.size
            params.setPreviewSize(optionPreviewSize.x, optionPreviewSize.y);
            //重新调整preview size, 有可能不支持
            mCamera.setParameters(params);
            Camera.Parameters afterParameters = mCamera.getParameters();
            Camera.Size afterSize = afterParameters.getPreviewSize();
            if (null != afterSize && (afterSize.width != optionPreviewSize.x || afterSize.height != optionPreviewSize.y)) {
                optionPreviewSize.x = afterSize.width;
                optionPreviewSize.y = afterSize.height;
                params.setPreviewSize(optionPreviewSize.x, optionPreviewSize.y);//重新设置
            }
            //4.设置flash
            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(params);//设置camera参数

            mCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDriver() {
        if (mCamera != null) {
            try {
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void setZoom(boolean zoomIn) {
        Camera.Parameters params = mCamera.getParameters();
        final int MAX_ZOOM = params.getMaxZoom();
        int zoomValue = params.getZoom();
        int newZoomValue = zoomIn? ++zoomValue : --zoomValue;
        if (newZoomValue < 0 || newZoomValue > MAX_ZOOM) {
            return;
        }

        params.setZoom(newZoomValue);
        mCamera.setParameters(params);
    }

    boolean setFlashLight(String flashMode) { //FLASH_MODE_OFF, FLASH_MODE_AUTO, FLASH_MODE_ON, FLASH_MODE_TORCH
        if (!isPreviewRunning()) return false;

        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) return false;

        String curFlashMode = parameters.getFlashMode();
        if (curFlashMode.equals(flashMode)) return true;

        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) return false;
        if (!flashModes.contains(flashMode)) return false;

        parameters.setFlashMode(flashMode);
        mCamera.setParameters(parameters);
        return true;
    }

    void startPreview(SurfaceHolder holder, int width, int height) {
        if (null == mCamera) {
            openDriver(holder, width, height);
        }

        if (mCamera != null) {
            try {
                mCamera.startPreview();
                if (null != mAutoFocusCallback) {
                    mCamera.autoFocus(mAutoFocusCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void stopPreview() {
        if (mCamera != null) {
            try {
                // 停止预览时把callback移除.
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
                if (null != mAutoFocusCallback) {
                    mAutoFocusHandler.removeCallbacksAndMessages(null);
                    mCamera.cancelAutoFocus();
                }
                closeDriver();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void takeShot(Camera.PictureCallback jpegPictureCallback){
        //shutter == null, //设置为空关闭拍照提示音
        // raw callback camera不支持
        mCamera.takePicture(null, null, jpegPictureCallback);
    }

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = info.orientation % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation + 360) % 360;
        }
        return rotation;
    }

    private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point hopePreviewSize, int orientation) {
        /* 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point pendingSize;
        if (90 == orientation || 270 == orientation) {
            pendingSize = new Point(hopePreviewSize.y, hopePreviewSize.x);
        } else {
            pendingSize = hopePreviewSize;
        }

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        Point largestSize = findBestSizeValue(rawSupportedSizes, pendingSize);
        if (null != largestSize) return largestSize;

        // If there is nothing at all suitable, return current preview size
        Camera.Size defaultPreview = parameters.getPreviewSize();
        if (defaultPreview == null) {
            throw new IllegalStateException("Parameters contained no preview size!");
        }
        return new Point(defaultPreview.width, defaultPreview.height);
    }

    private Point findBestPictureSize(Camera.Parameters parameters, Point hopePictureSize, int orientation) {

        /* 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
        Point pendingSize;
        if (90 == orientation || 270 == orientation) {
            pendingSize = new Point(hopePictureSize.y, hopePictureSize.x);
        } else {
            pendingSize = hopePictureSize;
        }

        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPictureSizes();
        Point largestSize = findBestSizeValue(rawSupportedSizes, pendingSize);
        if (null != largestSize) return largestSize;

        Camera.Size defaultPicture = parameters.getPictureSize();
        if (defaultPicture == null) {
            throw new IllegalStateException("Parameters contained no preview size!");
        }
        return new Point(defaultPicture.width, defaultPicture.height);
    }

    private Point findBestSizeValue(List<Camera.Size> rawSupportedSizes, Point sizeValue) {
        final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen

        if (null == rawSupportedSizes) return null;

        // Find a suitable size, with max resolution
        Camera.Size maxResPreviewSize = null;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size size : rawSupportedSizes) {
            int realWidth = size.width;
            int realHeight = size.height;
            int resolution = realWidth * realHeight;
            if (resolution < MIN_PREVIEW_PIXELS) {
                continue;
            }

            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;

            if (maybeFlippedWidth == sizeValue.x && maybeFlippedHeight == sizeValue.y) {
                return new Point(realWidth, realHeight);
            }

            int newDiff = Math.abs(maybeFlippedWidth - sizeValue.x) + Math.abs(maybeFlippedHeight - sizeValue.y);
            if (newDiff < diff) {
                maxResPreviewSize = size;
                diff = newDiff;
            }
        }

        // If no exact match, use largest preview size. This was not a great idea on older devices because
        // of the additional computation needed. We're likely to get here on newer Android 4+ devices, where
        // the CPU is much more powerful.
        if (maxResPreviewSize != null) {
            return new Point(maxResPreviewSize.width, maxResPreviewSize.height);
        }

        return null;
    }

    void requestAutoFocus() {
        if (isPreviewRunning()) {
            mAutoFocusHandler.sendEmptyMessage(AUTO_FOCUS_MSG);
        }
    }

    private final int AUTO_FOCUS_MSG = 100;
    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        private final long AUTO_FOCUS_INTERVAL_MS = 1300L; //自动对焦时间
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (!success) {
                mAutoFocusHandler.sendEmptyMessageDelayed(AUTO_FOCUS_MSG, AUTO_FOCUS_INTERVAL_MS);
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mAutoFocusHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == AUTO_FOCUS_MSG) {
                mCamera.autoFocus(mAutoFocusCallback);
            }
        }
    };
}
