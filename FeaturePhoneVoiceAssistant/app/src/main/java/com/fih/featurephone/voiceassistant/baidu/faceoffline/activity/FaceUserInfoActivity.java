package com.fih.featurephone.voiceassistant.baidu.faceoffline.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.camera.FaceCameraPreview;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.listener.UserInfoUpdateListener;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.User;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;

public class FaceUserInfoActivity extends Activity {
    public static final String DB_ID = "DB_ID";
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_INFO = "USER_INFO";
    public static final String CREATE_TIME = "CREATE_TIME";
    public static final String FACE_IMAGE_PATH = "FACE_IMAGE_PATH";
    private final int CROP_IMAGE_REQUEST_CODE = 100;
    private final int IMAGE_SELECT_REQUEST_CODE = 101;

    FaceCameraPreview mFaceCameraPreview;
    private User mUser;
    private boolean mUpdateUerImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_face_user_info);

        mUser = new User();
        mUser.setDBID(getIntent().getIntExtra(DB_ID, 0));
        mUser.setUserID(getIntent().getStringExtra(USER_ID));
        mUser.setUserName(getIntent().getStringExtra(USER_NAME));
        mUser.setUserInfo(getIntent().getStringExtra(USER_INFO));
        mUser.setCreateTime(getIntent().getLongExtra(CREATE_TIME, 0));
        mUser.setFaceImagePath(getIntent().getStringExtra(FACE_IMAGE_PATH));

        initView();
    }

    private void initView() {
        ((EditText)findViewById(R.id.user_name_edit_text)).setText(mUser.getUserName());
        ((EditText)findViewById(R.id.user_info_edit_text)).setText(mUser.getUserInfo());
        ((EditText)findViewById(R.id.user_register_time_edit_text))
                .setText(CommonUtil.formatTime(mUser.getCreateTime(), "yyyy.MM.dd HH:mm:ss"));

        if (FileUtils.isFileExist(mUser.getFaceImagePath())) {
            Bitmap bitmap = BitmapFactory.decodeFile(mUser.getFaceImagePath());
            ((ImageView)findViewById(R.id.user_image_image_view)).setImageBitmap(bitmap);
        }

        if (CommonUtil.isSupportMultiTouch(this)) {
            initTouchScreenView();
        }

        //人脸拍照
        mFaceCameraPreview = findViewById(R.id.face_camera_preview);
        mFaceCameraPreview.setFaceRegisterListener(new FaceCameraPreview.IFaceDetectListener() {
            public boolean onDetectFace(final Bitmap faceBitmap, byte[] feature, FaceInfo faceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFaceCameraPreview.setFaceDetect(false);
                        stopCameraPreview();
                        mUpdateUerImage = true;
                        ((ImageView) findViewById(R.id.user_image_image_view))
                                .setImageBitmap(faceBitmap);
                    }
                });
                return true;
            }
        });
    }

    private void initTouchScreenView() {
        findViewById(R.id.user_image_image_view).setOnLongClickListener(new AdapterView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onShowUpdateImageDialog();
                return true;
            }
        });

        findViewById(R.id.update_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });

        findViewById(R.id.camera_close_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCameraPreview();
            }
        });

        findViewById(R.id.capture_image_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mFaceCameraPreview.setFaceDetect(true);
                        ((ImageView)findViewById(R.id.capture_image_view)).setImageResource(R.drawable.baseline_camera_white_48dp);
                        break;
                    case MotionEvent.ACTION_UP:
                        mFaceCameraPreview.setFaceDetect(false);
                        ((ImageView)findViewById(R.id.capture_image_view)).setImageResource(R.drawable.baseline_face_white_48dp);
                        break;
                }
                return true;
            }
        });

        findViewById(R.id.camera_switch_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFaceCameraPreview.switchCameraPreview();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final String CROP_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_user_info.jpg";
        switch (requestCode) {
            case IMAGE_SELECT_REQUEST_CODE:
                SystemUtil.cropSelectImage(this, data.getData(), CROP_IMAGE_REQUEST_CODE, CROP_IMAGE_FILE_PATH);
                break;
            case CROP_IMAGE_REQUEST_CODE:
                if (FileUtils.isFileExist(CROP_IMAGE_FILE_PATH)) {
                    mUpdateUerImage = true;
                    int rotateDegree = BitmapUtils.getJpegImageRotateDegree(CROP_IMAGE_FILE_PATH);
                    ((ImageView) findViewById(R.id.user_image_image_view))
                            .setImageBitmap(BitmapUtils.rotateBitmap(rotateDegree, BitmapFactory.decodeFile(CROP_IMAGE_FILE_PATH)));
                }
                break;
        }
    }

    private void onShowUpdateImageDialog() {
        final String[] items = {
                getString(R.string.baidu_face_user_info_camera),
                getString(R.string.baidu_face_user_info_image),
        };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(FaceUserInfoActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://camera
                        startCameraPreview();
                        break;
                    case 1://image
                        SystemUtil.startSysAlbumActivity(FaceUserInfoActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                    default:
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void updateUserInfo() {
        boolean update = false;
        String userName = ((EditText)findViewById(R.id.user_name_edit_text)).getText().toString();
        if (!TextUtils.isEmpty(userName) && !userName.equals(mUser.getUserName())) {
            mUser.setUserName(userName);
            update = true;
        }

        String userInfo = ((EditText)findViewById(R.id.user_info_edit_text)).getText().toString();
        if (!TextUtils.isEmpty(userInfo) && !userInfo.equals(mUser.getUserInfo())) {
            mUser.setUserInfo(userInfo);
            update = true;
        }

        if (mUpdateUerImage) {
            ImageView userImage = findViewById(R.id.user_image_image_view);
            userImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(userImage.getDrawingCache());
            userImage.setDrawingCacheEnabled(false);
            FaceSDKManager.getInstance().updateImageIntoDBManager(bitmap, mUser, mUserInfoUpdateListener);
            mUpdateUerImage = false;
        } else if (update) {
            FaceSDKManager.getInstance().updateUserIntoDBManager(mUser, mUserInfoUpdateListener);
        } else {
            CommonUtil.toast(FaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_ignore));
        }
    }

    UserInfoUpdateListener mUserInfoUpdateListener = new UserInfoUpdateListener() {
        @Override
        public void updateImageSuccess(User user, Bitmap bitmap) {
            CommonUtil.toast(FaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_image_success));
            Intent intent = new Intent();
            intent.putExtra(DB_ID, mUser.getDBID());
            intent.putExtra(FACE_IMAGE_PATH, mUser.getFaceImagePath());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void updateImageFailure(User user, String message) {
            CommonUtil.toast(FaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_image_fail) + message);
        }

        @Override
        public void userUpdateSuccess(User user) {
            CommonUtil.toast(FaceUserInfoActivity.this, getString((R.string.baidu_face_user_info_update_success)));
            Intent intent = new Intent();
            intent.putExtra(DB_ID, mUser.getDBID());
            intent.putExtra(USER_INFO, mUser.getUserInfo());
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        @Override
        public void userUpdateFailure(User user, String message) {
            CommonUtil.toast(FaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_fail) + message);
        }
    };

    // 摄像头图像预览
    private void startCameraPreview() {
        findViewById(R.id.user_image_image_view).setVisibility(View.GONE);
        findViewById(R.id.user_image_text_view).setVisibility(View.GONE);
        findViewById(R.id.update_image_view).setVisibility(View.GONE);

        findViewById(R.id.camera_close_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.capture_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.camera_switch_image_view).setVisibility(View.VISIBLE);
        mFaceCameraPreview.setVisibility(View.VISIBLE);

        mFaceCameraPreview.startCameraPreview();
    }

    private void stopCameraPreview() {
        mFaceCameraPreview.stopCameraPreview();

        findViewById(R.id.camera_close_image_view).setVisibility(View.GONE);
        findViewById(R.id.capture_image_view).setVisibility(View.GONE);
        findViewById(R.id.camera_switch_image_view).setVisibility(View.GONE);
        mFaceCameraPreview.setVisibility(View.GONE);

        findViewById(R.id.user_image_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.user_image_text_view).setVisibility(View.VISIBLE);
        findViewById(R.id.update_image_view).setVisibility(View.VISIBLE);
    }
}
