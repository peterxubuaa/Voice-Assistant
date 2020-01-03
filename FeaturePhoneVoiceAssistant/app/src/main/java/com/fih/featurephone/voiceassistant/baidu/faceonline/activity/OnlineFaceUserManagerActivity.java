package com.fih.featurephone.voiceassistant.baidu.faceonline.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.BaiduBaseAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.BaiduFaceOnlineAI;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceDBOperate;
import com.fih.featurephone.voiceassistant.baidu.faceonline.model.FaceDBQuery;
import com.fih.featurephone.voiceassistant.camera.CameraCaptureActivity;
import com.fih.featurephone.voiceassistant.camera.ImageCropActivity;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;
import com.fih.featurephone.voiceassistant.utils.FileUtils;
import com.fih.featurephone.voiceassistant.utils.GlobalValue;
import com.fih.featurephone.voiceassistant.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.widget.AbsListView.CHOICE_MODE_NONE;

public class OnlineFaceUserManagerActivity extends Activity {
    private final int ADD_ITEM_CAMERA_REQUEST_CODE = 100;
    private final int UPDATE_ITEM_REQUEST_CODE = 300;
    private final int MAX_QUERY_ITEM_NUM = 10;
    private final int IMAGE_SELECT_REQUEST_CODE = 1000;

    private ArrayList<UserItem> mUserList = new ArrayList<>();
    private ListView mUserListView;
    private boolean mSelectAll = false;
    private ProgressDialog mProgressDialog;
    private ExecutorService mFaceExecutorService;
    private Future mFaceTaskFuture;
    private int mQueryStartPos = 0;

