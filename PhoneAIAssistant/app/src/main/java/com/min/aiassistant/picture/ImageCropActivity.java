package com.min.aiassistant.picture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;

import com.min.aiassistant.R;
import com.min.aiassistant.utils.BitmapUtils;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.GlobalValue;

import java.io.File;

public class ImageCropActivity extends Activity implements ViewTreeObserver.OnGlobalLayoutListener {
    public static final int OCR_TYPE = 1;
    public static final int CLASSIFY_IMAGE_TYPE = 2;
    public static final int IDENTIFY_FACE_TYPE = 3;
    public static final int DETECT_FACE_TYPE = 4;
    public static final int REGISTER_FACE_TYPE = 5;
    public static final int AUTHENTICATE_FACE_TYPE = 6;

    private int mCropType;
    private String mOcrLanguage;
    private int mClassifyType;
    private String mFaceName;
    private String mFaceIDCardNumber;

    private com.min.aiassistant.picture.RectFinderView mCropFinderView;
    private Point mImageViewSize, mImageShowSize;
    private String mCropImageFilePath;
    private int mCurImageRotate = 0;
    private Bitmap mMainBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        String imageFilePath = getIntent().getStringExtra(GlobalValue.INTENT_IMAGE_FILEPATH);
        if (!FileUtils.isFileExist(imageFilePath)) return;

        mMainBitmap = BitmapFactory.decodeFile(imageFilePath);
        mCurImageRotate = BitmapUtils.getJpegImageRotateDegree(imageFilePath);

        mCropImageFilePath = getIntent().getStringExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH);
        if (TextUtils.isEmpty(mCropImageFilePath)) {
            mCropImageFilePath = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator + "crop_main.jpg";
        }
        mCropType = getIntent().getIntExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, 0);
        switch (mCropType) {
            case OCR_TYPE:
                mOcrLanguage = getIntent().getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE);
                break;
            case CLASSIFY_IMAGE_TYPE:
                mClassifyType = getIntent().getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, -1);
                break;
        }
        mFaceName = getIntent().getStringExtra(GlobalValue.INTENT_FACE_NAME);
        mFaceIDCardNumber = getIntent().getStringExtra(GlobalValue.INTENT_FACE_ID_CARD_NUM);

        initView();
    }

    @Override
    public void onGlobalLayout() {
        //获得ImageView中Image的变换矩阵
        ImageView mainImageView = findViewById(R.id.main_image_view);
        Matrix matrix = mainImageView.getImageMatrix();
        float[] values = new float[10];
        matrix.getValues(values);
        //Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = values[0];
        float sy = values[4];
        //计算Image在屏幕上实际绘制的宽高
        mImageShowSize = new Point((int)(mainImageView.getDrawable().getBounds().width() * sx),
                (int)(mainImageView.getDrawable().getBounds().height() * sy));
        //首先默认设置的是android:scaleType="fitCenter"
        mCropFinderView.init(mImageViewSize,
                new Rect((mImageViewSize.x - mImageShowSize.x)/2,
                (mImageViewSize.y - mImageShowSize.y)/2,
                (mImageViewSize.x + mImageShowSize.x)/2,
                (mImageViewSize.y + mImageShowSize.y)/2),
                false);

        mainImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initView() {
        ImageView mainImageView = findViewById(R.id.main_image_view);
        mainImageView.setImageBitmap(BitmapUtils.rotateBitmap(mMainBitmap, mCurImageRotate));
        mainImageView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (isNotFullScreen()) {
            mImageViewSize = CommonUtil.getDisplaySize(this);
            int statusBarHeight = CommonUtil.getStatusBarHeight(this);
            mImageViewSize.y -= statusBarHeight;
        } else {
            mImageViewSize = CommonUtil.getScreenSize(this);
        }

        switch (mCropType) {
            case IDENTIFY_FACE_TYPE:
            case DETECT_FACE_TYPE:
            case OCR_TYPE:
            case CLASSIFY_IMAGE_TYPE:
                findViewById(R.id.recognize_hint).setVisibility(View.VISIBLE);
                break;
            case REGISTER_FACE_TYPE:
                findViewById(R.id.name_edit_text).setVisibility(View.VISIBLE);
                ((EditText)findViewById(R.id.name_edit_text)).setHint(mFaceName);
                break;
            case AUTHENTICATE_FACE_TYPE:
                findViewById(R.id.name_edit_text).setVisibility(View.VISIBLE);
                ((EditText)findViewById(R.id.name_edit_text)).setHint(mFaceName);
                findViewById(R.id.id_num_edit_text).setVisibility(View.VISIBLE);
                ((EditText)findViewById(R.id.id_num_edit_text)).setHint(mFaceIDCardNumber);
                break;
        }

        initTouchScreenView();
    }

    private void initTouchScreenView() {
        mCropFinderView = findViewById(R.id.crop_finder_view);
        mCropFinderView.setVisibility(View.VISIBLE);

        findViewById(R.id.rotate_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage();
            }
        });

        findViewById(R.id.crop_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCropImageFile(false);
            }
        });

        findViewById(R.id.crop_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveCropImageFile(true);
                return true;
            }
        });
    }

    private boolean isNotFullScreen() {
        int uiFlags = getWindow().getDecorView().getSystemUiVisibility();
        return ((uiFlags & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0);
    }

    private Rect getRelativeCropRect() {
        if (null != mCropFinderView) {
            Rect rect = new Rect(mCropFinderView.getRect());

            rect.left -= (mImageViewSize.x - mImageShowSize.x)/2;
            rect.top -= (mImageViewSize.y - mImageShowSize.y)/2;
            rect.right -= (mImageViewSize.x - mImageShowSize.x)/2;
            rect.bottom -= (mImageViewSize.y - mImageShowSize.y)/2;
            return rect;
        }
        return new Rect(0, 0, mImageViewSize.x, mImageViewSize.y);
    }

    private void rotateImage() {
        mCurImageRotate = (mCurImageRotate + 90) % 360;
        Bitmap bitmap = BitmapUtils.rotateBitmap(mMainBitmap, mCurImageRotate);

        ImageView mainImageView = findViewById(R.id.main_image_view);
        mainImageView.setImageBitmap(bitmap);
    }

    private void saveCropImageFile(boolean question) {
        BitmapUtils.saveCropJpeg(mMainBitmap, mImageShowSize, getRelativeCropRect(), mCurImageRotate, mCropImageFilePath);

        Intent intent = new Intent();
        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, mCropImageFilePath);
        intent.putExtra(GlobalValue.INTENT_UNIT_QUESTION, question);
        if (findViewById(R.id.name_edit_text).getVisibility() == View.VISIBLE) {
            intent.putExtra(GlobalValue.INTENT_FACE_NAME, ((EditText) findViewById(R.id.name_edit_text)).getText().toString());
        }
        if (findViewById(R.id.id_num_edit_text).getVisibility() == View.VISIBLE) {
            intent.putExtra(GlobalValue.INTENT_FACE_ID_CARD_NUM, ((EditText) findViewById(R.id.id_num_edit_text)).getText().toString());
        }
        switch (mCropType) {
            case OCR_TYPE:
                intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, mOcrLanguage);
                break;
            case CLASSIFY_IMAGE_TYPE:
                intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, mClassifyType);
                break;
        }

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
