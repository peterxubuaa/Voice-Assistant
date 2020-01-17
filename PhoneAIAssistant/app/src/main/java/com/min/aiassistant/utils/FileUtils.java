package com.min.aiassistant.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static byte[] readImageFile(String filePath) {
        try {
            FileInputStream inputFile = new FileInputStream(filePath);
            int fileLength = inputFile.available();
            byte[] buffer = new byte[fileLength];
            int readLength = 0;
            while (readLength < fileLength) {
                readLength += inputFile.read(buffer, readLength, fileLength - readLength);
            }
            inputFile.close();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeImageFile(byte[] content, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

//        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(content);
            fileOutputStream.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * Checks if is sd card available.检查SD卡是否可用
     */
    private static boolean isSdCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Gets the SD root file.获取SD卡根目录
     */
    private static File getSDRootFile() {
        if (isSdCardAvailable()) {
            return Environment.getExternalStorageDirectory();
        } else {
            return null;
        }
    }

    /**
     * 获取人脸图片保存的目录信息
     */
    public static File getFaceImageDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "FaceData" + File.separator + "Images");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    public static File getFaceTempImageDirectory() {
        File sdRootFile = getSDRootFile();
        File file = null;
        if (sdRootFile != null && sdRootFile.exists()) {
            file = new File(sdRootFile, "FaceData" + File.separator + "Temp");
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return file;
    }

    /**
     * 判断文件是否存在
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;

        File file = new File(filePath);
        try {
            if (!file.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 删除文件
     */
    public static void deleteFile(String filePath) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
