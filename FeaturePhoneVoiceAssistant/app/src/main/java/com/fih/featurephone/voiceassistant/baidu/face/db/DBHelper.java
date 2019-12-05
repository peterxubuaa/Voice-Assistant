package com.fih.featurephone.voiceassistant.baidu.face.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fih.featurephone.voiceassistant.utils.FileUtils;

import java.io.File;

/**
 * 数据库创建工具
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_START_SQL = "CREATE TABLE IF NOT EXISTS ";
    private static final String CREATE_TABLE_PRIMARY_SQL = " integer primary key autoincrement,";
    /** 数据库名称 */
    private static final String DB_NAME = FileUtils.getFaceDatabaseDirectory().getAbsolutePath() + File.separator + "face.db";
    /** 数据库版本 */
    private static final int VERSION = 1;
    /** 用户表 */
    static final String TABLE_USER = "user";

    DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            onCreate(db);
        }
    }

    private synchronized void createTables(SQLiteDatabase db) {
        if (db == null || db.isReadOnly()) {
            db = getWritableDatabase();
        }

        // 创建用户表的SQL语句
        StringBuilder userSql = new StringBuilder();
        userSql.append(CREATE_TABLE_START_SQL).append(TABLE_USER).append(" ( ");
        userSql.append(" _id").append(CREATE_TABLE_PRIMARY_SQL);
        userSql.append(" user_id").append(" varchar(32) default \"\"   ,");
        userSql.append(" user_name").append(" varchar(32) default \"\"   ,");
        userSql.append(" user_info").append(" varchar(32) default \"\"   ,");
        userSql.append(" face_image_path").append(" varchar(255) default \"\"  ,");
        userSql.append(" face_token").append(" varchar(128) default \"\" ,");
        userSql.append(" face_feature").append(" blob ,");
        userSql.append(" create_time").append(" long ,");
        userSql.append(" update_time").append(" long )");

        try {
            db.execSQL(userSql.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
