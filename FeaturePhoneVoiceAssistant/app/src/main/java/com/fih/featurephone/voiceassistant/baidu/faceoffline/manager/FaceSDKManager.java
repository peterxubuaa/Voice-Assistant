package com.fih.featurephone.voiceassistant.baidu.faceoffline.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceFeature;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceOcclusion;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.baidu.idl.main.facesdk.model.BDFaceSDKConfig;
import com.baidu.idl.main.facesdk.model.Feature;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.BaiduFaceOfflineAI;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.FaceDetectCallBack;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.callback.FaceFeatureCallBack;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.db.DBManager;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.listener.SdkInitListener;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.listener.UserInfoUpdateListener;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.GlobalSet;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.LivenessModel;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.SingleBaseConfig;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.User;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceAnakinRunMode.BDFACE_ANAKIN_RUN_AT_SMALL_CORE;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceLogInfo.BDFACE_LOG_ALL_MESSAGE;
import static com.fih.featurephone.voiceassistant.baidu.faceoffline.model.GlobalSet.FEATURE_SIZE;

public class FaceSDKManager {
    private final String TAG = FaceSDKManager.class.getSimpleName();

    public static final int SDK_MODEL_LOAD_SUCCESS = 0;
    private static final int SDK_INACTIVATION = 1;
    private static final int SDK_INIT_SUCCESS = 6;

    public static volatile int sInitStatus = SDK_INACTIVATION;
    private FaceAuth mFaceAuth;
    private FaceDetect mFaceDetect;
    private FaceFeature mFaceFeature;

    private ExecutorService mFaceDetectExecutorService;
    private Future mFaceDetectTaskFuture;
    private ExecutorService mDBExecutorService;
    private Future mDBTaskFuture;

    private FaceSDKManager() {
        mFaceDetectExecutorService = Executors.newSingleThreadExecutor();
        mDBExecutorService = Executors.newSingleThreadExecutor();

        mFaceAuth = new FaceAuth();
        mFaceAuth.setActiveLog(BDFACE_LOG_ALL_MESSAGE);
        mFaceAuth.setAnakinConfigure(BDFACE_ANAKIN_RUN_AT_SMALL_CORE, 2);

        mFaceDetect = new FaceDetect();
        mFaceFeature = new FaceFeature();
    }

    private static class HolderClass {
        private static final FaceSDKManager sInstance = new FaceSDKManager();
    }

    public static FaceSDKManager getInstance() {
        return HolderClass.sInstance;
    }

    private FaceDetect getFaceDetect() {
        return mFaceDetect;
    }

    private FaceFeature getFaceFeature() {
        return mFaceFeature;
    }

