package com.fih.featurephone.voiceassistant.baidu.humanbody.model;

import android.content.Context;
import android.graphics.Rect;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.humanbody.BaiduHumanBodyAI;
import com.fih.featurephone.voiceassistant.baidu.humanbody.parsejson.ParseGestureJson;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*序号	手势名称	classname
1	数字1（原食指）	One
2	数字5（原掌心向前）	Five
3	拳头	Fist
4	OK	OK
5	祈祷	Prayer
6	作揖	Congratulation
7	作别	Honour
8	单手比心	Heart_single
9	点赞	Thumb_up
10	Diss	Thumb_down
11	我爱你	ILY
12	掌心向上	Palm_up
13	双手比心1	Heart_1
14	双手比心2	Heart_2
15	双手比心3	Heart_3
16	数字2	Two
17	数字3	Three
18	数字4	Four
19	数字6	Six
20	数字7	Seven
21	数字8	Eight
22	数字9	Nine
23	Rock	Rock
24	竖中指	Insult*/

public class Gesture extends BaiduBaseModel<ParseGestureJson.Gesture> {

    private final Map<String, String> GESTURE_MAP = new HashMap<String, String>() {
        {
            put("One", "数字1");
            put("Two", "数字2");
            put("Three", "数字3");
            put("Four", "数字4");
            put("Five", "数字5");
            put("Six", "数字6");
            put("Seven", "数字7");
            put("Eight", "数字8");
            put("Nine", "数字9");
            put("Fist", "拳头");
            put("Ok", "OK确定");
            put("Prayer", "祈祷");
            put("Congratulation", "作揖");
            put("Honour", "作别");
            put("Heart_single", "单手比心");
            put("Thumb_up", "点赞");
            put("Thumb_down", "侮辱");
            put("ILY", "我爱你");
            put("Palm_up", "掌心向上");
            put("Heart_1", "双手比心(手腕相对)");
            put("Heart_2", "双手比心(尖朝上)");
            put("Heart_3", "双手比心(尖朝下)");
            put("Rock", "牛角");
            put("Insult", "竖中指");
            put("Other", "其他手势");
        }
    };

    public Gesture(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = STRING_PARAM_TYPE;
        mHostURL = "https://aip.baidubce.com/rest/2.0/image-classify/v1/gesture";
    }

    protected ParseGestureJson.Gesture parseJson(String json) {
        return ParseGestureJson.getInstance().parse(json);
    }

    protected void handleResult(ParseGestureJson.Gesture gesture) {
        if (null == gesture.mResultList || gesture.mResultList.size() == 0) {
            mBaiduBaseListener.onFinalResult(null, BaiduHumanBodyAI.GESTURE_ACTION);
            return;
        }

        ParseGestureJson.Result gestureResult = null;
        if (gesture.mResultList.size() == 1) { //只有手势
            if (!gesture.mResultList.get(0).mClassName.equals("Face")) {// 不等于人脸
                gestureResult = gesture.mResultList.get(0);
            }
        } else if (gesture.mResultList.size() == 2) {//有人脸和手势
            if (!gesture.mResultList.get(0).mClassName.equals("Face")) {
                gestureResult = gesture.mResultList.get(0);
            } else if (!gesture.mResultList.get(1).mClassName.equals("Face")) {
                gestureResult = gesture.mResultList.get(1);
            }
        }

        if (null != gestureResult) {
            if (gestureResult.mProbability < GESTURE_THRESHOLD) {
                mBaiduBaseListener.onFinalResult("手势置信度太低", BaiduHumanBodyAI.GESTURE_ACTION);
            } else {
                mBaiduBaseListener.onFinalResult(GESTURE_MAP.get(gestureResult.mClassName), BaiduHumanBodyAI.GESTURE_ACTION);

                Rect cropRect = new Rect(gestureResult.mLeft, gestureResult.mTop,
                        gestureResult.mLeft + gestureResult.mWidth, gestureResult.mTop + gestureResult.mHeight);
                String gestureImageFilePath = mContext.getFilesDir() + File.separator + "human_body_gesture.jpg";
                if (FileUtils.isFileExist(gestureImageFilePath)) FileUtils.deleteFile(gestureImageFilePath);
                BitmapUtils.saveCropJpeg(mImageFilePath, cropRect, gestureImageFilePath);
                mBaiduBaseListener.onFinalResult(gestureImageFilePath, BaiduHumanBodyAI.GESTURE_IMAGE_ACTION);
            }
        } else {
            mBaiduBaseListener.onFinalResult(null, BaiduHumanBodyAI.GESTURE_ACTION);
        }
    }
}
