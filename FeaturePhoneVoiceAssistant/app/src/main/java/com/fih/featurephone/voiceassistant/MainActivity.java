package com.fih.featurephone.voiceassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fih.featurephone.voiceassistant.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.camera.ImageCropActivity;
import com.fih.featurephone.voiceassistant.speechaction.WebSearchAction;
import com.fih.featurephone.voiceassistant.ui.Msg;
import com.fih.featurephone.voiceassistant.ui.MsgAdapter;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaiduAIActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private final int SETTING_REQUEST_CODE = 1000;
    private final int REQUEST_MULTIPLE_PERMISSION = 100;

    private final int TEXT_INPUT_SUB_UI_LEVER = 0x10;

    private List<Msg> mResultMsgList = new ArrayList<>();
    private ListView mResultMsgListView;
    private ProgressDialog mProgressDialog;
    private int mCropImageRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSupportTouch = CommonUtil.isSupportMultiTouch(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM));

        if (applyApkPermissions()) {
            initView();
            mActionHandler.post(new Runnable() {
                @Override
                public void run() {
                    initAssistant();//比较耗时，所以异步执行
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAssistant();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadCastReceiver);
    }

    @Override
    public void onGlobalLayout() {
        TextView debugTextView = findViewById(R.id.tv_debug_info);
        int displayHeight = CommonUtil.getDisplaySize(this).y - CommonUtil.getStatusBarHeight(this);
        int imageViewButtonHeight = findViewById(R.id.microphone_image_view).getLayoutParams().height;
        if (mSettingResult.mDebug) {
            debugTextView.getLayoutParams().height = displayHeight / 4;
            mResultMsgListView.getLayoutParams().height = displayHeight * 3/4 - imageViewButtonHeight;
        } else {
            mResultMsgListView.getLayoutParams().height = displayHeight - imageViewButtonHeight;
        }
        mResultMsgListView.setLayoutParams(mResultMsgListView.getLayoutParams());//及时生效
        mResultMsgListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initView() {
        mResultMsgListView = findViewById(R.id.msg_list_view);
        mResultMsgListView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        if (!mSettingResult.mDebug) {
            findViewById(R.id.tv_debug_info).setVisibility(View.GONE);
        } else {
            findViewById(R.id.tv_debug_info).setVisibility(View.VISIBLE);
            //和属性android:scrollbars="vertical"配合使用为了滚动
            ((TextView)findViewById(R.id.tv_debug_info)).setMovementMethod(ScrollingMovementMethod.getInstance());
        }

        MsgAdapter resultMsgAdapter = new MsgAdapter(MainActivity.this, R.layout.result_msglist_item, mResultMsgList);
        resultMsgAdapter.setHideHeadPic(true); //设置头像
        mResultMsgListView.setAdapter(resultMsgAdapter);
//        mResultMsgListView.setSelector(R.color.transparent);//设置条目没有选中背景@android:color/transparent
        mResultMsgListView.setSelector(R.color.item_selected);//设置条目选中背景颜色，不设置默认为黄色
        initMsg();
        initProgressDialog();
        if (mSupportTouch) {
            initTouchScreenView();
        }
    }

    private void initTouchScreenView() {
        findViewById(R.id.assistant_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.assistant_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowAssistantDialog();
            }
        });

        findViewById(R.id.manage_image_view).setVisibility(View.VISIBLE);
        findViewById(R.id.manage_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowManageDialog();
            }
        });
        findViewById(R.id.manage_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mSettingResult.mEnableExtraFun) onShowExtraManageDialog();
                return false;
            }
        });

        findViewById(R.id.microphone_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchVoiceAssistant();
            }
        });

        findViewById(R.id.left_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchTranslateLanguage(true);
            }
        });

        findViewById(R.id.right_arrow_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchTranslateLanguage(false);
            }
        });

        mResultMsgListView.setSelector(R.color.transparent);//设置条目没有选中背景@android:color/transparent
        mResultMsgListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onShowItemOptionDialog(view, position);
                return true;
            }
        });

        findViewById(R.id.input_text_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInputTextOver();
            }
        });
    }

    private void initMsg() {
        final String welcome;
        if (!mSupportTouch) {
            welcome = getString(R.string.baidu_unit_welcome);
        } else {
            welcome = getString(R.string.baidu_unit_welcome_support_touch);
        }
        Msg msgWelcome = new Msg(welcome, Msg.TYPE_RECEIVED_TEXT);
        mResultMsgList.add(msgWelcome);
/*
        mActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mSpeechEnable) startTTS(welcome);
            }
        }, 3000);
*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                relaunchApp();
                break;
            case IMAGE_SELECT_REQUEST_CODE:
//                SystemUtil.cropSelectImage(this, data.getData(), mCropImageRequestCode, CROP_IMAGE_FILE_PATH);
                final String CROP_IMAGE_FILE_PATH_PREFIX = FileUtils.getFaceTempImageDirectory().getAbsolutePath() + File.separator;
                Intent intent = new Intent(MainActivity.this, ImageCropActivity.class);
                switch (mCropImageRequestCode) {
                    case OCR_TEXT_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.OCR_TYPE);
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_ocr.jpg");
                        intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, mLastOCRLanguage);
                        break;
                    case FACE_IDENTIFY_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_face_identify.jpg");
                        intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.IDENTIFY_FACE_TYPE);
                        break;
                    case FACE_DETECT_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_face_detect.jpg");
                        intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.DETECT_FACE_TYPE);
                        break;
                    case FACE_AUTHENTICATE_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_face_authenticate.jpg");
                        intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.AUTHENTICATE_FACE_TYPE);
                        break;
                    case CLASSIFY_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_classify.jpg");
                        intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.CLASSIFY_IMAGE_TYPE);
                        intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, mLastClassifyImageType);
                        break;
                    case HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_gesture.jpg");
                        break;
                    case HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_headcount.jpg");
                        break;
                    case OCR_QRCODE_IMAGE_REQUEST_CODE:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_qrcode.jpg");
                        break;
                    default:
                        intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH_PREFIX  + "crop_image_main.jpg");
                }
                String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                startActivityForResult(intent, mCropImageRequestCode);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    // 请求权限
    public boolean applyApkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] requiredPermissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS};
            ArrayList<String> denyPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                denyPermissions.add(permission);
            }
            if (denyPermissions.size() > 0) {
                requestPermissions(denyPermissions.toArray(new String[0]), REQUEST_MULTIPLE_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        if (REQUEST_MULTIPLE_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    CommonUtil.toast(this, getString(R.string.permission_fail));
                    SystemClock.sleep(2000);
                    applyApkPermissions();
                    return;
                }
            }
            relaunchApp();
        }
    }


    private void relaunchApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
        }
        finish();
    }

    /*主界面下方按钮触发功能*/

    /*菜单功能选择*/
    private void onShowManageDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_translate_start));
        itemList.add(getString(R.string.option_ocr));
        itemList.add(getString(R.string.option_face_identify));
        itemList.add(getString(R.string.option_image_classify));
        itemList.add(getString(R.string.option_text_input));
        itemList.add(getString(R.string.option_clear_all_items));
        String[] items = itemList.toArray(new String[0]);

        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
            items[0] = getString(R.string.option_shortcut_translate_stop);
        }

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: //快捷翻译
                        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                            onSwitchTranslateLanguage("");//exit translate
                        } else {
                            onShowTranslateLanguageDialog();
                        }
                        break;
                    case 1: //OCR文字识别
                        if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                            onShowOCRInputDialog(mLastOCRLanguage);
                        } else {
                            onShowOCRLanguageDialog();
                        }
                        break;
                    case 2: //人脸识别
                        onShowFaceIdentifyInputDialog();
                        break;
                    case 3: //图像物体识别
                        onShowClassifyTypeDialog();
                        break;
                    case 4: //输入文本框
                        onSwitchInputText();
                        break;
                    case 5: //清除列表中所有显示
                        onClearAllItems();
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowTranslateLanguageDialog() {
        final String[] allItems = getResources().getStringArray(R.array.translate_action_language_keyword);
        final String[] items = Arrays.copyOfRange(allItems, 3, allItems.length);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
//                mLastTranslateLanguage = items[which]; // for feature phone
                onSwitchTranslateLanguage(items[which]);
            }
        });
        listDialog.show();
    }

    private void onShowOCRLanguageDialog() {
        final String[] items = getResources().getStringArray(R.array.ocr_language_item);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                onShowOCRInputDialog(items[which]);
            }
        });
        listDialog.show();
    }

    private void onShowOCRInputDialog(final String language) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
