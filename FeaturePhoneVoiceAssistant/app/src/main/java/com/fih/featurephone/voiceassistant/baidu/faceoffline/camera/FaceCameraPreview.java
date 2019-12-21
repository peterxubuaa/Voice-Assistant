package com.fih.featurephone.voiceassistant.baidu.faceoffline.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.CameraDataCallback;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.FaceDetectCallBack;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.FaceFeatureCallBack;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.LivenessModel;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.nio.ByteBuffer;

public class FaceCameraPreview extends FrameLayout {
    private final int CAMERA_VIDEO_WIDTH = 640;
    private final int CAMERA_VIDEO_HEIGHT = 480;

    private TextureView mCameraTextureView;
    private TextView mFaceDetectInfoTextView;
    private TextureView mFaceFrameTextureView;
    private FaceRoundView mFaceRoundView;
    private RectF mFaceFrameRectF;
    private Paint mFaceFramePaint;
    private ImageView mUploadRGBFaceImageView;
    private Handler mUIHandler;
    private IFaceDetectListener mFaceRegisterListener;
    private boolean mEnableFaceDetect = false;
    private boolean mShowUploadRGBFaceImageView = false;
    private boolean mLayoutRotate = true;
    private int mMirror = 0;
    private int mDetectDirection = 90;


    public interface IFaceDetectListener {
        boolean onDetectFace(Bitmap faceBitmap, byte[] feature, FaceInfo faceInfo);
    }

    public FaceCameraPreview(Context context) {
        super(context);
        init();
    }

    public FaceCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceCameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCameraTextureView = new TextureView(getContext());
        addView(mCameraTextureView);

        mFaceFrameTextureView = new TextureView(getContext());
        addView(mFaceFrameTextureView);
        mFaceFrameTextureView.setOpaque(false);
        mFaceFrameRectF = new RectF();
        mFaceFramePaint = new Paint();

        mFaceRoundView = new FaceRoundView(getContext());
        mFaceRoundView.setCoverType(FaceRoundView.COVER_TYPE.OVAL);
        addView(mFaceRoundView);

        mFaceDetectInfoTextView = new TextView(getContext());
        addView(mFaceDetectInfoTextView);
        mFaceDetectInfoTextView.setText(getContext().getString(R.string.baidu_face_camera_preview_hint));
        mFaceDetectInfoTextView.setTextColor(Color.RED);
        mFaceDetectInfoTextView.setMaxLines(2);

        mUploadRGBFaceImageView = new ImageView(getContext());
        addView(mUploadRGBFaceImageView);

