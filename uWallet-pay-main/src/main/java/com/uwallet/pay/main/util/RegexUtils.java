package com.uwallet.pay.main.util;

import com.uwallet.pay.core.util.Validator;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    /**
     * 正则表达式：验证密码
     */
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";
    /**
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";

    /**
     * 正则表达式：验证邮箱
     * 2021-04-02
     * ^[0-9A-Za-z][0-9AZa-z|(\!|\#|\$|\%|\&|\'|\*|\+|\-|\/|\=|\?|\^|\_|\`|\{|\||\}|\~|\.)]{7,23}[0-9A-Za-z]&?@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\.)+[a-zA-Z]{2,}$
     */
//    public static final String REGEX_EMAIL =  "^[0-9A-Za-z][0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{0,35}[0-9A-Za-z]@([a-z0-9A-Z]{0,10}(-[a-z0-9A-Z]+)?|\\.)+[a-zA-Z]{1,10}$";
    public static final String REGEX_EMAIL =  "^[0-9A-Za-z][0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{0,35}[0-9A-Za-z]@[0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{1,20}$";
    /**
     * 正则表达式：验证邮箱特殊符号重复
     */
    public static final String REGEX_EMAIL_FIRST="[\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.]{2,}";
    /**
     * 正则表达式：校验交易金额
     */
    public static final String REGEX_TRANSAMT = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";

    /**
     * 正则表达式：修改手机校验手机号
     */
    public static final String REGES_UPPHONE = "\\d{1,13}$";

    /**
     * 正则表达式：澳洲邮编
     */
    public static final String REGES_AU_POSTCODE = "^([0-9]{4})";

    public static void main(String[] args) {
//        System.out.println(isAuPostcode(null));
//        Pattern pattern = Pattern.compile(REGEX_EMAIL);
//        Matcher matcher = pattern.matcher("Ttyyyyy@qq.c2121212121212121om");
//        if (matcher.find()){
//            System.out.println(1);
//        }
        boolean email = RegexUtils.isEmail("43131097897@qqqq.c");

        System.out.println(email);
    }
    /**
     * 澳洲邮编
     *
     * @param password
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isAuPostcode(String password) {
        return Pattern.matches(REGES_AU_POSTCODE, password);
    }


    /**
     * 校验密码
     *
     * @param password
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isPassword(String password) {
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    /**
     * 校验手机号
     *
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    /**
     * 校验邮箱
     *
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail(String email) {
        // 校验重复特殊符号
        Pattern pattern = Pattern.compile(REGEX_EMAIL_FIRST);
        Matcher matcher = pattern.matcher(email);
        if (matcher.find()){
            return false;
        }
        return Pattern.matches(REGEX_EMAIL, email);
    }

    /**
     * 校验邮箱
     *
     * @param amount
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isTransAmt(String amount) {
        return Pattern.matches(REGEX_TRANSAMT, amount);
    }

    public static boolean checkPhone(String... phones){
        for (int i = 0; i > phones.length; i++){
            if (!Pattern.matches(REGES_UPPHONE, phones[i])){
                return false;
            }
        }
        return true;
    }

}