//                        mLastOCROptionMode = OCR_TEXT_CAMERA_REQUEST_CODE; //for feature phone
                        onOCRTextCameraActivity(language);
                        break;
                    case 1:
//                        mLastOCROptionMode = OCR_TEXT_IMAGE_REQUEST_CODE; //for feature phone
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);
                        mCropImageRequestCode = OCR_TEXT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        };

        onShowAlbumAndCameraOptionDialog(clickListener);
    }

    private void onOCRTextCameraActivity(String language) {
        mLastOCRLanguage = getAndShowMatchedOCRLanguage(language);

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.OCR_TYPE);
        intent.putExtra(GlobalValue.INTENT_OCR_LANGUAGE, mLastOCRLanguage);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_ocr_text.jpg");
        startActivityForResult(intent, OCR_TEXT_CAMERA_REQUEST_CODE);
    }

    private void onShowFaceIdentifyInputDialog() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        onFaceIdentifyCamera();
                        break;
                    case 1:
                        mCropImageRequestCode = FACE_IDENTIFY_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void onShowClassifyTypeDialog() {
        final String[] items = getResources().getStringArray(R.array.classify_image_type_item);
        ArrayList<String> selItemList = new ArrayList<>();
        for (Integer index : mSettingResult.mClassifyImageTypeList) {
            if (index < 0 || index >= items.length) continue;
            selItemList.add(items[index]);
        }

        final String[] selItems = selItemList.toArray(new String[0]);
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(selItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始, 需要转换为classify type
                int classifyType = mSettingResult.mClassifyImageTypeList.get(which);
                onShowClassifyImageInputDialog(classifyType);
            }
        });
        listDialog.show();
    }

    private void onShowClassifyImageInputDialog(final int classifyType) {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        onClassifyImageCameraActivity(classifyType);
                        break;
                    case 1:
                        mLastClassifyImageType = classifyType;
                        mCropImageRequestCode = CLASSIFY_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void onClassifyImageCameraActivity(int classifyType) {
        mLastClassifyImageType = classifyType;

        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, classifyType);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.CLASSIFY_IMAGE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_classify_image.jpg");
        startActivityForResult(intent, CLASSIFY_CAMERA_REQUEST_CODE);
    }

    private void onSwitchInputText() {
        EditText inputText = findViewById(R.id.input_text);
        if (inputText.getVisibility() == View.VISIBLE) {
            findViewById(R.id.input_text).setVisibility(View.GONE);
            if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.GONE);
            mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;
        } else {
            findViewById(R.id.input_text).setVisibility(View.VISIBLE);
            if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
            inputText.requestFocus();
            mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
        }
    }

    private void onClearAllItems() {
        mResultMsgList.clear();
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
    }

    /*额外菜单功能选择*/
    private void onShowExtraManageDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_face_detect));
        itemList.add(getString(R.string.option_face_compare));
        itemList.add(getString(R.string.option_human_body_gesture));
        itemList.add(getString(R.string.option_human_body_headcount));
        itemList.add(getString(R.string.option_face_merge));
        itemList.add(getString(R.string.option_face_authenticate));
        itemList.add(getString(R.string.option_show_face_manger));
        itemList.add(getString(R.string.option_ocr_qrcode));

        String[] items = itemList.toArray(new String[0]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: //额外功能，人脸检测
                        onShowFaceDetectInputDialog();
                        break;
                    case 1: //额外功能，人脸比较
                        onShowFaceCompare();
                        break;
                    case 2: //额外功能，手势识别
                        onShowHumanBodyGesture();
                        break;
                    case 3: //额外功能，人头清点
                        onShowHumanBodyHeadCount();
                        break;
                    case 4: //额外功能，人脸融合
                        onShowFaceMerge();
                        break;
                    case 5: //额外功能，身份认证，没权限需要企业认证！
                        onShowFaceAuthenticateInputDialog();
                        break;
                    case 6: //额外功能，人脸库管理
                        onShowFaceManager();
                        break;
                    case 7: //额外功能，二维码识别
                        onShowQRCodeInputDialog();
                        break;
                }
            }
        });
        listDialog.show();
    }

    void onShowFaceDetectInputDialog() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        onFaceDetectCamera();
                        break;
                    case 1:
                        mCropImageRequestCode = FACE_DETECT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void onFaceDetectCamera() {
        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.DETECT_FACE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_face_detect.jpg");
        startActivityForResult(intent, FACE_DETECT_CAMERA_REQUEST_CODE);
    }

    void onShowFaceAuthenticateInputDialog() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        onFaceAuthenticateCamera();
                        break;
                    case 1:
                        mCropImageRequestCode = FACE_AUTHENTICATE_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void onFaceAuthenticateCamera() {
        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.AUTHENTICATE_FACE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_face_authenticate.jpg");
        intent.putExtra(GlobalValue.INTENT_FACE_NAME, getString(R.string.baidu_face_authenticate_name_hint));
        intent.putExtra(GlobalValue.INTENT_FACE_ID_CARD_NUM, getString(R.string.baidu_face_authenticate_id_card_number_hint));
        startActivityForResult(intent, FACE_AUTHENTICATE_CAMERA_REQUEST_CODE);
    }

    private void onShowHumanBodyGesture() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.HUMAN_BODY_GESTURE_TYPE);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                                getFilesDir().getAbsolutePath() + File.separator + "camera_gesture.jpg");
                        startActivityForResult(intent, HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE);
                        break;
                    case 1:
                        mCropImageRequestCode = HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    private void onShowHumanBodyHeadCount() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.HUMAN_BODY_GESTURE_TYPE);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                                getFilesDir().getAbsolutePath() + File.separator + "camera_headcount.jpg");
                        startActivityForResult(intent, HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE);
                        break;
                    case 1:
                        mCropImageRequestCode = HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }

    void onShowQRCodeInputDialog() {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.OCR_TYPE);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                                getFilesDir().getAbsolutePath() + File.separator + "camera_ocr_qrcode.jpg");
                        startActivityForResult(intent, OCR_QRCODE_CAMERA_REQUEST_CODE);
                        break;
                    case 1:
                        mCropImageRequestCode = OCR_QRCODE_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(MainActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
    }


    /*系统帮助选项*/
    private void onShowAssistantDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_show_setting));
        itemList.add(getString(R.string.option_show_help));
        final String[] items = itemList.toArray(new String[0]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: // 系统设置
                        onShowSettings();
                        break;
                    case 1: // 显示帮助
                        onShowHelp();
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onShowHelp() {
        switch (mCurUILever) {
            case MAIN_UI_LEVER:
                startActivity(new Intent(MainActivity.this, HelpInfoActivity.class));
                break;
            case TRANSLATE_SUB_UI_LEVER:
                if (!mSupportTouch) {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help), Msg.TYPE_SEND_TEXT));
                } else {
                    mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help_support_touch), Msg.TYPE_SEND_TEXT));
                }
                break;
            case POEM_SUB_UI_LEVER:
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_poem_help), Msg.TYPE_SEND_TEXT));
                break;
            case COUPLET_SUB_UI_LEVER:
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_couplet_help), Msg.TYPE_SEND_TEXT));
                break;
        }
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    private void onShowSettings() {
        startActivityForResult(new Intent(MainActivity.this, SettingActivity.class), SETTING_REQUEST_CODE);
    }

    /*选中列表任一条目的功能选项*/
    private void onShowItemOptionDialog(final View view, final int position) {
        final ArrayList<String> itemList = new ArrayList<>();

        itemList.add(getString(R.string.option_query_again));
        itemList.add(getString(R.string.option_clear_item));
        if (view instanceof TextView) {
            itemList.add(getString(R.string.option_voice_output_text));
            itemList.add(getString(R.string.option_copy_edit_text));
            itemList.add(getString(R.string.option_web_query_text));
        } else {
            itemList.add(getString(R.string.option_save_image));
        }
        final String[] items = itemList.toArray(new String[0]);
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
//                mLastItemOptionFunction = which; //for feature phone
                Msg itemMsg = mResultMsgList.get(position);
                if (null == itemMsg) return;

                if (!TextUtils.isEmpty(itemMsg.getContent())) {
                    String itemText = itemMsg.getContent();//getSelectedItemText((TextView)view);
                    itemText = removeRedundancy(itemText);
                    if (!TextUtils.isEmpty(itemText)) {
                        doListItemTextOption(which, position, itemText);
                    }
                } else if (itemMsg.getImageBitmap() != null) {
                    Bitmap bitmap = itemMsg.getImageBitmap();
                    if (null != bitmap) {
                        doListItemBitmapOption(which, position, bitmap);
                    }
                }
            }
        });
        listDialog.show();
    }

    private void doListItemBitmapOption(int option, int position, Bitmap bitmap) {
        switch (option) {
            case 0: //重新提问
                triggerQuery(bitmap);
                break;
            case 1: //清除显示
                if (mSupportTouch) {
                    if (position >= 0) mResultMsgList.remove(position);
                } else {
                    mResultMsgList.remove(mResultMsgListView.getSelectedItemPosition());
                }
                ((MsgAdapter) mResultMsgListView.getAdapter()).notifyDataSetChanged();
                break;
            case 2: //保存图片
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", "");
                break;
        }
    }

    private String removeRedundancy(String text) {
        //remove botid name, xxx (xxx)
        if (!TextUtils.isEmpty(text) && text.lastIndexOf(")") == text.length() - 1) {
            int pos = text.lastIndexOf("(");
            if (pos >= 0) {
                text = text.substring(0, pos);
            }
        }

        return text;
    }

    private void doListItemTextOption(int option, int position, String text) {
        switch (option) {
            case 0://重新提问
                triggerQuery(text);
                break;
            case 1://清除显示
                if (mSupportTouch) {
                    if (position >= 0) mResultMsgList.remove(position);
                } else {
                    mResultMsgList.remove(mResultMsgListView.getSelectedItemPosition());
                }
                ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
                break;
            case 2://语音播报
                if (!mSpeechEnable) {// && (null != mBaiduTTSAI && !mBaiduTTSAI.isTTSRunning())) {
                    startTTS(text);
                }
                break;
            case 3://拷贝编辑
                copyAndEditMsgItemText(text);
                break;
            case 4://上网搜索
                WebSearchAction.getInstance(this).searchAction(text);
                break;
        }
    }

    private void onInputTextOver() {
        EditText inputText = findViewById(R.id.input_text);
        final String input = inputText.getText().toString();
        if (!TextUtils.isEmpty(input)) {
            triggerQuery(input);
        }
        findViewById(R.id.input_text).setVisibility(View.GONE);
        if (mSupportTouch) findViewById(R.id.input_text_send).setVisibility(View.GONE);

        mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;
        if (mSupportTouch) CommonUtil.hideSoftKeyboard(this);
    }


    private void copyAndEditMsgItemText(String text) {
        ClipboardManager clipboardManager =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (null != clipboardManager) {
            ClipData clipData = ClipData.newPlainText(null, text);
            clipboardManager.setPrimaryClip(clipData);
        }

        EditText inputText = findViewById(R.id.input_text);
        inputText.setVisibility(View.VISIBLE);
        findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
        inputText.setText(text);
        inputText.requestFocus();

        mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
    }

    private BroadcastReceiver mLocalBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA.equals(action)) {
                onOCRTextCameraActivity(intent.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE));
            }
        }
    };

    /*在UI界面上显示信息*/
    protected void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

    protected void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }

    protected void showDebugInfo(final String info, final boolean reset) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            subShowDebugInfo(info, reset);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    subShowDebugInfo(info, reset);
                }
            });
        }
    }

    protected void subShowDebugInfo(String info, boolean reset) {
        if (findViewById(R.id.tv_debug_info).getVisibility() != View.VISIBLE) {
            findViewById(R.id.tv_debug_info).setVisibility(View.VISIBLE);
        }

        TextView tv = findViewById(R.id.tv_debug_info);
        if (reset) tv.setText("");
        tv.append(info);

        //滚动到底部
        int offset = tv.getLineCount() * tv.getLineHeight();
        if(offset > tv.getHeight()){
            tv.scrollTo(0,offset - tv.getLayoutParams().height);
        }
    }

    protected void showFinalImageResponse(String imageFilePath, boolean reverse) {
        hideProgressDialog();

        if (reverse) {
            if (FileUtils.isFileExist(imageFilePath)) mResultMsgList.add(
                    new Msg(BitmapFactory.decodeFile(imageFilePath), Msg.TYPE_RECEIVED_IMAGE));
        } else {
            if (FileUtils.isFileExist(imageFilePath)) mResultMsgList.add(
                    new Msg(BitmapFactory.decodeFile(imageFilePath), Msg.TYPE_SEND_IMAGE));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showFinalTextResponse(String first, boolean reverse) {
        hideProgressDialog();
        if (reverse) {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_RECEIVED_TEXT));
        } else {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_SEND_TEXT));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showFinalTextResponse(String first, String second, boolean reverse) {
        hideProgressDialog();

        if (reverse) {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_RECEIVED_TEXT));
            if (!TextUtils.isEmpty(second)) mResultMsgList.add(new Msg(second, Msg.TYPE_SEND_TEXT));
        } else {
            if (!TextUtils.isEmpty(first)) mResultMsgList.add(new Msg(first, Msg.TYPE_SEND_TEXT));
            if (!TextUtils.isEmpty(second)) mResultMsgList.add(new Msg(second, Msg.TYPE_RECEIVED_TEXT));
        }

        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            mResultMsgList.add(new Msg(hint, Msg.TYPE_RECEIVED_TEXT));
            ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
            mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
        }
    }

    protected void showErrorMsg(final String msg) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            hideProgressDialog();
            mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED_TEXT));
            ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
            mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED_TEXT));
                    ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
                    mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
                }
            });
        }
    }

    private void onShowAlbumAndCameraOptionDialog(DialogInterface.OnClickListener listener) {
        final String[] items = {
                getString(R.string.option_camera),
                getString(R.string.option_image),
        };

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, listener);
        listDialog.show();
    }

    /*以下支持按键功能机*/
