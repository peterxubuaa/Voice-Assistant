package com.fih.featurephone.voiceassistant.baidu.faceoffline.model;

/**
 * @Time: 2019/5/24
 * @Author: v_zhangxiaoqing01

 */

public class GlobalSet {
    // 模型在asset 下path 为空
    private static final String PATH = "";
    // 模型在SD 卡下写对应的绝对路径
    // public static final String PATH = "/storage/emulated/0/baidu_face/model/";
    public static final int FEATURE_SIZE = 512;

    public static final String DETECT_VIS_MODEL = PATH
            + "detect/detect_rgb-faceboxes-pa-anakin.model.int8-0.0.1.7";
    public static final String DETECT_NIR_MODE = PATH
            + "detect/detect_nir-faceboxes-pa-autodl_anakin.model.int8-0.0.2.1";
    public static final String ALIGN_MODEL = PATH
            + "align/align-customized-ca-small8-anakin.model.float32.6_4_0_1";
//    public static final String LIVE_VIS_MODEL = PATH
//            + "silent_live/liveness_rgb-customized-pa-anakin.model.float32-4.1.6.3";
//    public static final String LIVE_NIR_MODEL = PATH
//            + "silent_live/liveness_nir-customized-pa-anakin.model.int8-4.1.5.2";
//    public static final String LIVE_DEPTH_MODEL = PATH
//            + "silent_live/liveness_depth-customized-pa-autodl_anakin.model.int8-4.1.7.1";
    public static final String RECOGNIZE_VIS_MODEL = PATH
            + "feature/feature_live-mobilenet-pa-anakin.model.int16-1.0.1.3";
//    public static final String RECOGNIZE_IDPHOTO_MODEL = PATH
//            + "feature/feature_id-mobilenet-pt-anakin.model.float32-2.0.60.3";
    public static final String OCCLUSION_MODEL = PATH
            + "occlusion/occlusion-unet-ca-anakin.model.float32-1.1.0.2";
    public static final String BLUR_MODEL = PATH
            + "blur/blur-vgg-ca-anakin.model.float32-3.0.1.1";
    public static final String ATTRIBUTE_MODEL = PATH
            + "attribute/attribute-vgg-ca-anakin.model.int16-1.0.3.3";
    public static final String EMOTION_MODEL = PATH
            + "emotion/emotion-vgg-ca-anakin.model.float32-2.0.1.2";
//    public static final String GAZE_MODEL = PATH
//            + "gaze/rgb_gaze.anakin.lite.bin";
}
