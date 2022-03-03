package com.uwallet.pay.main.util;

import com.alibaba.fastjson.JSONObject;
import com.uwallet.pay.main.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * CheckAppSignUtil
 *
 * @Author Laity
 */
@Slf4j
public class CheckAppSignUtil {

    /**
     * 密钥
     * @Author Laity
     */
    private final static String APP_KEY = "3.1415926UWallet&LoanCloud";
    /**
     * 密钥
     * @Author xucl
     */
    private final static String APP_KEY_H5 = "3.1415926UWallet&LoanCloud&pwp";

    /**
     * 解密
     * @Author Laity
     * @param info
     * @return java.lang.String
     */
    public static boolean decrypt(String info,HttpServletRequest request) throws Exception {
        JSONObject json = JSONObject.parseObject(info);
        String sign = json.getString("sign");
        String timeStamp = json.getString("timestamp");
        String md5Sign = DigestUtils.md5Hex(APP_KEY + timeStamp);
        Long a = System.currentTimeMillis();
        Long diff = a - Long.valueOf(timeStamp);
        log.info("sign:{}, our:{}, out:{}, diff:{}", sign, a, timeStamp, diff);
        if (diff < Constant.THRESHOLD_SECOND_DOWN || diff > Constant.THRESHOLD_SECOND) {
            return false;
        }
        if (!sign.equals(md5Sign)) {
            return false;
        }
        return true;
    }
    /**
     * 解密H5
     * @Author xucl
     * @param info
     * @return java.lang.String
     */
    public static boolean decryptH5(String info,HttpServletRequest request) throws Exception {
        JSONObject json = JSONObject.parseObject(info);
        String sign = json.getString("sign");
        String timeStamp = json.getString("timestamp");
        String md5Sign = DigestUtils.md5Hex(APP_KEY_H5 + timeStamp);
        Long a = System.currentTimeMillis();
        Long diff = a - Long.valueOf(timeStamp);
        log.info("sign:{}, our:{}, out:{}, diff:{}", sign, a, timeStamp, diff);
        if (diff < Constant.THRESHOLD_SECOND_DOWN || diff > Constant.THRESHOLD_SECOND) {
            return false;
        }
        if (!sign.equals(md5Sign)) {
            return false;
        }
        return true;
    }

}