        //设置后置摄像头
        if (CommonUtil.isSupportMultiTouch(getContext())) {
            mDetectDirection = 90;
        } else {
            mDetectDirection = 270;
        }
        mMirror = 0;
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);

        mUIHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom); must remove
        if (getWidth() == 0 || getHeight() == 0) return;

        // 尤其重要的是，FrameLayout的left，top，right，bottom值是相对于FrameLayout的父布局而言的；
        // 对子view调用layout方法时，传入的坐标系则是以FrameLayout而言的，这点需要进行转换
        int videoWidth = mLayoutRotate? CAMERA_VIDEO_HEIGHT : CAMERA_VIDEO_WIDTH;
        int videoHeight = mLayoutRotate? CAMERA_VIDEO_WIDTH : CAMERA_VIDEO_HEIGHT;

        // 根据camera采集的尺寸640 * 480调整预览尺寸
        if (getWidth() * videoHeight > getHeight() * videoWidth) {
            int scaledChildWidth = videoWidth * getHeight() / videoHeight;
            left = (getWidth() - scaledChildWidth) / 2;
            top = 0;
            right = (getWidth() + scaledChildWidth) / 2;
            bottom = getHeight();
        } else {
            int scaledChildHeight = videoHeight * getWidth() / videoWidth;
            left = 0;
            top = (getHeight() - scaledChildHeight) / 2;
            right = getWidth();
            bottom = (getHeight() + scaledChildHeight) / 2;
        }

        mCameraTextureView.layout(left, top, right, bottom);
        mFaceFrameTextureView.layout(left, top, right, bottom);
        mFaceRoundView.layout(left, top, right, bottom);

        mFaceDetectInfoTextView.layout(left, top, right - left, top + 100);
        mUploadRGBFaceImageView.layout(right - (right - left)/5, top, right, top + (bottom - top)/5);
    }

    public void setFaceRegisterListener(IFaceDetectListener listener) {
        mFaceRegisterListener = listener;
    }

    public void setFaceDetect(final boolean faceDetect) {
        displayTip(faceDetect? getContext().getString(R.string.baidu_face_camera_preview_detect_start)
                        : getContext().getString(R.string.baidu_face_camera_preview_detect_pause));

        mEnableFaceDetect = faceDetect;
    }

    public void setLayoutRotate(boolean layoutRotate) {
        mLayoutRotate = layoutRotate;
    }

    public void setShowUploadRGBFaceImageView(boolean show) {
        mShowUploadRGBFaceImageView = show;
    }

    public boolean isCameraPreviewing(){
        return CameraPreviewManager.getInstance().isPreviewRunning();
    }

    public void startCameraPreview() {
        if (CameraPreviewManager.getInstance().isPreviewRunning()) return;

        CameraPreviewManager.getInstance().startPreview(mCameraTextureView, CAMERA_VIDEO_WIDTH, CAMERA_VIDEO_HEIGHT, new CameraDataCallback() {
            @Override
            public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                // 调试模式打开 显示实际送检图片的方向，SDK只检测人脸朝上的图
                if (mShowUploadRGBFaceImageView) {
                    showDetectImage(data);
                }
                // 拿到相机帧数
                if (mEnableFaceDetect) {
                    faceDetect(data, width, height);
                }
            }
        });

        displayTip(getContext().getString(R.string.baidu_face_camera_preview_hint));
    }

    public void stopCameraPreview() {
        if (CameraPreviewManager.getInstance().isPreviewRunning()) {
            CameraPreviewManager.getInstance().stopPreview();
        }
    }

    public void switchCameraPreview() {
        if (CameraPreviewManager.getInstance().isPreviewRunning()) {
            CameraPreviewManager.getInstance().stopPreview();
            if (CameraPreviewManager.getInstance().getCameraFacing() == CameraPreviewManager.CAMERA_FACING_FRONT) {
                mDetectDirection = 90;
                mMirror = 0;
                CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
            } else {
                mDetectDirection = 270;
                mMirror = 1;
                CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
            }
            startCameraPreview();
        }
    }

    // 摄像头数据处理
    private void faceDetect(byte[] data, final int width, final int height) {
        // 摄像头预览数据进行人脸检测
        FaceSDKManager.getInstance().onDetectCheck(data, width, height, mDetectDirection, mMirror,
                new FaceDetectCallBack() {
            @Override
            public void onFaceDetectCallback(LivenessModel livenessModel) {
                // 做过滤
                boolean isFilterSuccess = faceSizeFilter(livenessModel.getFaceInfo(), width, height);
                if (isFilterSuccess) {
                    // 展示model
                    checkResult(livenessModel);
                }
            }

            @Override
            public void onTip(int code, final String msg) {
                if (0 == code) showFrame(null);
                displayTip(msg);
            }

            @Override
            public void onFaceDetectDrawCallback(LivenessModel livenessModel) {
                if (mEnableFaceDetect) {
                    showFrame(livenessModel);// 绘制人脸框
                } else {
                    showFrame(null);
                }
            }
        });
    }

    // 人脸大小顾虑
    public boolean faceSizeFilter(FaceInfo faceInfo, int bitMapWidth, int bitMapHeight) {
        // 判断人脸大小，若人脸超过屏幕二分一，则提示文案“人脸离手机太近，请调整与手机的距离”；
        // 若人脸小于屏幕三分一，则提示“人脸离手机太远，请调整与手机的距离”
        // 因为摄像头旋转了90度，所以宽高也需要交换
        if (bitMapWidth > bitMapHeight) {
            int temp = bitMapWidth;
            bitMapWidth = bitMapHeight;
            bitMapHeight = temp;
        }

        float ratio = faceInfo.width / (float)bitMapWidth;
        if (ratio > 0.6) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_close));
            return false;
        } else if (ratio < 0.3) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_far));
            return false;
        } else if (faceInfo.centerX > (float)bitMapWidth * 3 / 4) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_right));
            return false;
        } else if (faceInfo.centerX < (float)bitMapWidth / 4) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_left));
            return false;
        } else if (faceInfo.centerY > (float)bitMapHeight * 3 / 4) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_down));
            return false;
        } else if (faceInfo.centerY < (float)bitMapHeight / 4) {
            displayTip(getContext().getString(R.string.baidu_face_camera_preview_detect_up));
            return false;
        }

        return true;
    }

    private String getFaceAttr(FaceInfo faceInfo) {
        StringBuilder faceAttr = new StringBuilder();

        if (null != faceInfo) {
            faceAttr.append(faceInfo.age);
            String[] emotionThree = getContext().getResources().getStringArray(R.array.face_detect_emotion);
            faceAttr.append(",").append(emotionThree[faceInfo.emotionThree.ordinal()]);
//            String[] emotionSeven = getContext().getResources().getStringArray(R.array.face_detect_emotion_enum);
//            faceAttr.append(",").append(emotionSeven[faceInfo.emotionSeven.ordinal()]);
//
            String[] gender = getContext().getResources().getStringArray(R.array.face_detect_gender);
            faceAttr.append(",").append(gender[faceInfo.gender.ordinal()]);

            String[] glass = getContext().getResources().getStringArray(R.array.face_detect_glass);
            faceAttr.append(",").append(glass[faceInfo.glasses.ordinal()]);

            String[] race = getContext().getResources().getStringArray(R.array.face_detect_race);
            faceAttr.append(",").append(race[faceInfo.race.ordinal()]);
        }

        return faceAttr.toString();
    }

    // 检测结果输出
    private void checkResult(final LivenessModel model) {
        // 生活照
        FaceSDKManager.getInstance().onFeatureCheck(model.getBdFaceImageInstance(), model.getLandmarks(),
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, new FaceFeatureCallBack() {
                    @Override
                    public void onFaceFeatureCallBack(float featureSize, byte[] feature) {
                        // 特征提取成功
                        if (featureSize == 128) {
                            displayTip(getContext().getString(R.string.baidu_face_camera_preview_feature_success)
                                    + getFaceAttr(model.getFaceInfo()));
                            if (null != mFaceRegisterListener) {
                                Bitmap faceRgbBitmap = getInstanceBmp(model.getBdFaceImageInstance());
                                if (mFaceRegisterListener.onDetectFace(faceRgbBitmap, feature, model.getFaceInfo())) {
                                    CameraPreviewManager.getInstance().stopPreview();
                                }
                            }
                        } else {
                            displayTip(getContext().getString(R.string.baidu_face_camera_preview_feature_fail));
                        }
                    }
                });
    }

    private void displayTip(final String status) {
        if (mEnableFaceDetect) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFaceDetectInfoTextView.setText(status);
                }
            });
        }
    }

    private void showFrame(final LivenessModel model) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mFaceFrameTextureView.lockCanvas();
                if (null == canvas) return;

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);// 清空canvas
                if (null != model) {
                    FaceInfo[] faceInfos = model.getTrackFaceInfo();
                    if (faceInfos != null && faceInfos.length > 0) {
                        FaceInfo faceInfo = faceInfos[0];

                        mFaceFrameRectF.set(getFaceRect(faceInfo));
                        // 检测图片的坐标和显示的坐标不一样，需要转换。
                        mapFromOriginalRect(mFaceFrameRectF, mCameraTextureView, model.getBdFaceImageInstance());
                        mFaceFramePaint.setColor(Color.GREEN);
                        mFaceFramePaint.setStyle(Paint.Style.STROKE);
                        // 绘制框
                        canvas.drawRect(mFaceFrameRectF, mFaceFramePaint);
                    }
                }
                mFaceFrameTextureView.unlockCanvasAndPost(canvas);
            }
        });
    }

    private void showDetectImage(byte[] rgb) {
        if (rgb == null) {
            return;
        }
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(rgb, CAMERA_VIDEO_HEIGHT,
                CAMERA_VIDEO_WIDTH, BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                    mDetectDirection, mMirror);
        BDFaceImageInstance imageInstance = rgbInstance.getImage();
        final Bitmap bitmap = getInstanceBmp(imageInstance);
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                mUploadRGBFaceImageView.setImageBitmap(bitmap);
            }
        });
        // 流程结束销毁图片，开始下一帧图片检测，否则内存泄露
        rgbInstance.destory();
    }

    public Rect getFaceRect(FaceInfo faceInfo) {
        Rect rect = new Rect();
        rect.left = (int) ((faceInfo.centerX - faceInfo.width / 2));
        rect.top = (int) ((faceInfo.centerY - faceInfo.height / 2));
        rect.right = (int) ((faceInfo.centerX + faceInfo.width / 2));
        rect.bottom = (int) ((faceInfo.centerY + faceInfo.height / 2));
        return rect;
    }

    public void mapFromOriginalRect(RectF rectF,
                                           TextureView textureView,
                                           BDFaceImageInstance imageFrame) {
        int selfWidth = textureView.getWidth();
        int selfHeight = textureView.getHeight();
        Matrix matrix = new Matrix();
        if (selfWidth * imageFrame.height > selfHeight * imageFrame.width) {
            int targetHeight = imageFrame.height * selfWidth / imageFrame.width;
            int delta = (targetHeight - selfHeight) / 2;
            float ratio = 1.0f * selfWidth / imageFrame.width;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(0, -delta);
        } else {
            int targetWith = imageFrame.width * selfHeight / imageFrame.height;
            int delta = (targetWith - selfWidth) / 2;
            float ratio = 1.0f * selfHeight / imageFrame.height;
            matrix.postScale(ratio, ratio);
            matrix.postTranslate(-delta, 0);
        }
        matrix.mapRect(rectF);
    }

    public Bitmap getInstanceBmp(BDFaceImageInstance newInstance) {
        Bitmap transBmp = null;
        if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_RGBA) {
            transBmp = Bitmap.createBitmap(newInstance.width, newInstance.height, Bitmap.Config.ARGB_8888);
            transBmp.copyPixelsFromBuffer(ByteBuffer.wrap(newInstance.data));
        } else if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_BGR) {
            transBmp = BitmapUtils.BGR2Bitmap(newInstance.data, newInstance.width, newInstance.height);
        } else if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420) {
            transBmp = BitmapUtils.yuv2Bitmap(newInstance.data, newInstance.width, newInstance.height);
        } else if (newInstance.imageType == BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_GRAY) {
            transBmp = BitmapUtils.Depth2Bitmap(newInstance.data, newInstance.width, newInstance.height);
        }
        return transBmp;
    }
}
