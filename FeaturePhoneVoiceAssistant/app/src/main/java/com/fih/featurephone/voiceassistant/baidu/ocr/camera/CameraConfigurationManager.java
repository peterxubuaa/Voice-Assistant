/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fih.featurephone.voiceassistant.baidu.ocr.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置相机的参数信息，获取最佳的预览界面
 */
final class CameraConfigurationManager {

	private static final String TAG = "CameraConfiguration";
	// 相机分辨率
	private static Point sCameraResolution;
	// 拍照分辨率
	private Point mPictureResolution;

	void initFromCameraParameters(Context ctx, Camera camera, int orientation) {
		// 需要判断摄像头是否支持缩放
		Camera.Parameters parameters = camera.getParameters();
		if (parameters.isZoomSupported()) {
			// 设置成最大倍数的1/10，基本符合远近需求
			parameters.setZoom(parameters.getMaxZoom() / 10);
		}
		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			Rect focusRect = new Rect(-900, -900, 900, 900);
			focusAreas.add(new Camera.Area(focusRect, 1000));
			parameters.setFocusAreas(focusAreas);
		}

		// 屏幕分辨率
		Point screenResolution = CommonUtil.getDisplaySize(ctx);
		Log.i(TAG, "Screen resolution: " + screenResolution);

		sCameraResolution = findBestPreviewSizeValue(parameters, screenResolution, orientation);
		Log.i(TAG, "Camera resolution x: " + sCameraResolution.x);
		Log.i(TAG, "Camera resolution y: " + sCameraResolution.y);

		mPictureResolution = findBestPictureSize(parameters, screenResolution, orientation);
		Log.i(TAG, "Camera picture x: " + mPictureResolution.x);
		Log.i(TAG, "Camera picture y: " + mPictureResolution.y);
	}

	void setDesiredCameraParameters(Camera camera, int orientation) {
		Camera.Parameters parameters = camera.getParameters();

		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}

		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());

		parameters.setPreviewSize(sCameraResolution.x, sCameraResolution.y);
		parameters.setPictureSize(mPictureResolution.x, mPictureResolution.y);
		camera.setParameters(parameters);

		Camera.Parameters afterParameters = camera.getParameters();
		Camera.Size afterSize = afterParameters.getPreviewSize();
		if (afterSize != null && (sCameraResolution.x != afterSize.width || sCameraResolution.y != afterSize.height)) {
			Log.w(TAG, "Camera said it supported preview size " + sCameraResolution.x + 'x' + sCameraResolution.y + ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
			sCameraResolution.x = afterSize.width;
			sCameraResolution.y = afterSize.height;
		}

//		int orient = getDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK);
		camera.setDisplayOrientation(orientation);
		/* 设置相机预览为竖屏 */
//		camera.setDisplayOrientation(90);
		/* 设置相机预览为横屏 */
//		camera.setDisplayOrientation(0);
	}

	static Point getCameraResolution() {
		return sCameraResolution;
	}

/*	private int getDisplayOrientation(int cameraId) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation + 360) % 360;
		}

		return result;
	}*/

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
				Point exactPoint = new Point(realWidth, realHeight);
				Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
				return exactPoint;
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
			Point largestSize = new Point(maxResPreviewSize.width, maxResPreviewSize.height);
			Log.i(TAG, "Using largest suitable preview size: " + largestSize);
			return largestSize;
		}

		return null;
	}

	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution, int orientation) {
		/* 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
		Point screenSize;
		if (90 == orientation || 270 == orientation) {
			screenSize = new Point(screenResolution.y, screenResolution.x);
		} else {
			screenSize = new Point(screenResolution);
		}

		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		Point largestSize = findBestSizeValue(rawSupportedSizes, screenSize);
		if (null != largestSize) return largestSize;

		// If there is nothing at all suitable, return current preview size
		Camera.Size defaultPreview = parameters.getPreviewSize();
		if (defaultPreview == null) {
			throw new IllegalStateException("Parameters contained no preview size!");
		}
		Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
		Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
		return defaultSize;
	}

	private Point findBestPictureSize(Camera.Parameters parameters, Point screenResolution, int orientation) {

		/* 因为换成了竖屏显示，所以不替换屏幕宽高得出的预览图是变形的 */
		Point screenSize;
		if (90 == orientation || 270 == orientation) {
			screenSize = new Point(screenResolution.y, screenResolution.x);
		} else {
			screenSize = new Point(screenResolution);
		}

		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPictureSizes();
		Point largestSize = findBestSizeValue(rawSupportedSizes, screenSize);
		if (null != largestSize) return largestSize;

		Camera.Size defaultPicture = parameters.getPictureSize();
		if (defaultPicture == null) {
			throw new IllegalStateException("Parameters contained no preview size!");
		}
		Point defaultSize = new Point(defaultPicture.width, defaultPicture.height);
		Log.i(TAG, "No suitable preview sizes, using default: " + defaultSize);
		return defaultSize;
	}
}
