package com.fih.featurephone.voiceassistant.baidu.faceonline.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceMerge;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

public class OnlineFaceMergeActivity extends OnlineFaceDoubleBaseActivity {
    private FaceMerge mFaceMerge;
    private String mMergeFaceFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_face_merge);
        initView();

        mFaceMerge = new FaceMerge(this, mFaceOnlineListener);
    }

    void initTouchScreenView() {
        super.initTouchScreenView();
        findViewById(R.id.merge_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onShowSaveMergeFaceDialog();
                return false;
            }
        });

        findViewById(R.id.merge_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMerge(mFirstFaceFilePath, mSecondFaceFilePath);
            }
        });
    }


    void onShowSaveMergeFaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("提示");
        builder.setMessage(getString(R.string.baidu_face_merge_save_image_file));
        builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Bitmap bitmap = BitmapFactory.decodeFile(mMergeFaceFilePath);
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "face merge", "merged face");
            }
        });
        builder.setNegativeButton(getString(R.string.button_cancel), null);
        builder.show();
    }

    private BaiduBaseAI.IBaiduBaseListener mFaceOnlineListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(String msg) {
            CommonUtil.toast(OnlineFaceMergeActivity.this, msg);
        }

        @Override
        public void onFinalResult(Object result, int resultType) {
            final String mergeFilePath = (String)result;
            if (!FileUtils.isFileExist(mergeFilePath)) {
                CommonUtil.toast(OnlineFaceMergeActivity.this, "人脸融合失败");
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        mMergeFaceFilePath = mergeFilePath;
                        ((ImageView)findViewById(R.id.merge_image_view)).setImageBitmap(
                                BitmapUtils.rotateBitmap(BitmapFactory.decodeFile(mergeFilePath), BitmapUtils.getJpegImageRotateDegree(mergeFilePath)));
                    }
                });
            }
        }
    };

    public void onMerge(final String templateFilePath, final String targetFilePath) {
        showProgressDialog(getString(R.string.baidu_unit_working));
        new Thread() {
            @Override
            public void run() {
                mFaceMerge.request(templateFilePath, targetFilePath);
            }
        }.start();
    }
}
