/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.min.aiassistant.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 这个类提供一些操作Bitmap的方法
 */
public final class BitmapUtils {
    // 图像的旋转方向是0
    private static final int ROTATE0 = 0;
    private static final int ROTATE90 = 90;
    private static final int ROTATE180 = 180;
    private static final int ROTATE270 = 270;
    // 默认的图片压缩的质量
    private static final int DEFAULT_IMAGE_JPG_QUALITY = 90;

    /**
     * 根据从数据中读到的方向旋转图片
     *
     * @param orientation 图片方向
     * @param bitmap      要旋转的bitmap
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float orientation) {
        Bitmap transformed;
        if (orientation == 0) {
            transformed = bitmap;
        } else {
            Matrix m = new Matrix();
            m.setRotate(orientation);
            transformed = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        }
        return transformed;
    }

    /**
     * 解析图片的旋转方向
     *
     * @param path 图片的路径
     * @return 旋转角度
     */
    public static int getJpegImageRotateDegree(String path) {
        int degree;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation =
                    exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = ROTATE90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = ROTATE180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = ROTATE270;
                    break;
                default:
                    degree = ROTATE0;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            degree = ROTATE0;
        }
        return degree;
    }

    /*
     * 等比压缩图片
     *
     * @param bitmap 原图
     * @param scale  压缩因子
     * @return 压缩后的图片
     */
/*    public static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }*/

    /*
     * 尺寸缩放
     *
     * @param bitmap bitmap
     * @param w      width
     * @param h      height
     * @return scaleBitmap
     */
    private static Bitmap scaleBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static void saveBitmapToJpeg(Bitmap bitmap, String filePath) {
        try {
            // save image
            FileOutputStream out = new FileOutputStream(new File(filePath));
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, DEFAULT_IMAGE_JPG_QUALITY, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void resizeBitmapToJpeg(Bitmap bitmap, int maxWidth, int maxHeight, String filePath) {
        Bitmap scaleBitmap = scaleBitmap(bitmap, maxWidth, maxHeight);
        saveBitmapToJpeg(scaleBitmap, filePath);
    }

    public static void resizeJpegFile(String inFilePath, int maxWidth, int maxHeight, String outFilePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(inFilePath);
        resizeBitmapToJpeg(bitmap, maxWidth, maxHeight, outFilePath);
    }

    public static Bitmap getCameraShotBitmap(byte[] data, Point pictureSize, Point previewSize, Rect box, int orientation){
        //前置camera preview时默认会有mirror效果
        //data is jpeg format
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        Matrix matrix = new Matrix();
        if (ROTATE90 == orientation || ROTATE270 == orientation) {
            matrix.postRotate(orientation);
            int temp = pictureSize.x;
            pictureSize.x = pictureSize.y;
            pictureSize.y = temp;
        }
        int cropLeft = box.left * pictureSize.x / previewSize.x;
        int cropTop = box.top * pictureSize.y / previewSize.y;
        int cropRight = box.right * pictureSize.x / previewSize.x;
        int cropBottom = box.bottom * pictureSize.y / previewSize.y;
        if (ROTATE270 == orientation) { //facing camera mirror
            matrix.postScale(-1, 1); //镜像水平翻转
        }

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap = Bitmap.createBitmap(bitmap, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop);

        return bitmap;
    }

    public static void saveCropJpeg(Bitmap inputBitmap, Point previewSize, Rect box, int orientation, String outputJpegFilePath){
        int width, height;
        if (orientation % ROTATE180 == ROTATE90) {
            width = inputBitmap.getHeight();
            height = inputBitmap.getWidth();
        } else {
            width = inputBitmap.getWidth();
            height = inputBitmap.getHeight();
        }

        if (ROTATE0 != orientation) {//旋转image
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            inputBitmap = Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
        }

        int cropLeft = box.left * width / previewSize.x;
        int cropTop = box.top * height / previewSize.y;
        int cropRight = box.right * width / previewSize.x;
        int cropBottom = box.bottom * height / previewSize.y;

        Bitmap bitmap = Bitmap.createBitmap(inputBitmap, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop);
        saveBitmapToJpeg(bitmap, outputJpegFilePath);
    }

    public static void saveCropJpeg(Bitmap inputBitmap, Rect cropRect, String outputJpegFilePath) {
        if (cropRect.left < 0) cropRect.left = 0;
        if (cropRect.right > inputBitmap.getWidth()) cropRect.right = inputBitmap.getWidth();
        if (cropRect.top < 0) cropRect.top = 0;
        if (cropRect.bottom > inputBitmap.getHeight()) cropRect.bottom = inputBitmap.getHeight();

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        saveBitmapToJpeg(outputBitmap, outputJpegFilePath);
    }

    public static void saveCropJpeg(String inputJpegFilePath, Rect cropRect, String outputJpegFilePath) {
        Bitmap inputBitmap = BitmapFactory.decodeFile(inputJpegFilePath);

        if (cropRect.left < 0) cropRect.left = 0;
        if (cropRect.right > inputBitmap.getWidth()) cropRect.right = inputBitmap.getWidth();
        if (cropRect.top < 0) cropRect.top = 0;
        if (cropRect.bottom > inputBitmap.getHeight()) cropRect.bottom = inputBitmap.getHeight();

        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        saveBitmapToJpeg(outputBitmap, outputJpegFilePath);
    }
}
