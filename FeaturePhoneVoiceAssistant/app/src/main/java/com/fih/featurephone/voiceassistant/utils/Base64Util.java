package com.fih.featurephone.voiceassistant.utils;

/**
 * Base64 工具类
 */
public class Base64Util {

    private static final char last2byte = (char) Integer.parseInt("00000011", 2);
    private static final char last4byte = (char) Integer.parseInt("00001111", 2);
    private static final char last6byte = (char) Integer.parseInt("00111111", 2);
    private static final char lead6byte = (char) Integer.parseInt("11111100", 2);
    private static final char lead4byte = (char) Integer.parseInt("11110000", 2);
    private static final char lead2byte = (char) Integer.parseInt("11000000", 2);
    private static final char[] encodeTable = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    public Base64Util() {
    }

    public static String encode(byte[] from) {
        StringBuilder to = new StringBuilder((int) ((double) from.length * 1.34D) + 3);
        int num = 0;
        char currentByte = 0;

        int i;
        for (i = 0; i < from.length; ++i) {
            for (num %= 8; num < 8; num += 6) {
                switch (num) {
                    case 0:
                        currentByte = (char) (from[i] & lead6byte);
                        currentByte = (char) (currentByte >>> 2);
                        break;
                    case 1:
                    case 3:
                    case 5:
                    default:
                        break;
                    case 2:
                        currentByte = (char) (from[i] & last6byte);
                        break;
                    case 4:
                        currentByte = (char) (from[i] & last4byte);
                        currentByte = (char) (currentByte << 2);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead2byte) >>> 6);
                        }
                        break;
                    case 6:
                        currentByte = (char) (from[i] & last2byte);
                        currentByte = (char) (currentByte << 4);
                        if (i + 1 < from.length) {
                            currentByte = (char) (currentByte | (from[i + 1] & lead4byte) >>> 4);
                        }
                        break;
                }

                to.append(encodeTable[currentByte]);
            }
        }

        if (to.length() % 4 != 0) {
            for (i = 4 - to.length() % 4; i > 0; --i) {
                to.append("=");
            }
        }

        return to.toString();
    }

 /*   public static byte[] decode(String imgStr) {
        // 对字节数组字符串进行Base64解码并生成图片
        if (imgStr == null) {
            return null;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            // Base64解码
            byte[] b = decoder.decodeBuffer(imgStr);
            for (int i = 0; i < b.length; ++i) {
                // 调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }*/

    /**
     * 通过图片base64流判断图片等于多少字节
     * image 图片流
     */
    public static Integer getSize(String imgBase64) {
        if (imgBase64 == null || imgBase64.length() == 0) {
            return 0;
        }
        // 1.找到等号，把等号也去掉
        Integer equalIndex = imgBase64.indexOf("=");
        if (imgBase64.indexOf("=") > 0) {
            imgBase64 = imgBase64.substring(0, equalIndex);
        }
        // 2.原来的字符流大小，单位为字节
        Integer strLength = imgBase64.length();
        // 3.计算后得到的文件流大小，单位为字节
        Integer size = strLength - (strLength / 8) * 2;
        return size;
    }

    /**
     * 判定字符串是否为base64编码
     *
     * @param base64
     * @return
     */
    public static boolean isBase64(String base64) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        if (base64.lastIndexOf(",") != -1) {
            base64 = base64.substring(base64.lastIndexOf(",") + 1);
        }
        Boolean isLegal = base64.matches(base64Pattern);
        if (isLegal) {
            return true;
        }
        return false;
    }

}
