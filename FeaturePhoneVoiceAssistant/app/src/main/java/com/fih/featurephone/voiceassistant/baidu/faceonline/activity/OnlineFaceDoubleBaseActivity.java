package com.fih.featurephone.voiceassistant.baidu.faceonline.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.camera.ImageCropActivity;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;

abstract class OnlineFaceDoubleBaseActivity extends Activity {
    private final int IMAGE_BASE_REQUEST_CODE_MASK = 0x30;
    private final int FIRST_IMAGE_BASE_REQUEST_CODE = 0x10;
    private final int SECOND_IMAGE_BASE_REQUEST_CODE = 0x20;
    private final int ALBUM_SELECT_OFFSET_REQUEST_CODE = 0x1;
    private final int ALBUM_CROP_OFFSET_REQUEST_CODE = 0x2;
    private final int CAMERA_CAPTURE_OFFSET_REQUEST_CODE = 0x4;

    String mFirstFaceFilePath;
    String mSecondFaceFilePath;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void initView(){
        initProgressDialog();
        if (CommonUtil.isSupportMultiTouch(this)) {
            initTouchScreenView();
        }
    }

    void initTouchScreenView() {
        findViewById(R.id.first_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onShowFaceInputDialog(FIRST_IMAGE_BASE_REQUEST_CODE);
                return false;
            }
        });

        findViewById(R.id.second_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onShowFaceInputDialog(SECOND_IMAGE_BASE_REQUEST_CODE);
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final String CROP_FIRST_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_first.jpg";
        final String CROP_SECOND_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_second.jpg";
        int requestBaseCode = requestCode & IMAGE_BASE_REQUEST_CODE_MASK;

        if ((requestCode & ALBUM_SELECT_OFFSET_REQUEST_CODE) > 0) {//相册选择
            if (requestBaseCode == FIRST_IMAGE_BASE_REQUEST_CODE) {
                Intent intent = new Intent(OnlineFaceDoubleBaseActivity.this, ImageCropActivity.class);
                intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_FIRST_IMAGE_FILE_PATH);
                String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                startActivityForResult(intent, requestBaseCode + ALBUM_CROP_OFFSET_REQUEST_CODE);
            } else if (requestBaseCode == SECOND_IMAGE_BASE_REQUEST_CODE) {
                Intent intent = new Intent(OnlineFaceDoubleBaseActivity.this, ImageCropActivity.class);
                intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_SECOND_IMAGE_FILE_PATH);
                String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                startActivityForResult(intent, requestBaseCode + ALBUM_CROP_OFFSET_REQUEST_CODE);
            }
        } else {
            int imageViewID = requestBaseCode == FIRST_IMAGE_BASE_REQUEST_CODE?
                    R.id.first_image_view : R.id.second_image_view;
            int textViewID = requestBaseCode == FIRST_IMAGE_BASE_REQUEST_CODE?
                    R.id.first_hint_text_view : R.id.second_hint_text_view;
            findViewById(textViewID).setVisibility(View.GONE);
            String imagePath = null;
            if ((requestCode & ALBUM_CROP_OFFSET_REQUEST_CODE) > 0) {//相片裁剪完
                imagePath = requestBaseCode == FIRST_IMAGE_BASE_REQUEST_CODE?
                        CROP_FIRST_IMAGE_FILE_PATH : CROP_SECOND_IMAGE_FILE_PATH;
            } else if ((requestCode & CAMERA_CAPTURE_OFFSET_REQUEST_CODE) > 0) {//拍照
                imagePath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
            }
            if (!FileUtils.isFileExist(imagePath)) return;

            if (requestBaseCode == FIRST_IMAGE_BASE_REQUEST_CODE) {
                mFirstFaceFilePath = imagePath;
            } else {
                mSecondFaceFilePath = imagePath;
            }
            ((ImageView)findViewById(imageViewID)).setImageBitmap(
                    BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(imagePath), BitmapUtils.getJpegImageRotateDegree(imagePath)));
        }
    }


    void onShowFaceInputDialog(final int requestCode) {
        final String CAMERA_FIRST_IMAGE_FILE_PATH = getFilesDir().getAbsolutePath() + File.separator + "camera_first_face.jpg";
        final String CAMERA_SECOND_IMAGE_FILE_PATH = getFilesDir().getAbsolutePath() + File.separator + "camera_second_face.jpg";

        final String[] items = {
                getString(R.string.baidu_face_merge_camera_capture),
                getString(R.string.baidu_face_merge_select_album),
        };

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(OnlineFaceDoubleBaseActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent intent = new Intent(OnlineFaceDoubleBaseActivity.this, CameraCaptureActivity.class);
                        if ((requestCode & IMAGE_BASE_REQUEST_CODE_MASK) == FIRST_IMAGE_BASE_REQUEST_CODE) {
                            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH, CAMERA_FIRST_IMAGE_FILE_PATH);
                        } else if ((requestCode & IMAGE_BASE_REQUEST_CODE_MASK) == SECOND_IMAGE_BASE_REQUEST_CODE) {
                            intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH, CAMERA_SECOND_IMAGE_FILE_PATH);
                        }
                        startActivityForResult(intent, requestCode + CAMERA_CAPTURE_OFFSET_REQUEST_CODE);
                        break;
                    case 1:
                        SystemUtil.startSysAlbumActivity(OnlineFaceDoubleBaseActivity.this,
                                requestCode + ALBUM_SELECT_OFFSET_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    /*在UI界面上显示信息*/
    void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

    void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
}
