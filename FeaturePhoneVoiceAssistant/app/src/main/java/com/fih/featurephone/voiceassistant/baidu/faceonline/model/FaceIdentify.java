package com.fih.featurephone.voiceassistant.baidu.faceonline.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseModel;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.parsejson.ParseFaceIdentifyJson;
import com.fih.featurephone.voiceassistant.utils.BitmapUtils;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;

import java.io.File;

//https://ai.baidu.com/ai-doc/FACE/Gk37c1uzc
public class FaceIdentify extends BaiduBaseModel<ParseFaceIdentifyJson.FaceIdentify> {
    public static final int FACE_IDENTIFY_1_FROM_N = 1;
    public static final int FACE_IDENTIFY_M_FROM_N = 2;

    private int mType;

    public FaceIdentify(Context context, BaiduBaseAI.IBaiduBaseListener listener) {
        super(context, listener);
        mURLRequestParamType = MAP_PARAM_TYPE;
    }

    public void request(String imageFilePath, boolean question, int type) {
        mType = type;
        switch (type) {
            case FACE_IDENTIFY_1_FROM_N:
                mHostURL = "https://aip.baidubce.com/rest/2.0/face/v3/search";
                mURLRequestParamMap.put("group_id_list", GlobalValue.FACE_DEFAULT_GROUP_ID);//从指定的group中进行查找 用逗号分隔，上限10个
                mURLRequestParamMap.put("liveness_control", "NONE");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
                mURLRequestParamMap.put("quality_control", "NORMAL");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
                //mURLRequestParamMap.put("user_id", "");//当需要对特定用户进行比对时，指定user_id进行比对。即人脸认证功能。
                mURLRequestParamMap.put("max_user_num", 1);//查找后返回的用户数量。返回相似度最高的几个用户，默认为1，最多返回50个
                break;
            case FACE_IDENTIFY_M_FROM_N:
                mHostURL = "https://aip.baidubce.com/rest/2.0/face/v3/multi-search";
                mURLRequestParamMap.put("group_id_list", GlobalValue.FACE_DEFAULT_GROUP_ID);//从指定的group中进行查找 用逗号分隔，上限10个
                mURLRequestParamMap.put("max_face_num", 10);//最多处理人脸的数目, 默认值为1(仅检测图片中面积最大的那个人脸) 最大值10
                mURLRequestParamMap.put("match_threshold", FACE_IDENTIFY_THRESHOLD);//匹配阈值（设置阈值后，score低于此阈值的用户信息将不会返回） 最大100 最小0 默认80, 此阈值设置得越高，检索速度将会越快，推荐使用默认阈值80
                mURLRequestParamMap.put("liveness_control", "NONE");//图片质量控制,NONE: 不进行控制, LOW:较低的质量要求, NORMAL: 一般的质量要求, HIGH: 较高的质量要求
                mURLRequestParamMap.put("quality_control", "NORMAL");//活体检测控制,NONE: 不进行控制; LOW:较低的活体要求(高通过率 低攻击拒绝率); NORMAL: 一般的活体要求(平衡的攻击拒绝率, 通过率); HIGH: 较高的活体要求(高攻击拒绝率 低通过率); 默认NONE; 若活体检测结果不满足要求，则返回结果中会提示活体检测失败
                mURLRequestParamMap.put("max_user_num", 1);//识别返回的最大用户数，默认为1，最大20个
                break;
        }

        super.request(imageFilePath, question);
    }

    protected ParseFaceIdentifyJson.FaceIdentify parseJson(String json) {
        return ParseFaceIdentifyJson.getInstance().parse(json);
    }

    protected void handleResult(ParseFaceIdentifyJson.FaceIdentify faceIdentify) {
        final int MIN_FACE_PIX = 100;
        switch (mType) {
            case FACE_IDENTIFY_1_FROM_N:
                if (null == faceIdentify.mResult
                        || null == faceIdentify.mResult.mFaceList
                        || faceIdentify.mResult.mFaceList.size() == 0
                        || null == faceIdentify.mResult.mFaceList.get(0).mUserList
                        || faceIdentify.mResult.mFaceList.get(0).mUserList.size() == 0
                        || faceIdentify.mResult.mFaceList.get(0).mUserList.get(0).mScore < FACE_IDENTIFY_THRESHOLD) {
                    mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);
                    return;
                }
                mBaiduBaseListener.onFinalResult(faceIdentify.mResult.mFaceList.get(0).mUserList.get(0).mUserInfo,
                        mQuestion ? BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION : BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);
                break;
            case FACE_IDENTIFY_M_FROM_N:
                if (null == faceIdentify.mResult
                        || null == faceIdentify.mResult.mFaceList
                        || faceIdentify.mResult.mFaceList.size() == 0) {
                    mBaiduBaseListener.onFinalResult(null, BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);
                    return;
                }
                String faceBaseImageFilePath = mContext.getFilesDir() + File.separator + "face_identify_";
                int index = 1;
                Bitmap inputBitmap = BitmapFactory.decodeFile(mImageFilePath);
                for (ParseFaceIdentifyJson.Face face : faceIdentify.mResult.mFaceList) {
                    if (face.mUserList == null || face.mUserList.size() == 0) continue;
                    ParseFaceIdentifyJson.User user = face.mUserList.get(0); //目前只返回最大匹配的用户
                    if (user.mScore < FACE_IDENTIFY_THRESHOLD) continue;

                    mBaiduBaseListener.onFinalResult(user.mUserInfo, mQuestion?
                                BaiduFaceOnlineAI.FACE_IDENTIFY_QUESTION_ACTION : BaiduFaceOnlineAI.FACE_IDENTIFY_ACTION);

                    Rect cropRect = new Rect((int)face.mLocationF.mLeft, (int)face.mLocationF.mTop,
                            (int)face.mLocationF.mLeft + face.mLocationF.mWidth, (int)face.mLocationF.mTop + face.mLocationF.mHeight);
                    String faceImageFilePath = faceBaseImageFilePath + index + ".jpg";
                    index++;
                    if (FileUtils.isFileExist(faceImageFilePath)) FileUtils.deleteFile(faceImageFilePath);
                    BitmapUtils.saveCropJpeg(inputBitmap, cropRect, faceImageFilePath);
                    if (cropRect.width() < MIN_FACE_PIX) {//扩大人脸尺寸
                        int width = MIN_FACE_PIX;
                        int height = MIN_FACE_PIX * cropRect.height() / cropRect.width();
                        BitmapUtils.resizeJpegFile(faceImageFilePath, width, height, faceImageFilePath);
                    }
                    mBaiduBaseListener.onFinalResult(faceImageFilePath, BaiduFaceOnlineAI.FACE_IDENTIFY_IMAGE_ACTION);
                }
                break;
        }
    }
}
