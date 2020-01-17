package com.min.aiassistant.baidu.imageclassify;

import android.content.Context;

import com.min.aiassistant.baidu.BaiduBaseAI;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyAdvancedGeneral;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyAnimalPlant;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyCar;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyCurrency;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyDish;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyIngredient;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyLandMark;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyLogo;
import com.min.aiassistant.baidu.imageclassify.model.ClassifyRedWine;

//https://ai.baidu.com/docs#/ImageClassify-API/ebc492b1
public class BaiduClassifyImageAI extends BaiduBaseAI {
    public static final int CLASSIFY_ACTION = 1;
    public static final int CLASSIFY_QUESTION_ACTION = 2;
    public static final int CLASSIFY_TYPE_DEFAULT = 0;

    //必须和R.array.classify_image_type_item的顺序一致
    public static final int CLASSIFY_TYPE_ADVANCED_GENERAL = 0;
    public static final int CLASSIFY_TYPE_PLANT = 1;
    public static final int CLASSIFY_TYPE_CAR = 2;
    public static final int CLASSIFY_TYPE_DISH = 3;
    public static final int CLASSIFY_TYPE_RED_WINE = 4;
    public static final int CLASSIFY_TYPE_LOGO = 5;
    public static final int CLASSIFY_TYPE_ANIMAL = 6;
    public static final int CLASSIFY_TYPE_INGREDIENT = 7;
    public static final int CLASSIFY_TYPE_LANDMARK = 8;
    public static final int CLASSIFY_TYPE_CURRENCY = 9;
//    final int CLASSIFY_TYPE_VEHICLE = 10;
//    final int CLASSIFY_TYPE_VEHICLE_DAMAGE = 11;

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

    public BaiduClassifyImageAI(Context context, IBaiduBaseListener listener) {
        mClassifyAdvancedGeneral = new ClassifyAdvancedGeneral(context, listener);
        mClassifyDish = new ClassifyDish(context, listener);
        mClassifyLogo = new ClassifyLogo(context, listener);
        mClassifyAnimal = new ClassifyAnimalPlant(context, listener, ClassifyAnimalPlant.ANIMAL);
        mClassifyPlant = new ClassifyAnimalPlant(context, listener, ClassifyAnimalPlant.PLANT);
        mClassifyIngredient = new ClassifyIngredient(context, listener);
        mClassifyLandMark = new ClassifyLandMark(context, listener);
        mClassifyRedWine = new ClassifyRedWine(context, listener);
        mClassifyCurrency = new ClassifyCurrency(context, listener);
        mClassifyCar = new ClassifyCar(context, listener);
    }

    public void action(final int type, final String imageFilePath, final boolean question) {
        new Thread() {
            @Override
            public void run() {
                classifyImage(type, imageFilePath, question);
            }
        }.start();
    }

    private void classifyImage(int type, String imageFilePath, boolean question) {

        switch (type) {
            case CLASSIFY_TYPE_ADVANCED_GENERAL:
                mClassifyAdvancedGeneral.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_DISH:
                mClassifyDish.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_LOGO:
                mClassifyLogo.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_ANIMAL:
                mClassifyAnimal.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_PLANT:
                mClassifyPlant.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_INGREDIENT:
                mClassifyIngredient.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_LANDMARK:
                mClassifyLandMark.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_RED_WINE:
                mClassifyRedWine.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_CURRENCY:
                mClassifyCurrency.request(imageFilePath, question);
                break;
            case CLASSIFY_TYPE_CAR:
                mClassifyCar.request(imageFilePath, question);
                break;
//            case CLASSIFY_TYPE_VEHICLE:
//                break;
//            case CLASSIFY_TYPE_VEHICLE_DAMAGE:
//                break;
        }
    }
}
