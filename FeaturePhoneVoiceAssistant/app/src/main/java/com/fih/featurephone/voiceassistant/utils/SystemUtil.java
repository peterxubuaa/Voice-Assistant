package com.fih.featurephone.voiceassistant.utils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;

public class SystemUtil {
    //打开系统相册
    public static void startSysAlbumActivity(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);//"android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void cropSelectImage(Activity activity, Uri imageUri, int requestCode, String cropFilePath) {
        final int MAX_PIX = 1024; //默认4:3的比例
        String path = getAlbumImagePath(activity, imageUri);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int width, height;
        int degree = BitmapUtils.getJpegImageRotateDegree(path);
        if (degree % 180 == 90) {
            width = options.outHeight;
            height = options.outWidth;
        } else {
            width = options.outWidth;
            height = options.outHeight;
        }

        int aspectX, aspectY;
        //调整高宽和剪裁宽高比例
        if (width >= height) {
            if (width > MAX_PIX) {
                height = height * MAX_PIX / width;
                width = MAX_PIX;
                aspectX = 4; aspectY = 3;
            } else {
                int gcd = CommonUtil.getGCD(width, height);
                aspectX = width / gcd;
                aspectY = height / gcd;
            }
        } else {
            if (height > MAX_PIX) {
                width = width * MAX_PIX / height;
                height = MAX_PIX;
                aspectX = 3; aspectY = 4;
            } else {
                int gcd = CommonUtil.getGCD(width, height);
                aspectX = width / gcd;
                aspectY = height / gcd;
            }
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);//X方向上的比例, 设置为0剪裁框是任意比例的, 但保持的图像仍然是outputX
        intent.putExtra("aspectY", aspectY);//Y方向上的比例
        intent.putExtra("outputX", width);//裁剪后的图片的宽。与裁剪框的宽没有半毛钱关系
        intent.putExtra("outputY", height);//裁剪后的图片的高。与裁剪框的高没有半毛钱关系
        intent.putExtra("scale", true);//是否保留比例(不一定有效)
        intent.putExtra("return-data", false);//是否将数据保留在Bitmap中返回
        intent.putExtra("noFaceDetection", false);//是否取消人脸识别
        intent.putExtra("circleCrop", false);//是否是圆形裁剪区域(不一定有效)

        FileUtils.deleteFile(cropFilePath);//删除上一次临时剪裁文件
        Uri cropImageUri = Uri.fromFile(new File(cropFilePath));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);//设置大图保存到文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());//保存的图片格式

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

/*    //打开系统相机
    public static void startSysCameraActivity(Activity activity, String tempSaveImagePath, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {

        } else {
            Uri uri = Uri.fromFile(new File(tempSaveImagePath));
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    private static String getRealPathFromUri_AboveApi19(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }


        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.MediaColumns.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }*/
}
