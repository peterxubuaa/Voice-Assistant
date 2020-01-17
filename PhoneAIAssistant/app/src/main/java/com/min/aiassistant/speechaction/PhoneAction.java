package com.min.aiassistant.speechaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.min.aiassistant.R;
import com.min.aiassistant.baidu.unit.BaiduUnitAI;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.GlobalValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneAction implements BaseAction {
    private String[] REGEX_CALL;

    static private Map<String, ArrayList<String>> mContactInfoMap;
    private Context mContext;

    public PhoneAction(final Context context) {
        mContext = context;
        REGEX_CALL = mContext.getResources().getStringArray(R.array.phone_regex);
    }

    @Override
    public boolean checkAction(String query, BaiduUnitAI.BestResponse bestResponse) {
        query = CommonUtil.filterPunctuation(query);
        if (!CommonUtil.checkRegexMatch(query, REGEX_CALL)) return false;

        if (null == mContactInfoMap) {
            if (havePermissions(mContext)) {
                mContactInfoMap = getAllContactInfo(mContext);
            } else {
                Intent intent = new Intent(GlobalValue.LOCAL_BROADCAST_CONTACT_PERMISSION);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                return false;
            }
        }

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
        if (null == mContactInfoMap) return null;

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

    // 请求权限
    private boolean havePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] requiredPermissions = new String[]{
                    Manifest.permission.CALL_PHONE, //不是必须的敏感权限
                    Manifest.permission.READ_CONTACTS
            };
            ArrayList<String> denyPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                denyPermissions.add(permission);
            }
            return !(denyPermissions.size() > 0);
        }
        return true;
    }

    private HashMap<String, ArrayList<String>> getAllContactInfo(Context context) {
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        if (null == cursor) return null;

        HashMap<String, ArrayList<String>> contactInfoMap = new HashMap<>();
        //moveToNext方法返回的是一个boolean类型的数据
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) continue;

            ArrayList<String> phoneNumList = contactInfoMap.get(name);
            if (null == phoneNumList) {
                phoneNumList = new ArrayList<>();
            }
            phoneNumList.add(number);
            contactInfoMap.put(name, phoneNumList);
        }
        cursor.close();

        return contactInfoMap;
    }

    private void callTargetPhoneNumber(String targetPhoneNumber) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.CALL");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("tel:" + targetPhoneNumber));
        mContext.startActivity(intent);
    }
}
