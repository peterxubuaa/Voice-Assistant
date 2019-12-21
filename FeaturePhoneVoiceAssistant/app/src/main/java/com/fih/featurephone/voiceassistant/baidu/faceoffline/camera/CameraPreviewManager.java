package com.fih.featurephone.voiceassistant.baidu.faceoffline.camera;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;

import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.CameraDataCallback;

import java.io.IOException;
import java.util.List;

public class CameraPreviewManager implements TextureView.SurfaceTextureListener {
    private static final String TAG = "camera_preview";

    static final int CAMERA_FACING_BACK = 0;
    static final int CAMERA_FACING_FRONT = 1;
//    private static final int CAMERA_USB = 2;

    private TextureView mTextureView;
    private SurfaceTexture mSurfaceTexture;

    private int mCameraFacing = CAMERA_FACING_FRONT; //当前相机的ID
    private int mPreviewWidth;
    private int mPreviewHeight;

    private int mVideoWidth;
    private int mVideoHeight;
    private Camera mCamera;

    private CameraDataCallback mCameraDataCallback;
    @SuppressLint("StaticFieldLeak")
    private static volatile CameraPreviewManager sInstance = null;

    public static CameraPreviewManager getInstance() {
        synchronized (CameraPreviewManager.class) {
            if (null == sInstance) {
                sInstance = new CameraPreviewManager();
            }
        }
        return sInstance;
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

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        mSurfaceTexture = texture;
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        closeCamera();
        mSurfaceTexture = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }

    /**
     * 开启预览
     */
    void startPreview(TextureView textureView, int width, int height, CameraDataCallback cameraDataCallback) {
        Log.d(TAG, "startPreview");

        mTextureView = textureView;
        mPreviewWidth = width;
        mPreviewHeight = height;
        mCameraDataCallback = cameraDataCallback;

        mTextureView.setSurfaceTextureListener(this);

        if (null != mSurfaceTexture && null == mCamera) openCamera();
    }

    /**
     * 关闭预览
     */
    void stopPreview() {
        closeCamera();
        mTextureView = null;
        mCameraDataCallback = null;
    }

    /**
     * 开启摄像头
     */
    private void openCamera() {
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
            int cameraRotation = getCameraDisplayOrientation(cameraId);//90 , 270
            mCamera.setDisplayOrientation(cameraRotation);

            mTextureView.setRotationY(0);
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizeList = params.getSupportedPreviewSizes(); // 获取所有支持的camera尺寸
            final Camera.Size optionSize = getOptimalPreviewSize(sizeList, mPreviewWidth, mPreviewHeight); // 获取一个最为适配的camera.size
            if (optionSize.width == mPreviewWidth && optionSize.height == mPreviewHeight) {
                mVideoWidth = mPreviewWidth;
                mVideoHeight = mPreviewHeight;
            } else {
                mVideoWidth = optionSize.width;
                mVideoHeight = optionSize.height;
            }
            params.setPreviewSize(mVideoWidth, mVideoHeight);
            mCamera.setParameters(params);
            try {
                mCamera.setPreviewTexture(mSurfaceTexture);
                mCamera.startPreview();

                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        if (mCameraDataCallback != null) {
                            mCameraDataCallback.onGetCameraData(bytes, camera,
                                    mVideoWidth, mVideoHeight);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void closeCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewTexture(null);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Log.e(TAG, "camera destroy error");
                e.printStackTrace();
            }
        }
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

    /**
     * 解决预览变形问题
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double aspectTolerance = 0.1;
        double targetRatio = (double) width / height;
        if (sizes == null) {
            return null;
        }
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > aspectTolerance) {
                continue;
            }
            if (Math.abs(size.height - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - height);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
        return optimalSize;
    }
}
