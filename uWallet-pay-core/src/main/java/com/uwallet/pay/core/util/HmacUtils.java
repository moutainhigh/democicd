package com.uwallet.pay.core.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * webmvc配置
 *
 * @author
 * @date 2019/04/09
 */
public class HmacUtils {
    /**
     * HMAC算法加密
     *
     * @param message 待加密信息
     * @return
     */
    public static String HmacSHA256(byte[] message, String encryptKey) {
        try {
            Mac hmacSha256Mac = Mac.getInstance("HMACSha256");
            SecretKeySpec secretKey = new SecretKeySpec(encryptKey.getBytes(), "HMACSha256");
            hmacSha256Mac.init(secretKey);
            byte[] result = hmacSha256Mac.doFinal(message);
            return Base64.encodeBase64String(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
