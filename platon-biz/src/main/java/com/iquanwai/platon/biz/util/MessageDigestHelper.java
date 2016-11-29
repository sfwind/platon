package com.iquanwai.platon.biz.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by yangyuchen on 15-1-30.
 */
public class MessageDigestHelper {
    public static String getSHA1String(String s) {
        return getHash(s, "SHA-1").toLowerCase();
    }

    public static String getMD5String(String s) {
        return getHash(s, "MD5");
    }

    private static String getHash(String s, String algorithm) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            //ignore
        }
        md5.update(s.getBytes());
        byte[] codedBytes = md5.digest();
        //将加密后的字节数组转换成字符串
        return bytesToHexString(codedBytes);
    }

    private static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

}
