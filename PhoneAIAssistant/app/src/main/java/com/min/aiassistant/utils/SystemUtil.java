package com.min.aiassistant.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class SystemUtil {
    //打开系统相册
    public static void startSysAlbumActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);//"android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static String getAlbumImagePath(Context context, Uri uri) {
        if (null == uri) return null;

        String imagePath = null;

        if (DocumentsContract.isDocumentUri(context, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getAlbumImagePath(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getAlbumImagePath(context, contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getAlbumImagePath(context, uri, null);
        }

        return imagePath;
    }

    //获得图片路径
    private static String getAlbumImagePath(Context context, Uri uri, String selection) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);   //内容提供器
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));   //获取路径
            }
            cursor.close();
        }
        return path;
    }
}