/*
    private int mLastItemOptionFunction = 0;
    private String mLastTranslateLanguage = TranslateFixAction.DEFAULT_TRANSLATE_TARGET_LANGUAGE;
    private int mLastOCROptionMode = OCR_TEXT_CAMERA_REQUEST_CODE;// OCR_TEXT_CAMERA_REQUEST_CODE: camera, OCR_TEXT_IMAGE_REQUEST_CODE: photo album

    private void focusListView() {
        mResultMsgListView.requestFocus();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) > 0) {
                            onInputTextOver();
                            return false;
                        } else {
                            onSwitchVoiceAssistant();
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) == 0) {
                            focusListView();
                        }
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) > 0) {
            onSwitchInputText();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    onSwitchInputText();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    onSwitchTranslateLanguage(true);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    onSwitchTranslateLanguage(false);
                    break;
                case KeyEvent.KEYCODE_1:
                case KeyEvent.KEYCODE_2:
                case KeyEvent.KEYCODE_3:
                case KeyEvent.KEYCODE_5:
                    event.startTracking();
                    if(event.getRepeatCount() == 0){
                        mShortPress = true;
                    }
                    return true;
//                case KeyEvent.KEYCODE_2:
//                    scrollDebugView(true);
//                    break;
//                case KeyEvent.KEYCODE_8:
//                    scrollDebugView(false);
//                    break;
                case KeyEvent.KEYCODE_STAR:
                    onShowHelp();
                    break;
                case KeyEvent.KEYCODE_POUND:
                    onShowSettings();
                    break;
            }
        } else {
            if (KeyEvent.KEYCODE_MENU == keyCode) {
                onSwitchInputText();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_5:
                if(mShortPress) {
                    Msg msg = (Msg)mResultMsgListView.getSelectedItem();
                    String itemText = null;
                    if (null != msg) {
                        itemText = removeRedundancy(msg.getContent());
                    }
                    if (TextUtils.isEmpty(itemText)) {
                        doListItemTextOption(mLastItemOptionFunction, -1, itemText);
                    }
                }
                //Don't handle long press here, because the user will have to get his finger back up first
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_3:
                if(mShortPress) {
                    if (OCR_TEXT_CAMERA_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRTextCameraActivity(mLastOCRLanguage);
                    } else if (OCR_TEXT_IMAGE_REQUEST_CODE == mLastOCROptionMode) {
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(mLastOCRLanguage);
                        mCropImageRequestCode = OCR_TEXT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(this, IMAGE_SELECT_REQUEST_CODE);
                    }
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_2:
                if(mShortPress) {
                    onFaceIdentifyCamera();
                }
                mShortPress = false;
                return true;
            case KeyEvent.KEYCODE_1:
                if(mShortPress) onSwitchTranslateLanguage(mLastTranslateLanguage);
                mShortPress = false;
                return true;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean mShortPress = false;
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_5:
                mShortPress = false;
                onShowItemOptionDialog(null, -1);
                return true;
            case KeyEvent.KEYCODE_3:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    if (OCR_TEXT_CAMERA_REQUEST_CODE == mLastOCROptionMode) {
                        onOCRTextCameraActivity(mLastOCRLanguage);
                    } else if (OCR_TEXT_IMAGE_REQUEST_CODE == mLastOCROptionMode) {
                        mLastOCRLanguage = getAndShowMatchedOCRLanguage(mLastOCRLanguage);
                        mCropImageRequestCode = OCR_TEXT_IMAGE_REQUEST_CODE;
                        SystemUtil.startSysAlbumActivity(this, IMAGE_SELECT_REQUEST_CODE);
                    }
                } else {
                    onShowOCRLanguageDialog();
                }
                return true;
            case KeyEvent.KEYCODE_2:
                mShortPress = false;
                onShowFaceManager();
                return true;
            case KeyEvent.KEYCODE_1:
                mShortPress = false;
                if (TRANSLATE_SUB_UI_LEVER == mCurUILever) {
                    onSwitchTranslateLanguage(mLastTranslateLanguage);
                } else {
                    onShowTranslateLanguageDialog();
                }
                return true;
            default:
                break;
        }
        //Just return false because the super call does always the same (returning false)
        return false;
    }*/
}
