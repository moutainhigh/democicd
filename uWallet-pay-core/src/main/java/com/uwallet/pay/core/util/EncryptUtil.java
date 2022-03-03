package com.uwallet.pay.core.util;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description：加解密
 *
 * @date：2019/5/22
 * @author：Rainc
 */
@Slf4j
public class EncryptUtil {

    /**
     * 盐
     */
    public static final String SALT = "!@#rloan$data^Rainc";

    /**
     * SHA256加密盐
     */
    private static String encryptSalt;
    /**
     * 密钥
     */
    public static String aesKey;
    /**
     * 向量
     */
    public static String aesIv;

    static {
        // SHA256加密盐
        encryptSalt = EncryptUtil.encryptToSHA256(EncryptUtil.SALT);
        // 加密盐前16位作为密钥
        aesKey = EncryptUtil.getAESKey(encryptSalt);
        // 加密盐16到32位作为向量
        aesIv = EncryptUtil.getAESIv(encryptSalt);
    }

    /**
     * 进行SHA256加密
     *
     * @param str 要加密的信息
     * @return encodeStr 加密后的字符串
     */
    public static String encryptToSHA256(String str) {
        String encodeStr = "";
        try {
            // 得到一个SHA-256的消息摘要
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            // 添加要进行计算摘要的信息
            messageDigest.update(str.getBytes("UTF-8"));
            // 得到该摘要并转为字符串
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 取SHA256加密的盐的前16位作为私钥
     *
     * @param str
     * @return
     */
    public static String getAESKey(String str) {
        return str.substring(0, 16);
    }

    /**
     * 取SHA256加密的盐的16--32位作为向量
     *
     * @param str
     * @return
     */
    public static String getAESIv(String str) {
        return str.substring(16, 32);
    }

    /**
     * 创建一个AES的密钥
     *
     * @return SecretKey 秘密（对称）密钥
     */
    public SecretKey createSecretAESKey() {
        // 声明KeyGenerator对象
        KeyGenerator keygen;
        // 声明 密钥对象
        SecretKey deskey = null;
        try {
            // 返回生成指定算法的秘密密钥的 KeyGenerator 对象
            keygen = KeyGenerator.getInstance("AES");
            // 生成一个密钥
            deskey = keygen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 返回密匙
        return deskey;
    }

    /**
     * 十六进制字符串转化为二进制
     *
     * @param strhex
     * @return
     */
    public static byte[] hex2Byte(String strhex) {
        if (strhex == null) {
            return null;
        }
        int l = strhex.length();
        if (l % 2 == 1) {
            return null;
        }
        byte[] b = new byte[l / 2];
        for (int i = 0; i != l / 2; i++) {
            b[i] = (byte) Integer.parseInt(strhex.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    /**
     * 将二进制转化为16进制字符串
     *
     * @param b 二进制字节数组
     * @return String
     */
    public static String byte2Hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * 加密
     *
     * @param sSrc 加密的明文
     * @return
     * @throws Exception
     */
    public static String encrypt(String sSrc) throws Exception {
        if (aesKey == null) {
            throw new Exception("密钥为空");
        }
        if (aesKey.length() != 16) {
            throw new Exception("密钥长度必须16位");
        }
        if (aesIv.length() != 16) {
            throw new Exception("向量长度必须16位");
        }
        byte[] raw = aesKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv1 = new IvParameterSpec(aesIv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv1);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes());
        return new BASE64Encoder().encode(encrypted);
    }

    /**
     * 解密
     * @param sSrc 接收到的加密过后的字符串（带解密密文）
     * @param sKey 秘钥
     * @return
     */
    public static String decrypt(String sSrc, String sKey, String iv) {
        try {
            if (sKey == null) {
                throw new Exception("密钥为空");
            }
            if (sKey.length() != 16) {
                throw new Exception("密钥长度必须16位");
            }
            if (iv.length() != 16) {
                throw new Exception("向量长度必须16位");
            }
            // 先用Base64解码
            byte[] byte1 = Base64.decode(sSrc);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            SecretKeySpec key = new SecretKeySpec(sKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            // 与加密时不同MODE:Cipher.DECRYPT_MODE
            byte[] ret = cipher.doFinal(byte1);
            return new String(ret, "utf-8");
        } catch (Exception e) {
           log.error(e.getMessage(), e);
        }
        return null;
    }
}