    private FaceDBOperate mFaceDBOperate;
    private FaceDBQuery mFaceDBQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_face_user_manager);

        mFaceExecutorService = Executors.newSingleThreadExecutor();
        mFaceDBOperate = new FaceDBOperate(this, mFaceOnlineListener);
        mFaceDBQuery = new FaceDBQuery(this, mFaceOnlineListener);

        initProgressDialog();
        initView();
    }

    private BaiduBaseAI.IBaiduBaseListener mFaceOnlineListener = new BaiduBaseAI.IBaiduBaseListener() {
        @Override
        public void onError(String msg) {
            CommonUtil.toast(OnlineFaceUserManagerActivity.this, msg);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onFinalResult(Object result, int resultType) {
            switch (resultType) {
                case BaiduFaceOnlineAI.FACE_QUERY_ALL_USER_INFO_ACTION:
                    CommonUtil.toast(OnlineFaceUserManagerActivity.this, getString(R.string.baidu_face_user_info_query_success));
                    ArrayList<UserItem> queryItemList = (ArrayList<UserItem>)result;
                    mUserList.addAll(queryItemList);
                    mQueryStartPos += queryItemList.size();
                    refreshUserList(true);
                    break;
                case BaiduFaceOnlineAI.FACE_REGISTER_ACTION:
                    UserItem registerUserItem = (UserItem)result;
                    CommonUtil.toast(OnlineFaceUserManagerActivity.this,
                            String.format(getString(R.string.baidu_face_register_success), registerUserItem.getUserInfo()));
                    mUserList.add(registerUserItem);
                    refreshUserList(false);
                    break;
                case BaiduFaceOnlineAI.FACE_DELETE_ACTION:
                    UserItem deleteUserItem = (UserItem)result;
                    mUserList.remove(deleteUserItem);
                    refreshUserList(false);
                    break;
                case BaiduFaceOnlineAI.FACE_DELETE_LIST_ACTION:
                    ArrayList<UserItem> deletedUserItemList = (ArrayList<UserItem>)result;
                    for (UserItem userItem : deletedUserItemList) {
                        mUserList.remove(userItem);
                    }
                    refreshUserList(false);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) return;

        final int ADD_ITEM_ALBUM_REQUEST_CODE = 200;
        final String CROP_IMAGE_FILE_PATH = FileUtils.getFaceTempImageDirectory().getAbsolutePath()
                            + File.separator + "crop_face_manager.jpg";
        switch (requestCode) {
            case ADD_ITEM_CAMERA_REQUEST_CODE:
                showProgressDialog(getString(R.string.baidu_face_registering));
                final String cameraImagePath = data.getStringExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH);
                final String userInfo = data.getStringExtra(GlobalValue.INTENT_FACE_NAME);
                registerFace(cameraImagePath, userInfo);
                break;
            case UPDATE_ITEM_REQUEST_CODE:
                freshUpdateItem(data);
                break;

            case IMAGE_SELECT_REQUEST_CODE:
                Intent intent = new Intent(OnlineFaceUserManagerActivity.this, ImageCropActivity.class);
                intent.putExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH, CROP_IMAGE_FILE_PATH);
                intent.putExtra(GlobalValue.INTENT_FACE_NAME, getString(R.string.baidu_face_register_userinfo_hint));
                intent.putExtra(GlobalValue.INTENT_IMAGE_CROP_TYPE, ImageCropActivity.REGISTER_FACE_TYPE);
                String imagePath = SystemUtil.getAlbumImagePath(this, data.getData());
                intent.putExtra(GlobalValue.INTENT_IMAGE_FILEPATH, imagePath);
                startActivityForResult(intent, ADD_ITEM_ALBUM_REQUEST_CODE);
                break;
            case ADD_ITEM_ALBUM_REQUEST_CODE:
                final String cropImagePath = data.getStringExtra(GlobalValue.INTENT_CROP_IMAGE_FILEPATH);
                final String faceName = data.getStringExtra(GlobalValue.INTENT_FACE_NAME);
                registerFace(cropImagePath, faceName);
                break;
        }
    }

    private void initView() {
        mUserListView = findViewById(R.id.user_list_view);
        if (mUserListView.getAdapter() == null) {
            OnlineUserListAdapter userListAdapter =
                    new OnlineUserListAdapter(OnlineFaceUserManagerActivity.this, R.layout.online_face_userlist_item, mUserList);
            mUserListView.setAdapter(userListAdapter);
        }

        if (CommonUtil.isSupportMultiTouch(this)) {
            initTouchScreenView();
        }
    }

    private void initTouchScreenView() {
        mUserListView.setSelector(R.color.transparent);//设置条目没有选中背景@android:color/transparent
        mUserListView.setChoiceMode(CHOICE_MODE_NONE);
        mUserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onShowItemOptionDialog(position);
                return true;
            }
        });

        findViewById(R.id.face_item_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowAlbumAndCameraOptionDialog();
            }
        });

        findViewById(R.id.face_item_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRefreshItems(true);
            }
        });
        findViewById(R.id.face_item_refresh).setOnLongClickListener(new AdapterView.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onRefreshItems(false);
                return true;
            }
        });

        findViewById(R.id.face_item_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteSelectedItems();
            }
        });

        findViewById(R.id.face_item_select_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectedAllItems();
            }
        });
    }

    private void onShowItemOptionDialog(final int position) {
        final String[] items = {
                getString(R.string.baidu_face_user_manager_update),
                getString(R.string.baidu_face_user_manager_delete),
        };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(OnlineFaceUserManagerActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doOption(which, position);
            }
        });
        listDialog.show();
    }

    private void doOption(int option, int position) {
        UserItem userItem = (UserItem)mUserListView.getAdapter().getItem(position);

        switch (option) {
            case 0://更新
                onUpdateItem(userItem);
                break;
            case 1://删除
                onDeleteItem(userItem);
                break;
            default:
                break;
        }
    }

    private void onUpdateItem(UserItem userItem) {
        Intent intent = new Intent(OnlineFaceUserManagerActivity.this, OnlineFaceUserInfoActivity.class);
        intent.putExtra(GlobalValue.INTENT_USER_ID, userItem.getUserID());
        intent.putExtra(GlobalValue.INTENT_USER_INFO, userItem.getUserInfo());
        intent.putExtra(GlobalValue.INTENT_FACE_IMAGE_PATH, userItem.getFaceLocalImagePath());
        intent.putExtra(GlobalValue.INTENT_FACE_TOKEN, userItem.getFaceToken());

        startActivityForResult(intent, UPDATE_ITEM_REQUEST_CODE);
    }

    private void onShowAlbumAndCameraOptionDialog() {
        final String[] items = {
                getString(R.string.option_camera),
                getString(R.string.option_image),
        };

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(OnlineFaceUserManagerActivity.this);
        listDialog.setTitle(getString(R.string.option_dialog_title));
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0: //camera
                        onAddNewItemByCamera();
                        break;
                    case 1: //album
                        SystemUtil.startSysAlbumActivity(OnlineFaceUserManagerActivity.this, IMAGE_SELECT_REQUEST_CODE);
                        break;
                }
            }
        });
        listDialog.show();
    }

    private void onAddNewItemByCamera() {
        Intent intent = new Intent(OnlineFaceUserManagerActivity.this, CameraCaptureActivity.class);
        intent.putExtra(GlobalValue.INTENT_FACE_NAME, getString(R.string.baidu_face_register_userinfo_hint));
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_TYPE, CameraCaptureActivity.REGISTER_FACE_TYPE);
        intent.putExtra(GlobalValue.INTENT_CAMERA_CAPTURE_FILEPATH,
                getFilesDir().getAbsolutePath() + File.separator + "camera_face_register.jpg");
        startActivityForResult(intent, ADD_ITEM_CAMERA_REQUEST_CODE);
    }

    private void onDeleteItem(final UserItem userItem) {
        showProgressDialog(getString(R.string.baidu_face_register_deleting));
        mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mFaceDBOperate.requestFaceDelete(userItem);
            }
        });
    }

    private void onDeleteSelectedItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("提示");
        builder.setMessage(getString(R.string.baidu_face_manager_delete_msg));
        builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showProgressDialog(getString(R.string.baidu_face_register_deleting));
                mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<UserItem> delUserItemList = new ArrayList<>();
                        for (UserItem userItem : mUserList) {
                            if (userItem.isChecked()) {
                                delUserItemList.add(userItem);
                            }
                        }
                        mFaceDBOperate.requestFaceListDelete(delUserItemList);
                    }
                });
            }
        });
        builder.setNegativeButton(getString(R.string.button_cancel), null);
        builder.show();
    }

    private void onSelectedAllItems() {
        mSelectAll = !mSelectAll;
        for (UserItem user : mUserList) {
            user.setChecked(mSelectAll);
        }
        ((ArrayAdapter)mUserListView.getAdapter()).notifyDataSetChanged();
        ((ImageView)findViewById(R.id.face_item_select_all)).setImageResource(
                mSelectAll? R.drawable.baseline_check_box_white_48dp : R.drawable.baseline_check_box_outline_blank_white_48dp);
    }

    private void onRefreshItems(boolean nextPart) {
        if (mFaceTaskFuture != null && !mFaceTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        if (!nextPart) {
            mUserList.clear();
            mQueryStartPos = 0;
        }

        showProgressDialog(getResources().getString(R.string.baidu_face_manager_querying));
        mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                //后续需要添加分段查询，减少等待时间又能显示所有数据
                mFaceDBQuery.requestAllUserItem(GlobalValue.FACE_DEFAULT_GROUP_ID, mQueryStartPos, MAX_QUERY_ITEM_NUM);
            }
        });
    }

    private void refreshUserList(final boolean updateCheckbox) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            if (updateCheckbox) ((ImageView) findViewById(R.id.face_item_select_all))
                    .setImageResource(R.drawable.baseline_check_box_outline_blank_white_48dp);
            ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
            hideProgressDialog();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (updateCheckbox) ((ImageView) findViewById(R.id.face_item_select_all))
                            .setImageResource(R.drawable.baseline_check_box_outline_blank_white_48dp);
                    ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
                    hideProgressDialog();
                }
            });
        }
    }

    private void freshUpdateItem(Intent data) {
        String userID = data.getStringExtra(GlobalValue.INTENT_USER_ID);
        String userInfo = data.getStringExtra(GlobalValue.INTENT_USER_INFO);
        String faceImagePath = data.getStringExtra(GlobalValue.INTENT_FACE_IMAGE_PATH);

        for (UserItem userItem : mUserList) {
            if (userID.equals(userItem.getUserID())) {
                if (!TextUtils.isEmpty(userInfo)) {
                    userItem.setUserInfo(userInfo);
                }
                if (FileUtils.isFileExist(faceImagePath)) {
                    userItem.setFaceLocalImagePath(faceImagePath);
                }
                break;
            }
        }

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
                }
            });
        }
    }

    private void registerFace(final String cameraImagePath, final String userInfo) {
        if (mFaceTaskFuture != null && !mFaceTaskFuture.isDone()) {
            return;//上一次没有处理完，直接返回
        }

        mFaceTaskFuture = mFaceExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                mFaceDBOperate.requestFaceRegister(cameraImagePath, userInfo);
            }
        });
    }

    /*在UI界面上显示信息*/
    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }

    private void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
}