    /**
     * 初始化鉴权，如果鉴权通过，直接初始化模型
     *
     * @param context
     * @param listener
     */
    public void init(final Context context, final SdkInitListener listener) {
        PreferencesUtil.initPrefs(context.getApplicationContext());

        final String licenseOfflineKey = PreferencesUtil.getString("activate_offline_key", "");
        final String licenseOnlineKey = PreferencesUtil.getString("activate_online_key", "");

        // 如果licenseKey 不存在提示授权码为空，并跳转授权页面授权
        if (TextUtils.isEmpty(licenseOfflineKey) && TextUtils.isEmpty(licenseOnlineKey)) {
            CommonUtil.toast(context, context.getString(R.string.baidu_face_authentic_fail));
            if (listener != null) {
                listener.initLicenseFail(-1, context.getString(R.string.baidu_face_authentic_fail));
            }
            return;
        }

        if (listener != null) {
            listener.initStart();
        }

        if (!TextUtils.isEmpty(licenseOfflineKey)) {
            // 离线激活
            mFaceAuth.initLicenseOffLine(context, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        sInitStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                        initModel(context, listener);
                    }
                }
            });
        } else if (!TextUtils.isEmpty(licenseOnlineKey)) {
            // 在线激活
            mFaceAuth.initLicenseOnLine(context, licenseOnlineKey, new Callback() {
                @Override
                public void onResponse(int code, String response) {
                    if (code == 0) {
                        sInitStatus = SDK_INIT_SUCCESS;
                        if (listener != null) {
                            listener.initLicenseSuccess();
                        }
                        initModel(context, listener);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.initLicenseFail(-1, context.getString(R.string.baidu_face_authentic_fail));
            }
        }
    }

    /**
     * 初始化模型，目前包含检查，活体，识别模型；因为初始化是顺序执行，可以在最后初始化回调中返回状态结果
     *
     * @param context
     * @param listener
     */
    public void initModel(final Context context, final SdkInitListener listener) {
        CommonUtil.toast(context, context.getString(R.string.baidu_face_model_init_start));

        initConfig();

        //同步依次加载
        mFaceDetect.initModel(context,
                GlobalSet.DETECT_VIS_MODEL, //可见光模型
                "",//GlobalSet.DETECT_NIR_MODE, //近红外检测模型（非必要参数，可以为空）
                GlobalSet.ALIGN_MODEL,//对齐模型
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        mFaceDetect.initQuality(context,
                GlobalSet.BLUR_MODEL, //模糊检测模型
                GlobalSet.OCCLUSION_MODEL, //遮挡检测模型
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0 && listener != null) {
                            listener.initModelFail(code, response);
                        }
                    }
                });

        mFaceDetect.initAttrEmo(context, GlobalSet.ATTRIBUTE_MODEL, GlobalSet.EMOTION_MODEL, new Callback() {
            @Override
            public void onResponse(int code, String response) {
                if (code != 0 && listener != null) {
                    listener.initModelFail(code, response);
                }
            }
        });

        mFaceFeature.initModel(context,
                "", //GlobalSet.RECOGNIZE_IDPHOTO_MODEL, //证件照图片模型
                GlobalSet.RECOGNIZE_VIS_MODEL, //可见光图片模型
                "", //红外图片模型（非必要参数，可以为空）
                new Callback() {
                    @Override
                    public void onResponse(int code, String response) {
                        if (code != 0) {
                            CommonUtil.toast(context, context.getString(R.string.baidu_face_model_init_fail));
                            if (listener != null) {
                                listener.initModelFail(code, response);
                            }
                        } else {
                            sInitStatus = SDK_MODEL_LOAD_SUCCESS;
                            // 模型初始化成功，加载人脸数据
                            initDataBases(context);
                            CommonUtil.toast(context, context.getString(R.string.baidu_face_model_init_end));
                            if (listener != null) {
                                listener.initModelSuccess();
                            }
                        }
                    }
                });
    }

    /**
     * 初始化配置
     *
     * @return
     */
    private void initConfig() {
        // 注册默认开启质量检测
        SingleBaseConfig.getBaseConfig().setQualityControl(true);
        // 属性开启属性检测
        SingleBaseConfig.getBaseConfig().setAttribute(true);
        // 设置人脸注册的最大相似度
        SingleBaseConfig.getBaseConfig().setSimilarity(95);

        if (mFaceDetect != null) {
            BDFaceSDKConfig config = new BDFaceSDKConfig();
            // 最小人脸个数检查，默认设置为1,用户根据自己需求调整
            config.maxDetectNum = 1;
            // 默认为80px。可传入大于30px的数值，小于此大小的人脸不予检测，生效时间第一次加载模型
            config.minFaceSize = SingleBaseConfig.getBaseConfig().getMinimumFace();
            // 是否进行属性检测，默认关闭
            config.isAttribute = SingleBaseConfig.getBaseConfig().isAttribute();
            // 模糊，遮挡，光照三个质量检测和姿态角查默认关闭，如果要开启，设置页启动
            config.isCheckBlur = config.isOcclusion
                    = config.isIllumination = config.isHeadPose
                    = SingleBaseConfig.getBaseConfig().isQualityControl();
            mFaceDetect.loadConfig(config);
        }
    }

    private void initDataBases(Context context) {
        // 初始化数据库
        DBManager.getInstance().init(context);
        // 数据变化，更新内存
        pushFaceFeature();
    }

    /**
     * 检测-活体-特征- 全流程
     *
     * @param rgbData            可见光YUV 数据流
     * @param srcHeight          可见光YUV 数据流-高度
     * @param srcWidth           可见光YUV 数据流-宽度
     * @param faceDetectCallBack
     */
    public void onDetectCheck(final byte[] rgbData,
                              final int srcWidth,
                              final int srcHeight,
                              final int detectDirection,
                              final int mirror,
                              final FaceDetectCallBack faceDetectCallBack) {
        if (mFaceDetectTaskFuture != null && !mFaceDetectTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mFaceDetectTaskFuture = mFaceDetectExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                faceDataDetect(rgbData, srcWidth, srcHeight, detectDirection, mirror, faceDetectCallBack);
            }
        });
    }

    /**
     * 人脸检测
     *
     * @param argb
     * @param width
     * @param height
     * @param faceDetectCallBack
     */
    private void faceDataDetect(final byte[] argb, int width, int height, int detectDirection, int mirror,
                                FaceDetectCallBack faceDetectCallBack) {
        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(argb, height, width,
                BDFaceSDKCommon.BDFaceImageType.BDFACE_IMAGE_TYPE_YUV_420,
                detectDirection, mirror
//                SingleBaseConfig.getBaseConfig().getDetectDirection(),
//                SingleBaseConfig.getBaseConfig().getMirrorRGB()
        );

        LivenessModel livenessModel = new LivenessModel();
        livenessModel.setBdFaceImageInstance(rgbInstance);

        long startTime = System.currentTimeMillis();
        FaceInfo[] faceInfos = getFaceDetect().track(BDFaceSDKCommon.DetectType.DETECT_VIS, rgbInstance);
        livenessModel.setRgbDetectDuration(System.currentTimeMillis() - startTime);
        // getImage() 获取送检图片
        livenessModel.setBdFaceImageInstance(rgbInstance.getImage());

        if (faceInfos != null && faceInfos.length > 0) {
            livenessModel.setTrackFaceInfo(faceInfos);
            FaceInfo faceInfo = faceInfos[0];
            livenessModel.setFaceInfo(faceInfo);
            livenessModel.setLandmarks(faceInfo.landmarks);
            // 质量检测，针对模糊度、遮挡、角度
            if (onQualityCheck(livenessModel, faceDetectCallBack)) {
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
                if (faceDetectCallBack != null) {
                    faceDetectCallBack.onFaceDetectCallback(livenessModel);
                    faceDetectCallBack.onFaceDetectDrawCallback(livenessModel);
                }
            } else {
                // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
                rgbInstance.destory();
            }
        } else {
            // 流程结束销毁图片，开始下一帧图片检测，否着内存泄露
            rgbInstance.destory();
            if (faceDetectCallBack != null) {
                faceDetectCallBack.onTip(0, "未检测到人脸");
                faceDetectCallBack.onFaceDetectCallback(null);
                faceDetectCallBack.onFaceDetectDrawCallback(null);
            }
        }
    }

    /**
     * 质量检测结果过滤，如果需要质量检测，
     * 需要调用 SingleBaseConfig.getBaseConfig().setQualityControl(true);设置为true，
     * 再调用  FaceSDKManager.getInstance().initConfig() 加载到底层配置项中
     *
     * @param livenessModel
     * @param faceDetectCallBack
     * @return
     */
    private boolean onQualityCheck(final LivenessModel livenessModel,
                                  final FaceDetectCallBack faceDetectCallBack) {

        if (!SingleBaseConfig.getBaseConfig().isQualityControl()) {
            return true;
        }

        if (livenessModel != null && livenessModel.getFaceInfo() != null) {
            // 角度过滤
            if (Math.abs(livenessModel.getFaceInfo().yaw) > SingleBaseConfig.getBaseConfig().getYaw()) {
                faceDetectCallBack.onTip(-1, "人脸左右偏转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().roll) > SingleBaseConfig.getBaseConfig().getRoll()) {
                faceDetectCallBack.onTip(-1, "人脸平行平面内的头部旋转角超出限制");
                return false;
            } else if (Math.abs(livenessModel.getFaceInfo().pitch) > SingleBaseConfig.getBaseConfig().getPitch()) {
                faceDetectCallBack.onTip(-1, "人脸上下偏转角超出限制");
                return false;
            }

            // 模糊结果过滤
            float blur = livenessModel.getFaceInfo().bluriness;
            if (blur > SingleBaseConfig.getBaseConfig().getBlur()) {
                faceDetectCallBack.onTip(-1, "图片模糊");
                return false;
            }

            // 光照结果过滤
            float illum = livenessModel.getFaceInfo().illum;
            if (illum < SingleBaseConfig.getBaseConfig().getIllumination()) {
                faceDetectCallBack.onTip(-1, "图片光照不通过");
                return false;
            }

            // 遮挡结果过滤
            if (livenessModel.getFaceInfo().occlusion != null) {
                BDFaceOcclusion occlusion = livenessModel.getFaceInfo().occlusion;

                if (occlusion.leftEye > SingleBaseConfig.getBaseConfig().getLeftEye()) {
                    // 左眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "左眼遮挡");
                } else if (occlusion.rightEye > SingleBaseConfig.getBaseConfig().getRightEye()) {
                    // 右眼遮挡置信度
                    faceDetectCallBack.onTip(-1, "右眼遮挡");
                } else if (occlusion.nose > SingleBaseConfig.getBaseConfig().getNose()) {
                    // 鼻子遮挡置信度
                    faceDetectCallBack.onTip(-1, "鼻子遮挡");
                } else if (occlusion.mouth > SingleBaseConfig.getBaseConfig().getMouth()) {
                    // 嘴巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "嘴巴遮挡");
                } else if (occlusion.leftCheek > SingleBaseConfig.getBaseConfig().getLeftCheek()) {
                    // 左脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "左脸遮挡");
                } else if (occlusion.rightCheek > SingleBaseConfig.getBaseConfig().getRightCheek()) {
                    // 右脸遮挡置信度
                    faceDetectCallBack.onTip(-1, "右脸遮挡");
                } else if (occlusion.chin > SingleBaseConfig.getBaseConfig().getChinContour()) {
                    // 下巴遮挡置信度
                    faceDetectCallBack.onTip(-1, "下巴遮挡");
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 单独调用 特征提取
     *
     * @param imageInstance       可见光底层送检对象
     * @param landmark            检测眼睛，嘴巴，鼻子，72个关键点
     * @param featureCheckMode    特征提取模式
     * @param faceFeatureCallBack 回掉方法
     */
    public void onFeatureCheck(BDFaceImageInstance imageInstance, float[] landmark,
                               BDFaceSDKCommon.FeatureType featureCheckMode,
                               final FaceFeatureCallBack faceFeatureCallBack) {

        BDFaceImageInstance rgbInstance = new BDFaceImageInstance(imageInstance.data,
                imageInstance.height, imageInstance.width,
                imageInstance.imageType, 0, 0);

        byte[] feature = new byte[FEATURE_SIZE];
        float featureSize = getFaceFeature().feature(
                featureCheckMode, rgbInstance, landmark, feature);
        if (featureSize == FEATURE_SIZE / 4.0) {
            // 特征提取成功
            if (faceFeatureCallBack != null) {
                faceFeatureCallBack.onFaceFeatureCallBack(featureSize, feature);
            }

        }
        // 流程结束销毁图片
        rgbInstance.destory();
    }

    /**
     * 数据库发现变化时候，重新把数据库中的人脸信息添加到内存中，id+feature
     */
    public void pushFaceFeature() {
        if (mDBTaskFuture != null && !mDBTaskFuture.isDone()) {
            mDBTaskFuture.cancel(true);
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                ArrayList<Feature> features = new ArrayList<>();
                List<User> listUser = DBManager.getInstance().queryUserList(0, Integer.MAX_VALUE);
                for (int j = 0; j < listUser.size(); j++) {
                    Feature feature = new Feature();
                    feature.setId(listUser.get(j).getDBID());
                    feature.setFeature(listUser.get(j).getFaceFeature());
                    features.add(feature);
                }
                getFaceFeature().featurePush(features);
            }
        });
    }

    public void identifyImage(final Bitmap bitmap, final BaiduFaceOfflineAI.OnFaceOfflineListener listener) {
        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (null == listener) return;;
                byte[] feature = new byte[FEATURE_SIZE];
                if (getFeature(bitmap, feature) >= 0) {
                    User user = searchUserByFeature(feature);
                    if (null != user) {
                        listener.onFinalResult(user.getUserName(), 0);
                    } else {
                        listener.onFinalResult(null, 0);
                    }
                } else {
                    listener.onError("获取图片人脸特征出错");
                }
            }
        });
    }

    /**
     * 数据库操作
     */
    public boolean registerUserIntoDBManager(String userID, String userName, String userInfo,
                                                 String faceImagePath, byte[] faceFeature) {
        User user = new User();
        /*
         * 用户id（由数字、字母、下划线组成），长度限制128B
         * uid为用户的id,百度对uid不做限制和处理，应该与您的帐号系统中的用户id对应。
         */
        user.setUserID(userID);
        user.setUserName(userName);
        user.setUserInfo(userInfo);
        user.setFaceImagePath(faceImagePath);
        user.setFaceFeature(faceFeature);

        //根据用户名称查出所有人脸特征信息，如果已经有相同的人脸特征信息则返回
        List<User> userList = DBManager.getInstance().queryUserListByUserName(user.getUserName());
        if (null != userList) {
            for (User savedUser : userList) {
                float score = getFaceFeature().featureCompare(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                        user.getFaceFeature(),
                        savedUser.getFaceFeature(),
                        true
                );
                if (score >= SingleBaseConfig.getBaseConfig().getSimilarity()) {
                    Log.e(TAG, "The same face has been saved as the user name");
                    return false;
                }
            }
        }
        // 添加用户信息到数据库
        return DBManager.getInstance().addUser(user);
    }

    // 更换图片
    public void updateImageIntoDBManager(final Bitmap bitmap, final User user, final UserInfoUpdateListener listener) {
        if (null == bitmap || null == user || null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(user.getFaceImagePath())) {
                    listener.updateImageFailure(user, "头像保存文件路径为空");
                    return;
                }

                byte[] feature = new byte[FEATURE_SIZE];
                float ret;
                // 检测人脸，提取人脸特征值
                ret = getFeature(bitmap, feature);
                if (ret == -1) {
                    listener.updateImageFailure(user, "未检测到人脸，可能原因：人脸太小");
                } else if (ret == 128) {
                    // 添加用户信息到数据库
                    user.setFaceFeature(feature);
                    boolean update = DBManager.getInstance().updateUser(user);
                    if (update) {
                        // 保存图片到新目录中
                        File savePicPath = new File(user.getFaceImagePath());
                        if (FileUtils.saveBitmap(savePicPath, bitmap)) {
                            listener.updateImageSuccess(user, bitmap);
                        } else {
                            listener.updateImageFailure(user,"图片保存失败");
                        }
                        pushFaceFeature();
                    } else {
                        listener.updateImageFailure(user, "更新数据库失败");
                    }
                } else {
                    listener.updateImageFailure(user, "未检测到人脸");
                }
            }
        });
    }

    public void updateUserIntoDBManager(final User user, final UserInfoUpdateListener listener) {
        if (null == user || null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = DBManager.getInstance().updateUser(user);
                if (result) {
                    listener.userUpdateSuccess(user);
                } else {
                    listener.userUpdateFailure(user, "更新数据库失败");
                }
            }
        });
    }

    public void deleteUserIntoDBManager(final User user, final UserInfoUpdateListener listener) {
        if (null == user || null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = DBManager.getInstance().deleteUserByDBID(user.getDBID());

                List<User> deletedUserList = new ArrayList<>();
                if (result) {
                    FileUtils.deleteFile(user.getFaceImagePath());
                    deletedUserList.add(user);
                    listener.userListDeleteSuccess(deletedUserList);
                } else {
                    listener.userListDeleteFailure(deletedUserList,"删除数据失败");
                }
            }
        });
    }

    public void deleteUserListIntoDBManager(final List<User> userList, final UserInfoUpdateListener listener) {
        if (null == userList || null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                boolean result = true;
                String failureUserName = "";
                List<User> deletedUserList = new ArrayList<>();
                for (User user : userList) {
                    if (!DBManager.getInstance().deleteUserByDBID(user.getDBID())){
                        failureUserName = user.getUserName();
                        result = false;
                        break;
                    }
                    FileUtils.deleteFile(user.getFaceImagePath());
                    deletedUserList.add(user);
                }
                if (result) {
                    listener.userListDeleteSuccess(userList);
                } else {
                    listener.userListDeleteFailure(deletedUserList, "删除数据失败：" + failureUserName);
                }
            }
        });
    }

    public void queryUserListFromDBManager(final UserInfoUpdateListener listener) {
        if (null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                List<User> userList = DBManager.getInstance().queryUserList(0, Integer.MAX_VALUE);
                if (null != userList) {
                    listener.userListQuerySuccess(userList, true);
                } else {
                    listener.userListQueryFailure("查询数据失败");
                }
            }
        });
    }

    public void queryUserListFromDBManager(final ArrayList<String> userIDList, final UserInfoUpdateListener listener) {
        if (null == userIDList || userIDList.size() == 0 || null == listener) {
            Log.e(TAG, "Params of updateImage are invalid");
            return;
        }

        mDBTaskFuture = mDBExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                List<User> userList = new ArrayList<>();
                for (String userID : userIDList) {
                    User user = DBManager.getInstance().queryUserByUserID(userID);
                    if (null != user) {
                        userList.add(user);
                    }
                }
                if (userList.size() > 0) {
                    listener.userListQuerySuccess(userList, false);
                } else {
                    listener.userListQueryFailure("查询数据失败");
                }
            }
        });
    }

    /**
     * 提取特征值
     */
    private float getFeature(Bitmap bitmap, byte[] feature) {
        if (bitmap == null) return -1;

        BDFaceImageInstance imageInstance = new BDFaceImageInstance(bitmap);
        // 最大检测人脸，获取人脸信息
        FaceInfo[] faceInfos = getFaceDetect()
                .detect(BDFaceSDKCommon.DetectType.DETECT_VIS, imageInstance);
        float ret = -1;
        if (faceInfos != null && faceInfos.length > 0) {
            FaceInfo faceInfo = faceInfos[0];
            // 人脸识别，提取人脸特征值
            ret = getFaceFeature().feature(
                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO, imageInstance,
                    faceInfo.landmarks, feature);
        }
        imageInstance.destory();
        return ret;
    }

    public User searchUserByFeature(byte[] feature) {
        ArrayList<Feature> featureResult = getFaceFeature().featureSearch(feature,
                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,1, true);

        if (featureResult != null && featureResult.size() > 0) {
            // 获取第一个数据
            Feature topFeature = featureResult.get(0);
            // 判断第一个阈值是否大于设定阈值，如果大于，检索成功
            if (topFeature != null && topFeature.getScore() > SingleBaseConfig.getBaseConfig().getThreshold()) {
                // 当前featureEntity 只有id+feature 索引，在数据库中查到完整信息
                return DBManager.getInstance().queryUserByDBId(topFeature.getId());
            }
        }

        return null;
    }
}