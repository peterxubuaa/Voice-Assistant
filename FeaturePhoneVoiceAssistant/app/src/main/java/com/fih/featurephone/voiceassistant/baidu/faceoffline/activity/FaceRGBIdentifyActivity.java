package com.fih.featurephone.voiceassistant.baidu.faceoffline.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.camera.FaceCameraPreview;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.User;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;

public class FaceRGBIdentifyActivity extends Activity {
    FaceCameraPreview mFaceCameraPreview;
    private ImageView mDetectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_face_identification);

        initView();
    }

    private void initView() {
        mFaceCameraPreview = findViewById(R.id.face_camera_preview);
        mFaceCameraPreview.setLayoutRotate(true);
        mFaceCameraPreview.setShowUploadRGBFaceImageView(true);
        mFaceCameraPreview.setFaceRegisterListener(new FaceCameraPreview.IFaceDetectListener() {
            public boolean onDetectFace(Bitmap faceBitmap, byte[] feature, FaceInfo faceInfo) {
                return identify(feature);
            }
        });

        mDetectImage = findViewById(R.id.detect_reg_image_view);
        if (CommonUtil.isSupportMultiTouch(this)) {
            initTouchScreenView();
        }
    }

    private long mTouchUpTimeMS = 0;//防止误碰
    @SuppressLint("ClickableViewAccessibility")
    private void initTouchScreenView() {
        mDetectImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                if (!mFaceCameraPreview.isCameraPreviewing()) return true;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (System.currentTimeMillis() - mTouchUpTimeMS > 500) {
                            mFaceCameraPreview.setFaceDetect(true);
                            mDetectImage.setImageResource(R.drawable.baseline_camera_white_48dp);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mTouchUpTimeMS = System.currentTimeMillis();
                        mFaceCameraPreview.setFaceDetect(false);
                        mDetectImage.setImageResource(R.drawable.baseline_face_white_48dp);
                        break;
                }
                return true;
            }
        });

        findViewById(R.id.camera_switch).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceCameraPreview.setFaceDetect(false);
                if (mFaceCameraPreview.isCameraPreviewing()) {
                    mFaceCameraPreview.switchCameraPreview();
                } else {
                    mFaceCameraPreview.startCameraPreview();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFaceCameraPreview.startCameraPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFaceCameraPreview.stopCameraPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFaceCameraPreview.stopCameraPreview();
    }

    private boolean identify(byte[] faceFeature) {
        User user = FaceSDKManager.getInstance().searchUserByFeature(faceFeature);

        if (null != user && !TextUtils.isEmpty(user.getUserName())) {
            Intent intent = new Intent();
            intent.putExtra(GlobalValue.INTENT_FACE_USER_NAME, user.getUserName());
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView)findViewById(R.id.identify_info_text_view)).setText(R.string.baidu_face_identify_fail);
                }
            });
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mFaceCameraPreview.setFaceDetect(true);
                mDetectImage.setImageResource(R.drawable.baseline_camera_white_48dp);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mFaceCameraPreview.setFaceDetect(false);
                mDetectImage.setImageResource(R.drawable.baseline_face_white_48dp);
                break;
        }

        return super.onKeyUp(keyCode, event);
    }
}