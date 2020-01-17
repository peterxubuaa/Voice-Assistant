package com.min.aiassistant.baidu.contentapprove.parsejson;

import android.text.TextUtils;

import com.min.aiassistant.baidu.BaiduParseBaseJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ParsePublicFigureJson extends BaiduParseBaseJson {

    private static ParsePublicFigureJson sParseHeadcountJson = null;

    public static ParsePublicFigureJson getInstance() {
        if (null == sParseHeadcountJson) {
            sParseHeadcountJson = new ParsePublicFigureJson();
        }
        return sParseHeadcountJson;
    }

    public class PublicFigure extends BaiduParseBaseResponse {
//        String mConclusion;//审核结果，可取值描述：合规、不合规、疑似、审核失败
        public int mConclusionType;//审核结果类型，可取值1、2、3、4，分别代表1：合规，2：不合规，3：疑似，4：审核失败
        public ArrayList<Data> mDataList;//不合规/疑似/命中白名单项详细信息。响应成功并且conclusion为疑似或不合规或命中白名单时才返回，响应失败或conclusion为合规且未命中白名单时不返回
    }

    public class Data {
        String mMsg;//不合规项描述信息
        String mConclusion;//审核结果，可取值描述：合规、不合规、疑似、审核失败
        double mProbability; //不合规项置信度 0~1
        public int mType;//结果具体命中的模型：5：政治敏感识别
        int mSubType; //审核子类型，此字段需参照type主类型字段决定其含义：当type=5时subType取值含义：0:政治敏感、1:公众人物、2:自定义敏感人物
        int mConclusionType; //审核结果类型，可取值1、2、3、4，分别代表1：合规，2：不合规，3：疑似，4：审核失败
        public ArrayList<Stars> mStarsList;
    }

    public class Stars {
        public double mProbability; //不合规项置信度 0~1
        public String mName;//敏感人物名称
        double mDatasetName;//人脸所属数据集名称
    }

    public PublicFigure parse(String result) {
        if (TextUtils.isEmpty(result)) return null;

        PublicFigure publicFigure = new PublicFigure();
        try {
            JSONObject jsonObject = new JSONObject(result);
            baseParse(jsonObject, publicFigure);
            if (!jsonObject.isNull("conclusionType")) {
                publicFigure.mConclusionType = jsonObject.getInt("conclusionType");
            }
            if (!jsonObject.isNull("data")) {
                publicFigure.mDataList = parseData(jsonObject.getJSONArray("data"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return publicFigure;
    }

    private ArrayList<Data> parseData(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Data> dataList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Data data = new Data();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("msg")) {
                    data.mMsg = jsonObject.getString("msg");
                }
                if (!jsonObject.isNull("conclusion")) {
                    data.mConclusion = jsonObject.getString("conclusion");
                }
                if (!jsonObject.isNull("conclusionType")) {
                    data.mConclusionType = jsonObject.getInt("conclusionType");
                }
                if (!jsonObject.isNull("probability")) {
                    data.mProbability = jsonObject.getDouble("probability");
                }
                if (!jsonObject.isNull("type")) {
                    data.mType = jsonObject.getInt("type");
                }
                if (!jsonObject.isNull("subType")) {
                    data.mSubType = jsonObject.getInt("subType");
                }
                if (!jsonObject.isNull("stars")) {
                    data.mStarsList = parseStars(jsonObject.getJSONArray("stars"));
                }

                dataList.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    private ArrayList<Stars> parseStars(JSONArray jsonArray) {
        if (null == jsonArray) return null;

        ArrayList<Stars> starsList = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                Stars stars = new Stars();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("probability")) {
                    stars.mProbability = jsonObject.getDouble("probability");
                }
                if (!jsonObject.isNull("name")) {
                    stars.mName = jsonObject.getString("name");
                }
                if (!jsonObject.isNull("datasetName")) {
                    stars.mDatasetName = jsonObject.getDouble("datasetName");
                }

                starsList.add(stars);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return starsList;
    }
}
