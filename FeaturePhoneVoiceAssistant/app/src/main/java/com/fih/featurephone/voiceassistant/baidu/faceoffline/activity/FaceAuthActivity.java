package com.fih.featurephone.voiceassistant.baidu.faceoffline.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.idl.main.facesdk.FaceAuth;
import com.baidu.idl.main.facesdk.callback.Callback;
import com.baidu.idl.main.facesdk.utils.FileUitls;
import com.baidu.idl.main.facesdk.utils.PreferencesUtil;
import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.listener.SdkInitListener;
import com.fih.featurephone.voiceassistant.baidu.faceoffline.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

/**
 * 设备激活 （在线激活、离线激活）
 */
public class FaceAuthActivity extends Activity implements View.OnClickListener {
    private Context mContext;

    private TextView mTextViewDeviceID;
    private FaceAuth mFaceAuth;
    private int mLastKeyLen = 0;
    private EditText mEditTextKey;
    private enum ACTIVE_WAYS {
        ONLINE, OFFLINE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_face_auth);
        mContext = this;
        initView();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        // 复制按钮
        mFaceAuth = new FaceAuth();
        TextView copyText = findViewById(R.id.tv_copy_text);
        copyText.setOnClickListener(this);
        // device id
        mTextViewDeviceID = findViewById(R.id.tv_device_id);
        mTextViewDeviceID.setText(mFaceAuth.getDeviceId(this));
        // 输入序列码
        mEditTextKey = findViewById(R.id.et_key);
        mEditTextKey.setTransformationMethod(new AllUpperCaseTransformationMethod());
        addEditTextKeyListener();

        final String licenseOnLineKey = PreferencesUtil.getString("activate_online_key", "");
        if (TextUtils.isEmpty(licenseOnLineKey)) { //temp for license
            if (CommonUtil.isSupportMultiTouch(this)) {
                mEditTextKey.setText(getString(R.string.baidu_face_authentic_key1));
            } else {
                mEditTextKey.setText(getString(R.string.baidu_face_authentic_key2));
            }
        } else {
            mEditTextKey.setText(licenseOnLineKey);
        }

        // 在线激活按钮
        Button btOnLineActive = findViewById(R.id.bt_online_active);
        btOnLineActive.setOnClickListener(this);
        // 检查文件按钮
        Button btInspectSdcard = findViewById(R.id.bt_detect_sdcard);
        btInspectSdcard.setOnClickListener(this);
        // 离线激活按钮
        Button btOffLineActive = findViewById(R.id.bt_off_line_active);
        btOffLineActive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 点击复制
            case R.id.tv_copy_text:
                ClipboardManager clipboardManager = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                if (null != clipboardManager) {
                    ClipData clipData = ClipData.newPlainText(null, mTextViewDeviceID.getText());
                    clipboardManager.setPrimaryClip(clipData);
                }
                CommonUtil.toast(this, "deviceID 复制成功");
                break;
            // 在线激活
            case R.id.bt_online_active:
                initLicense(ACTIVE_WAYS.ONLINE);
                break;
            // 查看sdcard
            case R.id.bt_detect_sdcard:
                String path = FileUitls.getSDPath();
                String sdCardDir = path + "/" + "License.zip";
                if (FileUitls.fileIsExists(sdCardDir)) {
                    CommonUtil.toast(this, "读取到License.zip文件，文件地址为：" + sdCardDir);
                } else {
                    CommonUtil.toast(this, "未查找到License.zip文件");
                }
                break;
            // 离线激活
            case R.id.bt_off_line_active:
                initLicense(ACTIVE_WAYS.OFFLINE);
                break;
            default:
                break;
        }
    }

    private void initLicense(ACTIVE_WAYS activeWay) {
        if (ACTIVE_WAYS.ONLINE == activeWay) {
            String key = mEditTextKey.getText().toString().trim().toUpperCase();
            if (TextUtils.isEmpty(key)) {
                CommonUtil.toast(this, "请输入激活序列号!");
                return;
            }
            mFaceAuth.initLicenseOnLine(this, key, mLicenseCallback);
        } else if (ACTIVE_WAYS.OFFLINE == activeWay){
            mFaceAuth.initLicenseOffLine(this, mLicenseCallback);
        } else {
            CommonUtil.toast(this, "激活类别没有!");
        }
    }

    private Callback mLicenseCallback = new Callback() {
        @Override
        public void onResponse(final int code, final String response) {
            if (code == 0) {
                FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                    @Override
                    public void initStart() {}
                    @Override
                    public void initLicenseSuccess() {}
                    @Override
                    public void initLicenseFail(int errorCode, String msg) {
                        CommonUtil.toast(FaceAuthActivity.this, errorCode + msg);
                    }
                    @Override
                    public void initModelSuccess() {}
                    @Override
                    public void initModelFail(int errorCode, String msg) {}
                });
                CommonUtil.toast(FaceAuthActivity.this, "激活成功");
                finish();
            } else {
                CommonUtil.toast(FaceAuthActivity.this, "激活失败：" + response + " (" + code +")");
            }
        }
    };

    private void addEditTextKeyListener() {
        mEditTextKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                final int KEY_MAX_LENGTH = 19;
                final int LINE_THROUGH_POS = 5;
                final String LINE_THROUGH = "-";
                if (s.toString().length() > KEY_MAX_LENGTH) {
                    mEditTextKey.setText(s.toString().substring(0, KEY_MAX_LENGTH));
                    mEditTextKey.setSelection(mEditTextKey.getText().length());
                    mLastKeyLen = s.length();
                    return;
                }
                if (s.toString().length() < mLastKeyLen) {
                    mLastKeyLen = s.length();
                    return;
                }
                String text = s.toString().trim();
                if (mEditTextKey.getSelectionStart() < text.length()) {
                    return;
                }
                if ((text.length() + 1) % LINE_THROUGH_POS == 0) {
                    mEditTextKey.setText(text + LINE_THROUGH);
                    mEditTextKey.setSelection(mEditTextKey.getText().length());
                }

                mLastKeyLen = s.length();
            }
        });
    }

    public static class AllUpperCaseTransformationMethod extends ReplacementTransformationMethod {
        private final char[] LOWER_CASE = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
                'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        private final char[] UPPER_CASE = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

        @Override
        protected char[] getOriginal() {
            return LOWER_CASE;
       }

        @Override
        protected char[] getReplacement() {
            return UPPER_CASE;
        }
    }
}
