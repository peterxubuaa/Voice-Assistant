/*
 * Copyright (C) 2008 ZXing authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.fih.featurephone.voiceassistant.baidu.ocr.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The implementation
 * encapsulates the steps needed to take preview-sized images, which are used for both preview and decoding.
 */
public final class CameraManager {

    private static CameraManager sCameraManager;

    private final CameraConfigurationManager mConfigManager;
    /*
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to clear the handler so
     * it will only receive one message.
     */
    /** Auto-focus callbacks arrive here, and are dispatched to the Handler which requested them. */
    private final AutoFocusCallback mAutoFocusCallback;
    private Camera mCamera;
    private boolean mInitialized;
    private boolean mPreviewing;
    private boolean mUseAutoFocus;

    private CameraManager() {
        mConfigManager = new CameraConfigurationManager();
        mAutoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Initializes this static object with the Context of the calling Activity.
     */
    public static void init() {
        if (sCameraManager == null) {
            sCameraManager = new CameraManager();
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return sCameraManager;
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the mCamera will draw preview frames into.
     */
    public boolean openDriver(Context ctx, SurfaceHolder holder, int orientation) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (mCamera != null) {
                    mCamera.setPreviewDisplay(holder);

                    String currentFocusMode = mCamera.getParameters().getFocusMode();
                    mUseAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);

                    if (!mInitialized) {
                        mInitialized = true;
                        mConfigManager.initFromCameraParameters(ctx, mCamera, orientation);
                    }
                    mConfigManager.setDesiredCameraParameters(mCamera, orientation);

                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public boolean closeDriver() {
        if (mCamera != null) {
            try {
                mCamera.release();
                mInitialized = false;
                mPreviewing = false;
                mCamera = null;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
     * 打开或关闭闪光灯
     *
     * @param open 控制是否打开
     * @return 打开或关闭失败，则返回false。
     */
    boolean setFlashLight(boolean open) {
        if (mCamera == null || !mPreviewing) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) {
            // Use the screen as a flashlight (next best thing)
            return false;
        }
        String flashMode = parameters.getFlashMode();
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Asks the mCamera hardware to begin drawing preview frames to the screen.
     */
    void startPreview() {
        if (mCamera != null && !mPreviewing) {
            try {
                mCamera.startPreview();
                mPreviewing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tells the mCamera to stop drawing preview frames.
     */
    void stopPreview() {
        if (mCamera != null && mPreviewing) {
            try {
                // 停止预览时把callback移除.
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
                mAutoFocusCallback.setHandler(null, 0);
                mPreviewing = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the
     * message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively.
     */
    void requestPreviewFrame() {
//        if (mCamera != null && mPreviewing) {
//        }
    }

    /**
     * Asks the mCamera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    void requestAutoFocus(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mAutoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            if (mUseAutoFocus) {
                try {
                    mCamera.autoFocus(mAutoFocusCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void takeShot(Camera.ShutterCallback shutterCallback,
                         Camera.PictureCallback rawPictureCallback,
                         Camera.PictureCallback jpegPictureCallback ){

        mCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
    }

    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }
}
