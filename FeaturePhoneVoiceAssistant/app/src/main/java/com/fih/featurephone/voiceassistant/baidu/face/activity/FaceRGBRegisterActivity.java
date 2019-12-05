package com.fih.featurephone.voiceassistant.baidu.face.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.face.camera.FaceCameraPreview;
import com.fih.featurephone.voiceassistant.baidu.face.db.DBManager;
import com.fih.featurephone.voiceassistant.baidu.face.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CnToSpell;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class FaceRGBRegisterActivity extends Activity {
    public static final String REGISTER_USER_ID_LIST = "REGISTER_USER_ID_LIST";
    FaceCameraPreview mFaceCameraPreview;
    private TextView mDetectText;
    private ImageView mDetectImage;
    private String mUserID = null;
    private ArrayList<String> mRegisterUserIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register);

        initView();
    }

    private void initView() {
        mFaceCameraPreview = findViewById(R.id.face_camera_preview);
        mFaceCameraPreview.setLayoutRotate(true);
        mFaceCameraPreview.setShowUploadRGBFaceImageView(true);
        mFaceCameraPreview.setFaceRegisterListener(new FaceCameraPreview.IFaceDetectListener() {
            public boolean onDetectFace(Bitmap faceBitmap, byte[] feature, FaceInfo faceInfo) {
                return register(faceBitmap, feature);
            }
        });

        mDetectText = findViewById(R.id.detect_reg_text);
        mDetectImage = findViewById(R.id.detect_reg_image_item);

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
                            displayTip(getString(R.string.baidu_face_register_start),
                                    BitmapFactory.decodeResource(getResources(), R.drawable.baseline_camera_white_48dp));
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mTouchUpTimeMS = System.currentTimeMillis();
                        mFaceCameraPreview.setFaceDetect(false);
                        displayTip(getString(R.string.baidu_face_register_end),
                                BitmapFactory.decodeResource(getResources(),R.drawable.baseline_face_white_48dp));
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
                    ((ImageView)findViewById(R.id.camera_switch)).setImageResource(R.drawable.baseline_flip_camera_ios_white_48dp);
                    displayTip(getString(R.string.baidu_face_register_end),
                            BitmapFactory.decodeResource(getResources(),R.drawable.baseline_face_white_48dp));
                }

                if (findViewById(R.id.face_register_delete).getVisibility() == View.VISIBLE) {
                    findViewById(R.id.face_register_delete).setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.face_register_delete).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {//注册成功才会显示该按钮
                String msg;
                if (DBManager.getInstance().deleteUserByUserID(mUserID)) {
                    msg = getString(R.string.baidu_face_register_delete_success);
                    ((EditText)findViewById(R.id.name_edit_text)).setText("");
                    mRegisterUserIDList.remove(mUserID);
                } else {
                    msg = getString(R.string.baidu_face_register_delete_fail);
                }

                displayTip(msg, BitmapFactory.decodeResource(getResources(),R.drawable.baseline_face_white_48dp));
                findViewById(R.id.face_register_delete).setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        if (mRegisterUserIDList.size() > 0) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra(REGISTER_USER_ID_LIST, mRegisterUserIDList);
            setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    /**
     * 注册到人脸库
     */
    private boolean register(final Bitmap faceBitmap, byte[] faceFeature) {
        String username = ((EditText)findViewById(R.id.name_edit_text)).getText().toString();
        if (TextUtils.isEmpty(username)) {
            displayTip(getString(R.string.baidu_face_register_info_lack));
            return false;
        }

        String faceImagePath = getImageFilePath(username);
        // 注册到人脸库
        mUserID = UUID.randomUUID().toString();
        boolean result = FaceSDKManager.getInstance().registerUserIntoDBManager(
                mUserID, username, null, faceImagePath, faceFeature);

        if (result) {
            mFaceCameraPreview.setFaceDetect(false);
            runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      findViewById(R.id.face_register_delete).setVisibility(View.VISIBLE);
                      ((ImageView)findViewById(R.id.camera_switch)).setImageResource(R.drawable.baseline_photo_camera_white_48dp);
                  }
            });
            displayTip(String.format(getString(R.string.baidu_face_register_success), username), faceBitmap);

            // 压缩、保存人脸图片至300 * 300
            File file = new File(faceImagePath);
            BitmapUtils.resize(faceBitmap, file, 300, 300);

            mRegisterUserIDList.add(mUserID);
        } else {
            mUserID = null;
            displayTip(getString(R.string.baidu_face_register_fail));
        }

        return result;
    }

    private String getImageFilePath(String userName) {
        String faceDirPath = FileUtils.getFaceImageDirectory().getAbsolutePath();
        String pinyinUserName = CnToSpell.getInstance().getSelling(userName);

        for (int i = 0; i < 1000; i++) {
            String filePath = faceDirPath + File.separator + pinyinUserName + "-" + i + ".jpg";
            if (!FileUtils.isFileExist(filePath)) return filePath;
        }
        return null;
    }

    private void displayTip(String msg) {
        displayTip(msg, null);
    }

    private void displayTip(final String msg, final Bitmap imageBitmap) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            mDetectText.setText(msg);
            if (null != imageBitmap) mDetectImage.setImageBitmap(imageBitmap);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDetectText.setText(msg);
                    if (null != imageBitmap) mDetectImage.setImageBitmap(imageBitmap);
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                mFaceCameraPreview.setFaceDetect(true);
                displayTip(getString(R.string.baidu_face_register_start),
                        BitmapFactory.decodeResource(getResources(), R.drawable.baseline_camera_white_48dp));
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
                displayTip(getString(R.string.baidu_face_register_end),
                        BitmapFactory.decodeResource(getResources(),R.drawable.baseline_face_white_48dp));
                break;
        }

        return super.onKeyUp(keyCode, event);
    }
}
