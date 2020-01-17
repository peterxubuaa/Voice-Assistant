package com.min.aiassistant.baidu.faceonline.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.min.aiassistant.baidu.faceonline.model.FaceDBOperate;
import com.min.aiassistant.picture.CameraCaptureActivity;
import com.min.aiassistant.picture.ImageCropActivity;
import com.min.aiassistant.utils.BitmapUtils;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.GlobalValue;
import com.min.aiassistant.utils.SystemUtil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OnlineFaceUserInfoActivity extends Activity {
    private final int IMAGE_SELECT_REQUEST_CODE = 101;
    private final int CAMERA_CAPTURE_REQUEST_CODE = 102;

    private UserItem mUserItem;
    private boolean mUpdateUerImage = false;
    private FaceDBOperate mFaceDBOperate;
    private ExecutorService mFaceExecutorService;
    private Future mFaceTaskFuture;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_face_user_info);

        mFaceExecutorService = Executors.newSingleThreadExecutor();

        mUserItem = new UserItem();
        mUserItem.setUserID(getIntent().getStringExtra(GlobalValue.INTENT_USER_ID));
        mUserItem.setUserInfo(getIntent().getStringExtra(GlobalValue.INTENT_USER_INFO));
        mUserItem.setFaceLocalImagePath(getIntent().getStringExtra(GlobalValue.INTENT_FACE_IMAGE_PATH));
        mUserItem.setFaceToken(getIntent().getStringExtra(GlobalValue.INTENT_FACE_TOKEN));

        mFaceDBOperate = new FaceDBOperate(this, mFaceOnlineListener);

        initProgressDialog();
        initView();
    }

    private void initView() {
        ((EditText)findViewById(R.id.user_info_edit_text)).setText(mUserItem.getUserInfo());

        if (FileUtils.isFileExist(mUserItem.getFaceLocalImagePath())) {
            Bitmap bitmap = BitmapFactory.decodeFile(mUserItem.getFaceLocalImagePath());
            ((ImageView)findViewById(R.id.user_image_image_view)).setImageBitmap(bitmap);
        }

        initTouchScreenView();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final int CROP_IMAGE_REQUEST_CODE = 100;
        final String CROP_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_user_info.jpg";

        switch (requestCode) {
            case IMAGE_SELECT_REQUEST_CODE:
                Intent intent = new Intent(OnlineFaceUserInfoActivity.this, ImageCropActivity.class);
                intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH);
                String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                startActivityForResult(intent, CROP_IMAGE_REQUEST_CODE);
                break;
            case CROP_IMAGE_REQUEST_CODE:
                if (FileUtils.isFileExist(CROP_IMAGE_FILE_PATH)) {
                    mUpdateUerImage = true;
                    int rotateDegree = BitmapUtils.getJpegImageRotateDegree(CROP_IMAGE_FILE_PATH);
                    ((ImageView) findViewById(R.id.user_image_image_view))
                            .setImageBitmap(BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(CROP_IMAGE_FILE_PATH), rotateDegree));
                }
                break;
            case CAMERA_CAPTURE_REQUEST_CODE:
                final String cameraImagePath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
                if (FileUtils.isFileExist(cameraImagePath)) {
                    mUpdateUerImage = true;
                    ((ImageView) findViewById(R.id.user_image_image_view))
                            .setImageBitmap(BitmapFactory.decodeFile(cameraImagePath));
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
                new AlertDialog.Builder(OnlineFaceUserInfoActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://camera
                        Intent intent = new Intent(OnlineFaceUserInfoActivity.this, CameraCaptureActivity.class);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                                getFilesDir().getAbsolutePath() + File.separator + "camera_face_update.jpg");
                        startActivityForResult(intent, CAMERA_CAPTURE_REQUEST_CODE);
                        break;
                    case 1://image
                        SystemUtil.startSysAlbumActivity(OnlineFaceUserInfoActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                    default:
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void updateUserInfo() {
        if (null != mFaceTaskFuture && !mFaceTaskFuture.isDone()) return;

        boolean update = false;
        final String userInfo = ((EditText)findViewById(R.id.user_info_edit_text)).getText().toString();
        if (!TextUtils.isEmpty(userInfo) && !userInfo.equals(mUserItem.getUserInfo())) {
            update = true;
        }

        if (mUpdateUerImage) {
            showProgressDialog("正在更新人脸图片");
            ImageView userImage = findViewById(R.id.user_image_image_view);
            userImage.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(userImage.getDrawingCache());
            userImage.setDrawingCacheEnabled(false);
            final String SAVE_UPDATE_IMAGE_FILE_PATH = getFilesDir().getAbsolutePath() + File.separator + "update.jpg";
            BitmapUtils.saveBitmapToJpeg(bitmap, SAVE_UPDATE_IMAGE_FILE_PATH);
            mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    mFaceDBOperate.requestFaceUpdate(mUserItem,
                            userInfo, SAVE_UPDATE_IMAGE_FILE_PATH);
                }
            });
            mUpdateUerImage = false;
        } else if (update) {
            showProgressDialog("正在更新人脸信息");
            mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    mFaceDBOperate.requestFaceUpdate(mUserItem, userInfo, null);
                }
            });
        } else {
            CommonUtil.toast(OnlineFaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_ignore));
        }
    }

    private BaiduBaseAI.IBaiduBaseListener mFaceOnlineListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(String msg) {
            hideProgressDialog();
            CommonUtil.toast(OnlineFaceUserInfoActivity.this, msg);
        }

        @Override
        public void onFinalResult(Object result, int resultType) {
            hideProgressDialog();
            if (BaiduFaceOnlineAI.FACE_UPDATE_ACTION == resultType) {
                CommonUtil.toast(OnlineFaceUserInfoActivity.this, getString(R.string.baidu_face_user_info_update_image_success));
                UserItem userItem = (UserItem)result;
                Intent intent = new Intent();
                intent.putExtra(GlobalValue.INTENT_USER_ID, mUserItem.getUserID());
                if (!TextUtils.isEmpty(userItem.getUserInfo())) {
                    intent.putExtra(GlobalValue.INTENT_USER_INFO, mUserItem.getUserInfo());
                }
                if (!TextUtils.isEmpty(mUserItem.getFaceLocalImagePath())) {
                    intent.putExtra(GlobalValue.INTENT_FACE_IMAGE_PATH, mUserItem.getFaceLocalImagePath());
                }
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };

    /*在UI界面上显示信息*/
    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

    private void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
}
