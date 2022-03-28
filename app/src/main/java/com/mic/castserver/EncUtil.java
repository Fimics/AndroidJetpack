package com.mic.castserver;

import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("unused")
public class EncUtil {

    private static final String DEFAULT_CODING = "utf-8";

    @SuppressLint("GetInstance")
    public static String decrypt(String encrypted, String seed) throws Exception {
        byte[] keyBytes = seed.getBytes(DEFAULT_CODING);
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] theDigest = md.digest(keyBytes);
        SecretKeySpec secretKeySpec = new SecretKeySpec(theDigest, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] clearByte = cipher.doFinal(toByte(encrypted));
        return new String(clearByte);
    }

    public static String encrypt(String content, String key) throws Exception {
        byte[] input = content.getBytes(DEFAULT_CODING);

        return encrypt(input, key);
    }

    @SuppressLint("GetInstance")
    private static String encrypt(byte[] input, String key) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, ShortBufferException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] theDigest = md.digest(key.getBytes(DEFAULT_CODING));
        SecretKeySpec skc = new SecretKeySpec(theDigest, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skc);

        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int textLength = cipher.update(input, 0, input.length, cipherText, 0);
        //textLength += cipher.doFinal(cipherText, textLength);
        return parseByte2HexStr(cipherText);
    }

    @SuppressLint("GetInstance")
    public static String encrypt(byte[] input, byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, ShortBufferException{
        SecretKeySpec skc = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skc);

        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int textLength = cipher.update(input, 0, input.length, cipherText, 0);
        //textLength += cipher.doFinal(cipherText, textLength);

        return parseByte2HexStr(cipherText);
    }


    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b:buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }
}

