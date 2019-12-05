package com.fih.featurephone.voiceassistant.baidu.face.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.fih.featurephone.voiceassistant.R;
import com.fih.featurephone.voiceassistant.baidu.face.listener.UserInfoUpdateListener;
import com.fih.featurephone.voiceassistant.baidu.face.manager.FaceSDKManager;
import com.fih.featurephone.voiceassistant.baidu.face.model.User;
import com.fih.featurephone.voiceassistant.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

import static android.widget.AbsListView.CHOICE_MODE_NONE;

public class FaceUserManagerActivity extends Activity {
    private final int ADD_ITEM_REQUEST_CODE = 100;
    private final int UPDATE_ITEM_REQUEST_CODE = 101;

    private List<User> mUserList = new ArrayList<>();
    private ListView mUserListView;
    private boolean mSelectAll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_user_manager);
        initView();
        initUserList();
    }

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

        switch (requestCode) {
            case ADD_ITEM_REQUEST_CODE:
                ArrayList<String> userIDList = data.getStringArrayListExtra(FaceRGBRegisterActivity.REGISTER_USER_ID_LIST);
                FaceSDKManager.getInstance().queryUserListFromDBManager(userIDList, mUserInfoUpdateListener);
                break;
            case UPDATE_ITEM_REQUEST_CODE:
                freshUpdateItem(data);
                break;
        }
    }

    private void initView() {
        mUserListView = findViewById(R.id.user_list_view);

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
                onAddNewItem();
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

    private void initUserList() {
        mUserList.clear();
        FaceSDKManager.getInstance().queryUserListFromDBManager(mUserInfoUpdateListener);
    }

    private void onShowItemOptionDialog(final int position) {
        final String[] items = {
                getString(R.string.baidu_face_user_manager_update),
                getString(R.string.baidu_face_user_manager_delete),
        };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(FaceUserManagerActivity.this);
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
        User user = (User)mUserListView.getAdapter().getItem(position);

        switch (option) {
            case 0://更新
                onUpdateItem(user);
                break;
            case 1://删除
                onDeleteItem(user);
                break;
            default:
                break;
        }
    }

    private void onUpdateItem(User user) {
        Intent intent = new Intent(FaceUserManagerActivity.this, FaceUserInfoActivity.class);
        intent.putExtra(FaceUserInfoActivity.DB_ID, user.getDBID());
        intent.putExtra(FaceUserInfoActivity.USER_ID, user.getUserID());
        intent.putExtra(FaceUserInfoActivity.USER_NAME, user.getUserName());
        intent.putExtra(FaceUserInfoActivity.USER_INFO, user.getUserInfo());
        intent.putExtra(FaceUserInfoActivity.CREATE_TIME, user.getCreateTime());
        intent.putExtra(FaceUserInfoActivity.FACE_IMAGE_PATH, user.getFaceImagePath());

        startActivityForResult(intent, UPDATE_ITEM_REQUEST_CODE);
    }

    private void onAddNewItem() {
        startActivityForResult(new Intent(FaceUserManagerActivity.this, FaceRGBRegisterActivity.class), ADD_ITEM_REQUEST_CODE);
    }

    private void onDeleteItem(User user) {
        FaceSDKManager.getInstance().deleteUserIntoDBManager(user, mUserInfoUpdateListener);
    }

    private void onDeleteSelectedItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("提示");
        builder.setMessage(getString(R.string.baidu_face_manager_delete_msg));
        builder.setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<User> delUserList = new ArrayList<>();
                for (User user : mUserList) {
                    if (user.getChecked()) {
                        delUserList.add(user);
                    }
                }
                FaceSDKManager.getInstance().deleteUserListIntoDBManager(delUserList, mUserInfoUpdateListener);
            }
        });
        builder.setNegativeButton(getString(R.string.button_cancel), null);
        builder.show();
    }

    UserInfoUpdateListener mUserInfoUpdateListener = new UserInfoUpdateListener() {
        @Override
        public void userListQuerySuccess(List<User> userInfoList, boolean bAll) {
            CommonUtil.toast(FaceUserManagerActivity.this, getString(R.string.baidu_face_user_info_query_success));
            if (bAll) mUserList.clear();
            mUserList.addAll(userInfoList);

            if (mUserListView.getAdapter() == null) {
                UserListAdapter userListAdapter = new UserListAdapter(FaceUserManagerActivity.this, R.layout.face_userlist_item, mUserList);
                mUserListView.setAdapter(userListAdapter);
            }

            refreshUserList(false);
            FaceSDKManager.getInstance().pushFaceFeature(); //temp solution
        }

        @Override
        public void userListQueryFailure(String message) {
            CommonUtil.toast(FaceUserManagerActivity.this, getString(R.string.baidu_face_user_info_query_fail) + message);
        }

        @Override
        public void userListDeleteSuccess(final List<User> deletedUserList) {
            CommonUtil.toast(FaceUserManagerActivity.this, getString(R.string.baidu_face_user_info_delete_success));
            for (User user : deletedUserList) {
                mUserList.remove(user);
            }
            refreshUserList(true);
        }

        @Override
        public void userListDeleteFailure(final List<User> deletedUserList, String message) {
            CommonUtil.toast(FaceUserManagerActivity.this, getString(R.string.baidu_face_user_info_delete_fail));
            for (User user : deletedUserList) {
                mUserList.remove(user);
            }
            refreshUserList(true);
        }
    };

    private void onSelectedAllItems() {
        mSelectAll = !mSelectAll;
        for (User user : mUserList) {
            user.setChecked(mSelectAll);
        }
        ((ArrayAdapter)mUserListView.getAdapter()).notifyDataSetChanged();
        ((ImageView)findViewById(R.id.face_item_select_all)).setImageResource(
                mSelectAll? R.drawable.baseline_check_box_white_48dp : R.drawable.baseline_check_box_outline_blank_white_48dp);
    }

    private void refreshUserList(final boolean updateCheckbox) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            if (updateCheckbox) ((ImageView) findViewById(R.id.face_item_select_all))
                    .setImageResource(R.drawable.baseline_check_box_outline_blank_white_48dp);
            ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (updateCheckbox) ((ImageView) findViewById(R.id.face_item_select_all))
                            .setImageResource(R.drawable.baseline_check_box_outline_blank_white_48dp);
                    ((ArrayAdapter) mUserListView.getAdapter()).notifyDataSetChanged();
                }
            });
        }
    }

    private void freshUpdateItem(Intent data) {
        int dbID = data.getIntExtra(FaceUserInfoActivity.DB_ID, 0);
        String userInfo = data.getStringExtra(FaceUserInfoActivity.USER_INFO);
        String faceImagePath = data.getStringExtra(FaceUserInfoActivity.FACE_IMAGE_PATH);

        for (User user : mUserList) {
            if (dbID == user.getDBID()) {
                if (!TextUtils.isEmpty(userInfo)) {
                    user.setUserInfo(userInfo);
                }
                if (!TextUtils.isEmpty(faceImagePath)) {
                    user.setFaceImagePath(faceImagePath);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                onAddNewItem();
                break;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                onShowItemOptionDialog(mUserListView.getSelectedItemPosition());
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                User user = (User)mUserListView.getSelectedItem();
                if (null != user) {
                    user.setChecked(!user.getChecked());
                    refreshUserList(true);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                onDeleteSelectedItems();
                break;
            case KeyEvent.KEYCODE_BACK:
                finish();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }
}

