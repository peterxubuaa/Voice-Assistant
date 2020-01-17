package com.min.aiassistant;

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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.min.aiassistant.baidu.nlp.BaiduNLPAI;
import com.min.aiassistant.baidu.speech.BaiduSpeechAI;
import com.min.aiassistant.picture.CameraCaptureActivity;
import com.min.aiassistant.picture.ImageCropActivity;
import com.min.aiassistant.speechaction.WebSearchAction;
import com.min.aiassistant.ui.Msg;
import com.min.aiassistant.ui.MsgAdapter;
import com.min.aiassistant.utils.CommonUtil;
import com.min.aiassistant.utils.FileUtils;
import com.min.aiassistant.utils.GlobalValue;
import com.min.aiassistant.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaiduAIActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private final int SETTING_REQUEST_CODE = 1000;
    private final int REQUEST_NECESSARY_PERMISSION = 100;
    private final int REQUEST_CONTACT_PERMISSION = 200;
    private final int REQUEST_INSTALL_PACKAGES_PERMISSION = 300;

    private List<Msg> mResultMsgList = new ArrayList<>();
    private ListView mResultMsgListView;
    private ProgressDialog mProgressDialog;
    private int mCropImageRequestCode;
    private boolean mInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (applyNecessaryPermissions()) {
            initView();
            initLocalBroadCast();
            mActionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initAssistant();//比较耗时，所以异步执行
                    mInitialized = true;
                    showHint(getString(R.string.baidu_unit_welcome_support_touch));
                }
            }, 200);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInitialized) releaseAssistant();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadCastReceiver);
    }

    @Override
    public void onGlobalLayout() {
        int displayHeight = CommonUtil.getDisplaySize(this).y - CommonUtil.getStatusBarHeight(this);
        int imageViewButtonHeight = findViewById(R.id.microphone_image_view).getLayoutParams().height;
        mResultMsgListView.getLayoutParams().height = displayHeight - imageViewButtonHeight;
        mResultMsgListView.setLayoutParams(mResultMsgListView.getLayoutParams());//及时生效
        mResultMsgListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    private void initView() {
        mResultMsgListView = findViewById(R.id.msg_list_view);
        mResultMsgListView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        MsgAdapter resultMsgAdapter = new MsgAdapter(MainActivity.this, R.layout.result_msglist_item, mResultMsgList);
        resultMsgAdapter.setHideHeadPic(true); //设置头像
        mResultMsgListView.setAdapter(resultMsgAdapter);
//        mResultMsgListView.setSelector(R.color.transparent);//设置条目没有选中背景@android:color/transparent
        mResultMsgListView.setSelector(R.color.item_selected);//设置条目选中背景颜色，不设置默认为黄色

        initProgressDialog();
        initTouchScreenView();
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
                return true;
            }
        });

        findViewById(R.id.microphone_image_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchVoiceAssistant(true);
            }
        });
        findViewById(R.id.microphone_image_view).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CommonUtil.toast(MainActivity.this, getString(R.string.voice_input));
                onSwitchVoiceAssistant(false);
                return true;
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
                onInputTextOver(false);
            }
        });
        findViewById(R.id.input_text_send).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onInputTextOver(true);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        switch (requestCode) {
            case SETTING_REQUEST_CODE:
                relaunchApp();
                break;
            case IMAGE_SELECT_REQUEST_CODE:
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
                if (null != data) {
                    String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                    intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                    startActivityForResult(intent, mCropImageRequestCode);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    // 请求权限
    public boolean applyNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] requiredPermissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
            };
            ArrayList<String> denyPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                denyPermissions.add(permission);
            }
            if (denyPermissions.size() > 0) {
                requestPermissions(denyPermissions.toArray(new String[0]), REQUEST_NECESSARY_PERMISSION);
                return false;
            }
        }
        return true;
    }

    // 请求Contact权限
    public void applyContactPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            final String[] requiredPermissions = new String[]{
                    Manifest.permission.CALL_PHONE, //不是必须的敏感权限
                    Manifest.permission.READ_CONTACTS
            };
            ArrayList<String> denyPermissions = new ArrayList<>();
            for (String permission : requiredPermissions) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                    continue;
                denyPermissions.add(permission);
            }
            if (denyPermissions.size() > 0) {
                requestPermissions(denyPermissions.toArray(new String[0]), REQUEST_CONTACT_PERMISSION);
            }
        }
    }

    public void applyReadInstalledPkgPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, REQUEST_INSTALL_PACKAGES_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults) {
        if (REQUEST_NECESSARY_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    CommonUtil.toast(this, getString(R.string.permission_fail));
                    SystemClock.sleep(2000);
                    applyNecessaryPermissions();
                    return;
                }
            }
            relaunchApp(); //允许了必要权限后，则重启
        } else if (REQUEST_CONTACT_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    CommonUtil.toast(this, getString(R.string.permission_contact_fail));
                    break;
                }
            }
        } else if (REQUEST_INSTALL_PACKAGES_PERMISSION == requestCode) {
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    CommonUtil.toast(this, getString(R.string.permission_read_installed_pkg_fail));
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if ((TEXT_INPUT_SUB_UI_LEVER & mCurUILever) > 0) {
            onSwitchInputText();
        } else {
            if (MAIN_UI_LEVER != mCurUILever) {
                switch (mCurUILever) {
                    case TRANSLATE_SUB_UI_LEVER:
                        findViewById(R.id.left_arrow_image_view).setVisibility(View.GONE);
                        findViewById(R.id.right_arrow_image_view).setVisibility(View.GONE);
                        String msg = String.format(getString(R.string.baidu_unit_fix_translate_stop),
                                mBaiduUnitAI.getTranslateFixAction().getTargetLanguage());
                        showFinalTextResponse(msg, false);
                        startTTS(msg);
                        mBaiduUnitAI.getTranslateFixAction().forceAction(false);
                        mBaiduSpeechAI.setLanguageType(BaiduSpeechAI.BAIDU_SPEECH_CHINESE);
                        break;
                    case POEM_SUB_UI_LEVER:
                        showFinalTextResponse(getString(R.string.baidu_unit_fix_poem_stop), false);
                        startTTS(getString(R.string.baidu_unit_fix_poem_stop));
                        mBaiduUnitAI.getPoemFixAction().forceAction(false);
                        break;
                    case COUPLET_SUB_UI_LEVER:
                        showFinalTextResponse(getString(R.string.baidu_unit_fix_couplet_stop), false);
                        startTTS(getString(R.string.baidu_unit_fix_couplet_stop));
                        mBaiduUnitAI.getCoupletFixAction().forceAction(false);
                        break;
                }

                mCurUILever = MAIN_UI_LEVER;
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.application_exit));
                builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        MainActivity.super.onBackPressed();
                    }
                });
                builder.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        moveTaskToBack(true);
                    }
                });
                builder.show();
            }
        }
    }

    private void relaunchApp() {
        Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, restartIntent); // 0.5秒钟后重启应用
        }
        finish();
    }

    /*主界面下方按钮触发功能*/

    /*菜单功能选择*/
    private void onShowManageDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_translate_start));
        itemList.add(getString(R.string.option_ocr));
        itemList.add(getString(R.string.option_image_classify));
        itemList.add(getString(R.string.option_face_identify));
        itemList.add(getString(R.string.option_text_input));
        itemList.add(getString(R.string.option_clear_all_items));
        itemList.add(getString(R.string.option_paste_item));
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
                    case 2: //图像物体识别
                        onShowClassifyTypeDialog();
                        break;
                    case 3: //人脸识别
                        onShowFaceIdentifyInputDialog();
                        break;
                    case 4: //输入文本框
                        onSwitchInputText();
                        break;
                    case 5: //清除列表中所有显示
                        onClearAllItems();
                        break;
                    case 6: //粘贴文本增加一条目显示
                        onPasteItem();
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
        onShowOCRLanguageDialog(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                onShowOCRInputDialog(items[which]);
            }
        });
    }

    protected void onShowOCRLanguageDialog(String[] items, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, listener);
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

    protected void onFaceIdentifyCamera() {
        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.IDENTIFY_FACE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_face_identify.jpg");
        startActivityForResult(intent, FACE_IDENTIFY_CAMERA_REQUEST_CODE);
    }

    private void onShowClassifyTypeDialog() {
        onShowClassifyTypeDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始, 需要转换为classify type
                int classifyType = mSettingResult.mClassifyImageTypeList.get(which);
                onShowClassifyImageInputDialog(classifyType);
            }
        });
    }

    protected void onShowClassifyTypeDialog(DialogInterface.OnClickListener listener){
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
        listDialog.setItems(selItems, listener);
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
            findViewById(R.id.input_text_send).setVisibility(View.GONE);
            mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;
        } else {
            findViewById(R.id.input_text).setVisibility(View.VISIBLE);
            findViewById(R.id.input_text_send).setVisibility(View.VISIBLE);
            inputText.requestFocus();
            mCurUILever |= TEXT_INPUT_SUB_UI_LEVER;
        }
    }

    private void onClearAllItems() {
        mResultMsgList.clear();
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
    }

    private void onPasteItem() {
        ClipboardManager clipboardManager =(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (null != clipboardManager && clipboardManager.getPrimaryClip() != null) {
            ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
            if (null != item) {
                showFinalTextResponse(item.getText().toString(), false);
            }
        }
    }

    /*额外菜单功能选择*/
    private void onShowExtraManageDialog() {
        ArrayList<String> itemList = new ArrayList<>();
        itemList.add(getString(R.string.option_face_detect));
        itemList.add(getString(R.string.option_face_compare));
        itemList.add(getString(R.string.option_face_merge));
        itemList.add(getString(R.string.option_face_authenticate));

        itemList.add(getString(R.string.option_human_body_gesture));
        itemList.add(getString(R.string.option_human_body_headcount));
        itemList.add(getString(R.string.option_human_body_segment));

        itemList.add(getString(R.string.option_image_process_selfie_anime));
        itemList.add(getString(R.string.option_image_process_colourize));
        itemList.add(getString(R.string.option_image_process_style_trans));

        itemList.add(getString(R.string.option_ocr_qrcode));
        itemList.add(getString(R.string.option_ocr_handwriting));

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
                    case 2: //额外功能，人脸融合
                        onShowFaceMerge();
                        break;
                    case 3: //额外功能，身份认证，没权限需要企业认证！
                        onShowFaceAuthenticateInputDialog();
                        break;

                    case 4: //额外功能，手势识别
                        onShowImageInputDialog(HUMAN_BODY_GESTURE_CAMERA_REQUEST_CODE, HUMAN_BODY_GESTURE_IMAGE_REQUEST_CODE);
                        break;
                    case 5: //额外功能，人头清点
                        onShowImageInputDialog(HUMAN_BODY_HEADCOUNT_CAMERA_REQUEST_CODE, HUMAN_BODY_HEADCOUNT_IMAGE_REQUEST_CODE);
                        break;
                    case 6: //额外功能，人像分割
                        onShowImageInputDialog(HUMAN_BODY_SEGMENT_CAMERA_REQUEST_CODE, HUMAN_BODY_SEGMENT_IMAGE_REQUEST_CODE);
                        break;

                    case 7: //额外功能，人像动漫化
                        onShowImageInputDialog(IMAGE_PROCESS_SELFIE_ANIME_CAMERA_REQUEST_CODE, IMAGE_PROCESS_SELFIE_ANIME_IMAGE_REQUEST_CODE);
                        break;
                    case 8: //额外功能，黑白图像上色
                        onShowImageInputDialog(IMAGE_PROCESS_COLOURIZE_CAMERA_REQUEST_CODE, IMAGE_PROCESS_COLOURIZE_IMAGE_REQUEST_CODE);
                        break;
                    case 9: //额外功能，图像风格转换
                        onShowImageInputDialog(IMAGE_PROCESS_STYLE_TRANS_CAMERA_REQUEST_CODE, IMAGE_PROCESS_STYLE_TRANS_IMAGE_REQUEST_CODE);
                        break;

                    case 10: //额外功能，二维码识别
                        onShowImageInputDialog(OCR_QRCODE_CAMERA_REQUEST_CODE, OCR_QRCODE_IMAGE_REQUEST_CODE);
                        break;
                    case 11: //额外功能，二维码识别
                        onShowImageInputDialog(OCR_HANDWRITING_CAMERA_REQUEST_CODE, OCR_HANDWRITING_IMAGE_REQUEST_CODE);
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

    void onShowImageInputDialog(final int cameraRequestCode, final int albumRequestCode) {
        onShowAlbumAndCameraOptionDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                                getFilesDir().getAbsolutePath() + File.separator + "camera_capture.jpg");
                        startActivityForResult(intent, cameraRequestCode);
                        break;
                    case 1:
                        mCropImageRequestCode = albumRequestCode;
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
        if (mSettingResult.mEnableExtraFun) {
            itemList.add(getString(R.string.option_show_face_manger));
        }
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
                    case 2: //额外功能，人脸库管理
                        onShowFaceManager();
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
                mResultMsgList.add(new Msg(getString(R.string.baidu_unit_fix_translate_help_support_touch), Msg.TYPE_SEND_TEXT));
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
        if (!(view instanceof LinearLayout)) return;

        boolean textItem = false, imageItem = false;
        if (view.findViewById(R.id.send_layout).getVisibility() == View.VISIBLE) {
            if (view.findViewById(R.id.send_msg_text_view).getVisibility() == View.VISIBLE) {
                textItem = true;
            } else if (view.findViewById(R.id.send_extra_image_view).getVisibility() == View.VISIBLE) {
                imageItem = true;
            }
        } else {
            if (view.findViewById(R.id.receive_msg_text_view).getVisibility() == View.VISIBLE) {
                textItem = true;
            } else if (view.findViewById(R.id.receive_extra_image_view).getVisibility() == View.VISIBLE) {
                imageItem = true;
            }
        }
        if (!textItem && !imageItem) return;

        final ArrayList<String> itemList = new ArrayList<>();
        if (textItem) {
            itemList.add(getString(R.string.option_query_again));
            itemList.add(getString(R.string.option_clear_item));
            itemList.add(getString(R.string.option_voice_output_text));
            itemList.add(getString(R.string.option_copy_edit_text));
            itemList.add(getString(R.string.option_web_query_text));
            itemList.add(getString(R.string.option_nlp_correct_text));
            itemList.add(getString(R.string.option_nlp_news_summary));
            itemList.add(getString(R.string.option_nlp_dnn_sentence));
        } else if (imageItem){
            itemList.add(getString(R.string.option_process_again));
            itemList.add(getString(R.string.option_clear_item));
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
                if (position >= 0) mResultMsgList.remove(position);
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
                if (position >= 0) mResultMsgList.remove(position);
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

            case 5: //文本纠错
                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                mBaiduNLPAI.action(BaiduNLPAI.CORRECT_TEXT_TYPE, text);
                break;
            case 6: //文章摘要
                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                mBaiduNLPAI.action(BaiduNLPAI.NEWS_SUMMARY_TYPE, text);
                break;
            case 7: //DNN语言模型
                showProgressDialog(getResources().getString(R.string.baidu_unit_working));
                mBaiduNLPAI.action(BaiduNLPAI.DNN_SENTENCE_TYPE, text);
                break;
        }
    }

    private void onInputTextOver(boolean query) {
        findViewById(R.id.input_text).setVisibility(View.GONE);
        findViewById(R.id.input_text_send).setVisibility(View.GONE);

        mCurUILever &= ~TEXT_INPUT_SUB_UI_LEVER;

        EditText inputText = findViewById(R.id.input_text);
        final String input = inputText.getText().toString();
        if (!TextUtils.isEmpty(input)) {
            if (query) {
                triggerQuery(input);
            } else {
                showFinalTextResponse(input, false);
            }
        }

        CommonUtil.hideSoftKeyboard(this);
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

    private void initLocalBroadCast() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_LAUNCH_PHOTO_ALBUM));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_CONTACT_PERMISSION));
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadCastReceiver,
                new IntentFilter(GlobalValue.LOCAL_BROADCAST_INSTALL_PACKAGES_PERMISSION));
    }

    private BroadcastReceiver mLocalBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (GlobalValue.LOCAL_BROADCAST_LAUNCH_CAMERA.equals(action)) {
                if (!TextUtils.isEmpty(intent.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE))) {
                    onOCRTextCameraActivity(intent.getStringExtra(GlobalValue.INTENT_OCR_LANGUAGE));
                } else if (intent.getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, -1) != -1) {
                    onClassifyImageCameraActivity(intent.getIntExtra(GlobalValue.INTENT_CLASSIFY_IMAGE_TYPE, 0));
                } else {
                    onFaceIdentifyCamera();
                }
            } else if (GlobalValue.LOCAL_BROADCAST_CONTACT_PERMISSION.equals(action)) {
                applyContactPermissions();
            } else if (GlobalValue.LOCAL_BROADCAST_INSTALL_PACKAGES_PERMISSION.equals(action)) {
                applyReadInstalledPkgPermissions();
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

    protected void showFinalImageResponse(final String imageFilePath, final boolean reverse) {
        if (!FileUtils.isFileExist(imageFilePath)) return;

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            showFinalImageResponseInUI(imageFilePath, reverse);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFinalImageResponseInUI(imageFilePath, reverse);
                }
            });
        }
    }

    private void showFinalImageResponseInUI(String imageFilePath, boolean reverse) {
        hideProgressDialog();
        mResultMsgList.add(new Msg(BitmapFactory.decodeFile(imageFilePath),
                        reverse? Msg.TYPE_RECEIVED_IMAGE : Msg.TYPE_SEND_IMAGE));
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showFinalTextResponse(final String text, final boolean reverse) {
        if (TextUtils.isEmpty(text)) return;

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            showFinalTextResponseInUI(text, reverse);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFinalTextResponseInUI(text, reverse);
                }
            });
        }
    }

    private void showFinalTextResponseInUI(String text, boolean reverse) {
        hideProgressDialog();
        mResultMsgList.add(new Msg(text, reverse? Msg.TYPE_RECEIVED_TEXT : Msg.TYPE_SEND_TEXT));
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showFinalTextResponse(final String first, final String second, final boolean reverse) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            showFinalTextResponseInUI(first, second, reverse);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showFinalTextResponseInUI(first, second, reverse);
                }
            });
        }
    }

    private void showFinalTextResponseInUI(String first, String second, boolean reverse) {
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

    protected void showHint(final String hint) {
        if (TextUtils.isEmpty(hint)) return;

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            showHintInUI(hint);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showHintInUI(hint);
                }
            });
        }
    }

    private void showHintInUI(String hint) {
        mResultMsgList.add(new Msg(hint, Msg.TYPE_RECEIVED_TEXT));
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
    }

    protected void showErrorMsg(final String msg) {
        if (TextUtils.isEmpty(msg)) return;

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            showErrorMsgInUI(msg);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorMsgInUI(msg);
                }
            });
        }
    }

    private void showErrorMsgInUI(String msg) {
        hideProgressDialog();
        mResultMsgList.add(new Msg(msg, Msg.TYPE_RECEIVED_TEXT));
        ((MsgAdapter)mResultMsgListView.getAdapter()).notifyDataSetChanged();
        mResultMsgListView.setSelection(mResultMsgListView.getAdapter().getCount() - 1);
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
}
