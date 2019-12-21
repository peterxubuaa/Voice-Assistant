package com.fih.featurephone.voiceassistant.baidu.imageclassify;

import android.content.Context;

import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyAdvancedGeneral;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyAnimalPlant;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyCar;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyCurrency;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyDish;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyIngredient;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyLandMark;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyLogo;
import com.fih.featurephone.voiceassistant.baidu.imageclassify.classifytype.ClassifyRedWine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//https://ai.baidu.com/docs#/ImageClassify-API/ebc492b1
public class BaiduClassifyImageAI {
    //必须和R.array.classify_image_type_item的顺序一致
    public static final int CLASSIFY_TYPE_ADVANCED_GENERAL = 0;
    private final int CLASSIFY_TYPE_PLANT = 1;
    private final int CLASSIFY_TYPE_CAR = 2;
    private final int CLASSIFY_TYPE_DISH = 3;
    private final int CLASSIFY_TYPE_REDWINE = 4;
    private final int CLASSIFY_TYPE_LOGO = 5;
    private final int CLASSIFY_TYPE_ANIMAL = 6;
    private final int CLASSIFY_TYPE_INGREDIENT = 7;
    private final int CLASSIFY_TYPE_LANDMARK = 8;
    private final int CLASSIFY_TYPE_CURRENCY = 9;
//    private final int CLASSIFY_TYPE_VEHICLE = 10;
//    private final int CLASSIFY_TYPE_VEHICLE_DAMAGE = 11;

    private final Map<Integer, String> OBJECT_CLASSIFY_TYPE_HOST_MAP = new HashMap<Integer, String>() {
        {
            put(CLASSIFY_TYPE_ADVANCED_GENERAL, "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general");
            put(CLASSIFY_TYPE_DISH, "https://aip.baidubce.com/rest/2.0/image-classify/v2/dish");
            put(CLASSIFY_TYPE_LOGO, "https://aip.baidubce.com/rest/2.0/image-classify/v2/logo");
            put(CLASSIFY_TYPE_ANIMAL, "https://aip.baidubce.com/rest/2.0/image-classify/v1/animal");
            put(CLASSIFY_TYPE_PLANT, "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant");
            put(CLASSIFY_TYPE_INGREDIENT, "https://aip.baidubce.com/rest/2.0/image-classify/v1/classify/ingredient");
            put(CLASSIFY_TYPE_LANDMARK, "https://aip.baidubce.com/rest/2.0/image-classify/v1/landmark");
            put(CLASSIFY_TYPE_REDWINE, "https://aip.baidubce.com/rest/2.0/image-classify/v1/redwine");
            put(CLASSIFY_TYPE_CURRENCY, "https://aip.baidubce.com/rest/2.0/image-classify/v1/currency");
            put(CLASSIFY_TYPE_CAR, "https://aip.baidubce.com/rest/2.0/image-classify/v1/car");
//            put(CLASSIFY_TYPE_VEHICLE, "https://aip.baidubce.com/rest/2.0/image-classify/v1/vehicle_detect");
//            put(CLASSIFY_TYPE_VEHICLE_DAMAGE, "https://aip.baidubce.com/rest/2.0/image-classify/v1/vehicle_damage");
        }
    };

    private ExecutorService mClassifyExecutorService = Executors.newSingleThreadExecutor();
    private Future mClassifyTaskFuture;
    private ClassifyAdvancedGeneral mClassifyAdvancedGeneral;
    private ClassifyDish mClassifyDish;
    private ClassifyLogo mClassifyLogo;
    private ClassifyAnimalPlant mClassifyAnimal;
    private ClassifyAnimalPlant mClassifyPlant;
    private ClassifyIngredient mClassifyIngredient;
    private ClassifyLandMark mClassifyLandMark;
    private ClassifyRedWine mClassifyRedWine;
    private ClassifyCurrency mClassifyCurrency;
    private ClassifyCar mClassifyCar;

    public interface OnClassifyImageListener {
        void onError(String msg);

        void onFinalResult(String result, String description, boolean question);
    }

    public BaiduClassifyImageAI(Context context, OnClassifyImageListener listener) {
        mClassifyAdvancedGeneral = new ClassifyAdvancedGeneral(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_ADVANCED_GENERAL),
                "&baike_num=2");
        mClassifyDish = new ClassifyDish(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_DISH),
                "&top_num=2&baike_num=2");
        mClassifyLogo = new ClassifyLogo(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_LOGO),
                "&custom_lib=false");
        mClassifyAnimal = new ClassifyAnimalPlant(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_ANIMAL),
                "&top_num=2&baike_num=2");
        mClassifyPlant = new ClassifyAnimalPlant(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_PLANT),
                "&top_num=2&baike_num=2");
        mClassifyIngredient = new ClassifyIngredient(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_INGREDIENT),
                "&top_num=2");
        mClassifyLandMark = new ClassifyLandMark(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_LANDMARK), "");
        mClassifyRedWine = new ClassifyRedWine(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_REDWINE), "");
        mClassifyCurrency = new ClassifyCurrency(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_CURRENCY), "");
        mClassifyCar = new ClassifyCar(context, listener, OBJECT_CLASSIFY_TYPE_HOST_MAP.get(CLASSIFY_TYPE_CAR),
                "&top_num=2&baike_num=2");
    }

    public void initBaiduClassifyImage() {

    }

    public void releaseBaiduClassifyImage() {

    }

    public void classifyImageThread(final int type, final String imageFilePath, final boolean question) {
        if (mClassifyTaskFuture != null && !mClassifyTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mClassifyTaskFuture = mClassifyExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                classifyImage(type, imageFilePath, question);
            }
        });
    }

    private void classifyImage(int type, String imageFilePath, boolean question) {
        switch (type) {
            case CLASSIFY_TYPE_ADVANCED_GENERAL:
                mClassifyAdvancedGeneral.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_DISH:
                mClassifyDish.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_LOGO:
                mClassifyLogo.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_ANIMAL:
                mClassifyAnimal.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_PLANT:
                mClassifyPlant.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_INGREDIENT:
                mClassifyIngredient.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_LANDMARK:
                mClassifyLandMark.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_REDWINE:
                mClassifyRedWine.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_CURRENCY:
                mClassifyCurrency.classifyImage(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_CAR:
                mClassifyCar.classifyImage(imageFilePath, question);
                break;
//            case CLASSIFY_TYPE_VEHICLE:
//                break;
//            case CLASSIFY_TYPE_VEHICLE_DAMAGE:
//                break;
        }
    }
}
