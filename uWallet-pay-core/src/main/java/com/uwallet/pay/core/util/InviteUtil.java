package com.uwallet.pay.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author baixinyue
 * @createdDate 2020/09/08
 * @desctiption 邀请码生成工具类，也可用于其他生成六位数字加字母随机码
 */

public class InviteUtil {

    public static String getBindNum(Integer digits) throws Exception {
        String[] beforeShuffle = new String[] {
                "0", "2", "3", "4", "5", "6", "7", "8", "9",
                "A","B", "D", "C", "E", "F", "G", "H", "J",
                "0", "2", "3", "4", "5", "6", "7", "8", "9",
                "K", "L", "M", "N", "P", "Q", "R", "S", "T",
                "U", "V","W", "X", "Y", "Z" };
        List list = Arrays.asList(beforeShuffle);
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }
        String afterShuffle = sb.toString();
        String result = "";
        if (digits < afterShuffle.length()) {
            result = afterShuffle.substring(3, 3 + digits.intValue());
        } else {
            throw new Exception("Digits out of range");
        }
        return result;
    }

}
