package com.uwallet.pay.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author baixinyue
 * @date 2019/12/12
 * @description 隐秘工具类
 */

public class AnonymityUtil {

    /**
     * 隐匿银行卡号 前4后4
     * @param cardNo
     * @return
     */
    public static String hideCardNo(String cardNo) {
        if(StringUtils.isBlank(cardNo)) {
            return cardNo;
        }
//        int beforeLength = 4;
        int afterLength = 4;
        String left = "";
        String right = "";
        int length = cardNo.length();
//        if(length >= beforeLength){
//            left = cardNo.substring(0,beforeLength);
//        }else{
//            left= cardNo;
//        }
        if(length>=afterLength){
            right = cardNo.substring(length-afterLength,length);
        }else{
            right = cardNo;
        }


//        //替换字符串，当前使用“*”
//        String replaceSymbol = "*";
//        StringBuffer sb = new StringBuffer();
//        for(int i=0; i<length; i++) {
//            if(i < beforeLength || i >= (length - afterLength)) {
//                sb.append(cardNo.charAt(i));
//            } else {
//                sb.append(replaceSymbol);
//            }
//        }

        return "**** "+right;
    }

    /**
     * 隐匿电话号码 前3后4
     * @param phoneNo
     * @return
     */
    public static String hidePhoneNo(String phoneNo) {
        if(StringUtils.isBlank(phoneNo)) {
            return phoneNo;
        }

        int length = phoneNo.length();
        int beforeLength = 3;
        int afterLength = 4;
        //替换字符串，当前使用“*”
        String replaceSymbol = "*";
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<length; i++) {
            if(i < beforeLength || i >= (length - afterLength)) {
                sb.append(phoneNo.charAt(i));
            } else {
                sb.append(replaceSymbol);
            }
        }

        return sb.toString();
    }

}
