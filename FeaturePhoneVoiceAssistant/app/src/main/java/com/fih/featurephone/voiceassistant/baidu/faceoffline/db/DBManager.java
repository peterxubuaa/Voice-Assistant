package com.fih.featurephone.voiceassistant.baidu.faceoffline.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.fih.featurephone.voiceassistant.baidu.faceoffline.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理类
 */
public class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();

    private static DBManager sInstance;
    private static SQLiteOpenHelper mDBHelper;
    private SQLiteDatabase mDatabase;

    /**
     * 单例模式，初始化DBManager
     * @return DBManager实例
     */
    public static synchronized DBManager getInstance() {
        if (sInstance == null) {
            sInstance = new DBManager();
        }
        return sInstance;
    }

    /**
     * 数据库初始化
     * @param context 当前上下文
     */
    public void init(Context context) {
        if (context == null) return;

        if (mDBHelper == null) {
            mDBHelper = new DBHelper(context.getApplicationContext());
        }
    }

    /**
     * 释放数据库
     */
    public void release() {
        if (mDBHelper != null) {
            mDBHelper.close();
            mDBHelper = null;
        }
        sInstance = null;
    }

    /**
     * 添加用户
     */
    public boolean addUser(User user) {
        if (null == mDBHelper || null == user) return false;

        try {
            mDatabase = mDBHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put("user_id", user.getUserID());
            cv.put("user_name", user.getUserName());
            cv.put("user_info", user.getUserInfo());
            cv.put("face_image_path", user.getFaceImagePath());
            cv.put("face_token", user.getFaceToken());
            cv.put("face_feature", user.getFaceFeature());
            cv.put("create_time", System.currentTimeMillis());
            cv.put("update_time", System.currentTimeMillis());

            long rowId = mDatabase.insert(DBHelper.TABLE_USER, null, cv);
            if (rowId < 0) return false;

            mDatabase.setTransactionSuccessful();
            Log.d(TAG, "insert user success:" + rowId);
        } catch (Exception e) {
            Log.e(TAG, "addUser e = " + e.getMessage());
            return false;
        } finally {
            mDatabase.endTransaction();
        }
        return true;
    }

    /**
     * 查询一定数量的用户
     */
    public List<User> queryUserList(int start, int offset) {
        if (mDBHelper == null) return null;

        Cursor cursor = null;
        try {
            List<User> userList = new ArrayList<>();
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String limit =  start + " , " + offset;
            cursor = db.query(DBHelper.TABLE_USER, null, null, null, null, null, null, limit);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                userList.add(getUserByCursor(cursor));
            }
            return userList;
        } catch (Exception e) {
            Log.e(TAG, "queryUserList e = " + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return null;
    }

    /**
     * 查询用户（根据dbId）, 唯一记录
     */
    public User queryUserByDBId(int _id) {
        if (mDBHelper == null) return null;

        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "_id = ? ";
            String[] whereValue = { String.valueOf(_id) };
            cursor = db.query(DBHelper.TABLE_USER, null, where, whereValue, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                return getUserByCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "queryUserByDBId e = " + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return null;
    }

    /**
     * 查询用户(根据 userId 只有唯一的一条记录
     */
    public User queryUserByUserID(String userId) {
        if (mDBHelper == null || TextUtils.isEmpty(userId)) return null;

        Cursor cursor = null;
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "user_id = ? ";
            String[] whereValue = { userId };
            cursor = db.query(DBHelper.TABLE_USER, null, where, whereValue, null, null, null);
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                return getUserByCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "queryUserByUserID e = " + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return null;
    }

    /**
     * 查询用户（根据 userName， 可有多条记录）
     */
    public List<User> queryUserListByUserName(String userName) {
        if (mDBHelper == null || TextUtils.isEmpty(userName)) return null;

        Cursor cursor = null;
        List<User> userList = new ArrayList<>();
        try {
            SQLiteDatabase db = mDBHelper.getReadableDatabase();
            String where = "user_name = ? ";
            String[] whereValue = { userName };
            cursor = db.query(DBHelper.TABLE_USER, null, where, whereValue, null, null, null);
            while (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                userList.add(getUserByCursor(cursor));
            }
            return userList;
        } catch (Exception e) {
            Log.e(TAG, "queryUserListByUserName e = " + e.getMessage());
        } finally {
            closeCursor(cursor);
        }
        return null;
    }

    /**
     * 更新用户, 根据唯一的user_id
     */
    public boolean updateUser(User user) {
        if (null == mDBHelper || null == user) return false;

        boolean result = false;
        try {
            mDatabase = mDBHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            String where = "_id = ? ";
            String[] whereValue = { String.valueOf(user.getDBID()) };

            ContentValues cv = new ContentValues();
            if (user.getUserName() != null) cv.put("user_name", user.getUserName());
            if (user.getUserInfo() != null) cv.put("user_info", user.getUserInfo());
            if (user.getFaceImagePath() != null) cv.put("face_image_path", user.getFaceImagePath());
            if (user.getFaceToken() != null) cv.put("face_token", user.getFaceToken());
            if (user.getFaceFeature() != null) cv.put("face_feature", user.getFaceFeature());
            cv.put("update_time", System.currentTimeMillis());

            if (mDatabase.update(DBHelper.TABLE_USER, cv, where, whereValue) >= 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "updateUser e = " + e.getMessage());
        } finally {
            mDatabase.endTransaction();
        }
        return result;
    }

    /**
     * 删除用户，根据唯一的dbid
     */
    public boolean deleteUserByDBID(int _id) {
        if (null == mDBHelper) return false;

        boolean result = false;
        try {
            mDatabase = mDBHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            String where = "_id = ? ";
            String[] whereValue = { String.valueOf(_id) };
            if (mDatabase.delete(DBHelper.TABLE_USER, where, whereValue) >= 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "deleteUserByDBID e = " + e.getMessage());
        } finally {
            mDatabase.endTransaction();
        }
        return result;
    }

    /**
     * 删除用户，根据唯一的user_id
     */
    public boolean deleteUserByUserID(String userID) {
        if (null == mDBHelper || TextUtils.isEmpty(userID)) return false;

        boolean result = false;
        try {
            mDatabase = mDBHelper.getWritableDatabase();
            mDatabase.beginTransaction();

            String where = "user_id = ? ";
            String[] whereValue = { userID };
            if (mDatabase.delete(DBHelper.TABLE_USER, where, whereValue) >= 0) {
                result = true;
            }
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "deleteUserByUserID e = " + e.getMessage());
        } finally {
            mDatabase.endTransaction();
        }
        return result;
    }

    private User getUserByCursor(Cursor cursor) {
        if (null == cursor) return null;

        User user = new User();
        user.setDBID(cursor.getInt(cursor.getColumnIndex("_id")));
        user.setUserID(cursor.getString(cursor.getColumnIndex("user_id")));
        user.setUserName(cursor.getString(cursor.getColumnIndex("user_name")));
        user.setUserInfo(cursor.getString(cursor.getColumnIndex("user_info")));
        user.setFaceImagePath(cursor.getString(cursor.getColumnIndex("face_image_path")));
        user.setFaceToken(cursor.getString(cursor.getColumnIndex("face_token")));
        user.setFaceFeature(cursor.getBlob(cursor.getColumnIndex("face_feature")));
        user.setCreateTime(cursor.getLong(cursor.getColumnIndex("create_time")));
        user.setUpdateTime(cursor.getLong(cursor.getColumnIndex("update_time")));
        return user;
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Throwable e) {
                Log.e(TAG, "closeCursor e = " + e.getMessage());
            }
        }
    }
}