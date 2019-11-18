package com.fih.featurephone.voiceassistant.speechaction;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.unit.BaiduUnitAI;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneAction implements BaseAction {
    private String[] REGEX_CALL;

    static private Map<String, ArrayList<String>> mContactInfoMap;
    private Context mContext;

    public PhoneAction(Context context) {
        mContext = context;
        REGEX_CALL = mContext.getResources().getStringArray(R.array.phone_regex);
        if (null == mContactInfoMap) {
            getAllContactInfo(context);
        }
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!CommonUtil.checkRegexMatch(query, REGEX_CALL)) return false;

        String phoneNumber = haveTargetPhoneNumber(query);
        if (!TextUtils.isEmpty(phoneNumber)) {
            callTargetPhoneNumber(phoneNumber);
            bestResponse.reset();
            bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_phone_dialing) + phoneNumber;
        } else {
            String[] result = seekTargetPhoneNumber(query);
            if (null != result && result.length == 2) {
                phoneNumber = result[1];
                callTargetPhoneNumber(phoneNumber);
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_phone_dialing) + result[0];
            } else {
                bestResponse.reset();
                bestResponse.mAnswer = mContext.getString(R.string.baidu_unit_hint_phone_fail);
            }
        }
        return true;
    }

    private String haveTargetPhoneNumber(String query) {
//        return CommonUtil.getRegexMatch(query, new String[]{"[^\\d]*(\\d+)[^\\d]*"}, 1);
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    private String[] seekTargetPhoneNumber(String query) {
        for (String key : mContactInfoMap.keySet()) {
            if (query.contains(key)) {
                ArrayList<String> phoneNumList = mContactInfoMap.get(key);
                if (null != phoneNumList && phoneNumList.size() > 0) {
                    return new String[]{key, phoneNumList.get(0)};
                }
            }
        }
        return null;
    }

    private void getAllContactInfo(Context context) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        if (null == cursor) return;

        mContactInfoMap = new HashMap<String, ArrayList<String>>();
        //moveToNext方法返回的是一个boolean类型的数据
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) continue;

            ArrayList<String> phoneNumList = mContactInfoMap.get(name);
            if (null == phoneNumList) {
                phoneNumList = new ArrayList<String>();
            }
            phoneNumList.add(number);
            mContactInfoMap.put(name, phoneNumList);
        }
        cursor.close();
    }

    private void callTargetPhoneNumber(String targetPhoneNumber) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("tel:" + targetPhoneNumber));
        mContext.startActivity(intent);
    }

//    public void action(String event, String targetName, String targetPhoneNumber) {
//        if (!TextUtils.isEmpty(targetName)) {
//            if (callTargetName(targetName)) return;
//        }
//
//        if (!TextUtils.isEmpty(targetPhoneNumber)) {
//            callTargetPhoneNumber(targetPhoneNumber);
//        }
//    }
//
//    private boolean callTargetName(String targetName) {
//        for (String key : mContactInfoMap.keySet()) {
//            if (targetName.contains(key) || key.contains(targetName)) {
//                ArrayList<String> phoneNumList = mContactInfoMap.get(key);
//                if (null != phoneNumList && phoneNumList.size() > 0) {
//                    String phoneNumber = phoneNumList.get(0);
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_CALL); //Intent.ACTION_DIAL
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.setData(Uri.parse("tel:" + phoneNumber));
//                    mContext.startActivity(intent);
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
}
