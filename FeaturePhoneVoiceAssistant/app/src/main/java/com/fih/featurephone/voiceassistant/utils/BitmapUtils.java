/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.fih.featurephone.voiceassistant.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 这个类提供一些操作Bitmap的方法
 */
public final class BitmapUtils {
    // 图像的旋转方向是0
    private static final int ROTATE0 = 0;
    private static final int ROTATE90 = 90;
    private static final int ROTATE180 = 180;
    private static final int ROTATE270 = 270;
    // 图像压缩边界
//    private static final int IMAGEBOUND = 128;
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

    /*
     * 获取无损压缩图片合适的压缩比例
     *
     * @param options        图片的一些设置项
     * @param minSideLength  最小边长
     * @param maxNumOfPixels 最大的像素数目
     * @return 返回合适的压缩值
     */
//    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
//        int initialSize = BitmapUtils.computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
//        int roundedSize;
//        if (initialSize <= 8) { // SUPPRESS CHECKSTYLE
//            roundedSize = 1;
//            while (roundedSize < initialSize) {
//                roundedSize <<= 1;
//            }
//        } else {
//            roundedSize = (initialSize + 7) / 8 * 8; // SUPPRESS CHECKSTYLE
//        }
//        return roundedSize;
//    }

    /*
     * 获取无损压缩图片的压缩比
     *
     * @param options        图片的一些设置项
     * @param minSideLength  最小边长
     * @param maxNumOfPixels 最大的像素数目
     * @return 返回合适的压缩值
     */
//    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
//                                               int maxNumOfPixels) {
//        double w = options.outWidth;
//        double h = options.outHeight;
//        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
//        int upperBound =
//                (minSideLength == -1) ? BitmapUtils.IMAGEBOUND : (int) Math.min(
//                        Math.floor(w / minSideLength), Math.floor(h / minSideLength));
//        if (upperBound < lowerBound) {
//            return lowerBound;
//        }
//        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
//            return 1;
//        } else if (minSideLength == -1) {
//            return lowerBound;
//        } else {
//            return upperBound;
//        }
//    }

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
    public static Bitmap scaleBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

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

    /*
     * 等比压缩图片
     * @param resBitmap 原图
     * @param desWidth  压缩后图片的宽度
     * @param desHeight 压缩后图片的高度
     * @return 压缩后的图片
     */
//    public static Bitmap calculateInSampleSize(Bitmap resBitmap, int desWidth, int desHeight) {
//        int resWidth = resBitmap.getWidth();
//        int resHeight = resBitmap.getHeight();
//        if (resHeight > desHeight || resWidth > desWidth) {
//            // 计算出实际宽高和目标宽高的比率
//            final float heightRatio = (float) desHeight / (float) resHeight;
//            final float widthRatio = (float) desWidth / (float) resWidth;
//            float scale = heightRatio < widthRatio ? heightRatio : widthRatio;
//            return scale(resBitmap, scale);
//        }
//        return resBitmap;
//    }

    public static Bitmap yuv2Bitmap(byte[] data, int width, int height) {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1)]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0, width, 0, 0, width, height);
        return bmp;
    }

    public static Bitmap Depth2Bitmap(byte[] depthBytes, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] argbData = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            argbData[i] = (((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10 & 0x000000ff)
                    | ((((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10) & 0x000000ff) << 8
                    | ((((int) depthBytes[i * 2] + depthBytes[i * 2 + 1] * 256) / 10) & 0x000000ff) << 16
                    | 0xff000000;

        }
        bitmap.setPixels(argbData, 0, width, 0, 0, width, height);
        return bitmap;
    }

//    public static Bitmap RGB2Bitmap(byte[] bytes, int width, int height) {
//        // use Bitmap.Config.ARGB_8888 instead of type is OK
//        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        byte[] rgba = new byte[width * height * 4];
//        for (int i = 0; i < width * height; i++) {
//            byte b1 = bytes[i * 3];
//            byte b2 = bytes[i * 3 + 1];
//            byte b3 = bytes[i * 3 + 2];
//            // set value
//            rgba[i * 4] = b1;
//            rgba[i * 4 + 1] = b2;
//            rgba[i * 4 + 2] = b3;
//            rgba[i * 4 + 3] = (byte) 255;
//        }
//        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
//        return stitchBmp;
//    }

    public static Bitmap BGR2Bitmap(byte[] bytes, int width, int height) {
        // use Bitmap.Config.ARGB_8888 instead of type is OK
        Bitmap stitchBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] rgba = new byte[width * height * 4];
        for (int i = 0; i < width * height; i++) {
            byte b1 = bytes[i * 3];
            byte b2 = bytes[i * 3 + 1];
            byte b3 = bytes[i * 3 + 2];
            // set value
            rgba[i * 4] = b3;
            rgba[i * 4 + 1] = b2;
            rgba[i * 4 + 2] = b1;
            rgba[i * 4 + 3] = (byte) 255;
        }
        stitchBmp.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return stitchBmp;
    }

//    public static byte[] ARGB2R(byte[] bytes, int width, int height) {
//        byte[] IR = new byte[width * height];
//        for (int i = 0; i < width * height; i++) {
//            IR[i] = bytes[i * 4];
//        }
//        return IR;
//    }

    /*
     * 获取图片数据
     * @param path
     * @return
     */
//    public static Bitmap getBitmapFromJpegFile(String path) {
//        FileInputStream fis;
//        Bitmap bm = null;
//        try {
//            fis = new FileInputStream(path);
//            BitmapFactory.Options options = new BitmapFactory.Options();
////            options.inSampleSize = 8;//图片的长宽都是原来的1/8
//            BufferedInputStream bis = new BufferedInputStream(fis);
//            bm = BitmapFactory.decodeStream(bis, null, options);
//            fis.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bm;
//    }

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

    public static void resizeBitmapToJpeg(Bitmap bitmap, int maxWidth, int maxHeight, String filePath) {
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

/*    public static void saveCropJpeg(String inputJpegFilePath, Point previewSize, Rect box, int orientation, String outputJpegFilePath){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(inputJpegFilePath, options);

        Bitmap bitmap = BitmapFactory.decodeFile(inputJpegFilePath);
        saveCropJpeg(bitmap, previewSize, box, orientation, outputJpegFilePath);
    }

    public static void saveCropJpeg(String inputJpegFilePath, Point previewSize, Rect box, String outputJpegFilePath){
        int orientation = BitmapUtils.getJpegImageRotateDegree(inputJpegFilePath);
        saveCropJpeg(inputJpegFilePath, previewSize, box, orientation, outputJpegFilePath);
    }*/

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
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        saveBitmapToJpeg(outputBitmap, outputJpegFilePath);
    }

    public static void saveCropJpeg(String inputJpegFilePath, Rect cropRect, String outputJpegFilePath) {
        Bitmap inputBitmap = BitmapFactory.decodeFile(inputJpegFilePath);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height());
        saveBitmapToJpeg(outputBitmap, outputJpegFilePath);
    }
}
