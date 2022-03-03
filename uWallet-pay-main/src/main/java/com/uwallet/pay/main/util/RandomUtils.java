package com.uwallet.pay.main.util;

import com.uwallet.pay.core.util.MD5FY;

import java.util.Random;

/**
 * @author zhangzeyuan
 * @date 2021年03月23日 15:15
 */

public class RandomUtils {
    //所有字符
    /**
     * 数字+小写字母+大写字母
     */
    private static final String ALL_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 小写字母
     */
    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * 数字
     */
    private static final String NUMBER_CHAR = "0123456789";

    /**
     * 获取定长的随机数，包含大小写、数字
     *
     * @param length
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:17
     */
    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length())));
        }
        return sb.toString();
    }


    /**
     * 获取定长的随机数，包含大小写字母
     *
     * @param length
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:17
     */
    public static String generateMixString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(LETTER_CHAR.charAt(random.nextInt(LETTER_CHAR.length())));
        }
        return sb.toString();
    }


    /**
     * 获取定长的随机数，只包含小写字母
     *
     * @param length
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:17
     */
    public static String generateLowerString(int length) {
        return generateMixString(length).toLowerCase();
    }


    /**
     * 获取定长的随机数，只包含大写字母
     *
     * @param length
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:17
     */
    public static String generateUpperString(int length) {
        return generateMixString(length).toUpperCase();
    }


    /**
     * 获取定长的随机数，只包含数字
     *
     * @param length
     * @return java.lang.String
     * @author zhangzeyuan
     * @date 2021/3/23 15:17
     */
    public static String generateNumberString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_CHAR.charAt(random.nextInt(NUMBER_CHAR.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {

        //adaasds
//        merchantId&privateKey&random& timestamp
        String re = "534507564966825984" + "&" + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2MjU3MTMzMDIsInV1aWQiOiI4MjE2NzQxNDE1IiwiaWF0IjoxNjE3MDczMzAyfQ.rE6C3DD25ViBHPsmgIXtYWZQ-F1qdBbJ8yWycGQ7ot4"
                + "&" + "ltS7S5HECZ30&" + "1617083716000";

        String md5Sign = MD5FY.MD5Encode(re);
        System.out.println(md5Sign);

    }
}
