package com.uwallet.pay.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验器：利用正则表达式校验邮箱、手机号等
 * @author baixinyue
 */
public class Validator {

    /**
     * 常规文本长度
     */
    public static final Integer TEXT_LENGTH_3 = 3;

    public static final Integer TEXT_LENGTH_10 = 10;

    public static final Integer TEXT_LENGTH_80 = 80;

    public static final Integer TEXT_LENGTH_100 = 100;

    public static final Integer TEXT_LENGTH_255 = 255;

    public static final Integer TEXT_LENGTH_1000 = 1000;

    /**
     * 渠道费率最大值
     */
    public static final Integer RATE_MAX = 100;

    /**
     * 渠道费率最小值
     */
    public static final Integer RATE_MIN = 0;
    /**
     * 手机长度
     */
    public static final Integer PHONE_LENGTH = 11;

    public static final Integer PHONE_AUD_LENGTH = 9;
    public static final Integer ABN = 11;
    public static final Integer ACN = 9;
    /**
     * 密码最小长度
     */
    public static final Integer USER_PASSWORD_MIN_LENGTH = 6;

    /**
     * 密码最大长度
     */
    public static final Integer USER_PASSWORD_MAX_LENGTH = 12;

    /**
     * 驾照长度
     */
    public static final Integer DRIVER_LICENSE_LENGTH = 20;

    /**
     * 护照长度
     */
    public static final Integer PASSPORT_LENGTH = 9;
    /**
     * 澳大利亚手机号长度
     */
    public static final Integer PHONE_LENGTH_AU = 11;

    /**
     * 护照长度
     */
    public static final Integer PASSPORT_LENGTH1 = 8;

    /**
     * tfn税号长度
     */
    public static final Integer TFN_LENGTH = 9;

    /**
     * 身份证号长度
     */
    public static final Integer ID_NO = 18;

    /**
     * bsb码
     */
    public static final Integer BSB_NO_LENGTH = 6;

    /**
     * 银行账户号长度区间
     */
    public static final Integer BANK_ACCOUNT_NAME_MIN_LENGTH = 6;

    public static final Integer BANK_ACCOUNT_NAME_MAX_LENGTH = 10;

    /**
     * 银行卡号长度区间
     */
    public static final Integer BANK_CARD_MIN_LENGTH = 15;

    public static final Integer BANK_CARD_MAX_LENGTH = 20;

    /**
     * 安全码
     */
    public static final Integer CC_CVC_MIN_LENGTH = 3;

    public static final Integer CC_CVC_MAX_LENGTH = 4;

    /**
     * 邮编长度
     */
    public static final Integer BANK_ZIP_LENGTH = 4;

    /**
     * 正则表达式:验证金额(正数,小数点后两位)
     */
    public static final String REGEX_AMOUNT= "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$";

    /**
     * 正则表达式:验证用户姓名
     * */
    public static final String REGEX_NAME="^[a-zA-Z0-9\\s*\\u4e00-\\u9fa5]{1,100}$";

    /**
     * 正则表达式:验证用户名(不包含中文和特殊字符)如果用户名使用手机号码或邮箱 则结合手机号验证和邮箱验证
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z\\d]{0,20}$";

    /**
     * 正则表达式:验证密码(不包含特殊字符)
     */
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";

    /**
     *  <br>　　　　　2019年1月16日已知
     *         中国电信号段
     *          133,149,153,173,174,177,180,181,189,199
     *         中国联通号段
     *          130,131,132,145,146,155,156,166,175,176,185,186
     *         中国移动号段
     *          134(0-8),135,136,137,138,139,147,148,150,151,152,157,158,159,165,178,182,183,184,187,188,198
     *         上网卡专属号段（用于上网和收发短信，不能打电话）
     *          如中国联通的是145
     *         虚拟运营商
     *          电信：1700,1701,1702
     *          移动：1703,1705,1706
     *          联通：1704,1707,1708,1709,171
     *      卫星通信： 1349 <br>　　　　　未知号段：141、142、143、144、154
     */
    public static final String REGEX_MOBILE = "^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";

    /**
     * 正则表达式:验证邮箱
     * 2021/03/30版本：^[0-9A-Za-z][0-9AZa-z|(\!|\#|\$|\%|\&|\'|\*|\+|\-|\/|\=|\?|\^|\_|\`|\{|\||\}|\~|\.)]{0,23}[0-9A-Za-z]&?@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\.)+[a-zA-Z]{2,}$
     */
//    public static final String REGEX_EMAIL =  "^[0-9A-Za-z][0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{0,35}[0-9A-Za-z]@([a-z0-9A-Z]{0,10}(-[a-z0-9A-Z]+)?|\\.)+[a-zA-Z]{1,10}$";
//    public static final String REGEX_EMAIL =  "^[0-9A-Za-z][0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{0,35}[0-9A-Za-z]@[a-zA-Z0-9][a-zA-Z0-9\\-]*[\\.][A-Za-z]{2,6}$";
    public static final String REGEX_EMAIL =  "^[0-9A-Za-z][0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{0,64}[0-9A-Za-z]@[0-9A-Za-z|(\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.)]{1,255}$";


    /**
     * 正则表达式：验证邮箱特殊符号重复
     */
    public static final String REGEX_EMAIL_FIRST="[\\!|\\#|\\$|\\%|\\&|\\'|\\*|\\+|\\-|\\/|\\=|\\?|\\^|\\_|\\`|\\{|\\||\\}|\\~|\\.]{2,}";
    /**
     * 正则表达式:验证汉字(1-9个汉字)  {1,9} 自定义区间
     */
    public static final String REGEX_CHINESE = "^[\u4e00-\u9fa5]{1,9}$";

    /**
     * 正则表达式:验证身份证
     */
    public static final String REGEX_ID_CARD = "(\\d{14}[0-9a-zA-Z])|(\\d{17}[0-9a-zA-Z])";

    /**
     * 正则表达式:验证URL
     */
    public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

    /**
     * 正则表达式:验证IP地址
     */
    public static final String REGEX_IP_ADDR = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
    /**
     * 正则表达式:支付密码复杂密码验证
     */
    public static final String REGEX_TRACEPASSWORD="(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z~!@#$%^&*()_+-=\\{}\\[\\]\\|\\\\;':\"\"<>,.?/]{8,16}";
    /**
     * 正则表达式:管理员密码复杂密码验证
     */
    public static final String REGEX_ADMINPASSWORD="(?![0-9]+$)(?![a-zA-Z]+$)(?![!@#$%^&*()-=_+\\[\\]{}|,./<>?;':\"`~]+$)[0-9A-Za-z\\W_!@#$%^&*()-=_+\\[\\]{}|,./<>?;':\"`~]{6,20}";
    /**
     * 校验用户名
     *
     * @param username
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUserName(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
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
     * 校验姓名
     * */
    public static boolean isName(String name) {
        return Pattern.matches(REGEX_NAME, name);
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
     * 校验汉字
     *
     * @param chinese
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isChinese(String chinese) {
        return Pattern.matches(REGEX_CHINESE, chinese);
    }

    /**
     * 校验身份证
     *
     * @param idCard
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isIDCard(String idCard) {
        return Pattern.matches(REGEX_ID_CARD, idCard);
    }

    /**
     * 校验URL
     *
     * @param url
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUrl(String url) {
        return Pattern.matches(REGEX_URL, url);
    }

    /**
     * 校验IP地址
     *
     * @param ipAddress
     * @return
     */
    public static boolean isIPAddress(String ipAddress) {
        return Pattern.matches(REGEX_IP_ADDR, ipAddress);
    }


    /**
     * 校验复杂支付密码
     *
     * @param tracePassword
     * @return
     */
    public static boolean isTracePassword(String tracePassword) {
        return Pattern.matches(REGEX_TRACEPASSWORD, tracePassword);
    }


    /**
     * 验证金钱
     * @param str
     * @return
     */
    public static boolean isAmount(String str) {
        // 判断小数点后2位的数字的正则表达式
        Pattern pattern = Pattern.compile(REGEX_AMOUNT);
        Matcher match = pattern.matcher(str);
        return match.matches();

    }

    /**
     * 校验复杂管理员密码
     *
     * @param tracePassword
     * @return
     */
    public static boolean isAdminPassword(String tracePassword) {
        return Pattern.matches(REGEX_ADMINPASSWORD, tracePassword);
    }


    public static void main(String[] args) {
        String email = "1@2#3$4%5^6&7*89012}3|4{5?678@qq.com";
        boolean email1 = isEmail(email);
        System.out.println(email1);
//        if (StringUtils.isBlank(email) || email.length() > Validator.TEXT_LENGTH_100 || !(Validator.isEmail(email))) {
//            System.out.println("error");
//        }else {
//            System.out.println("yes");
//        }

        System.out.println(SnowflakeUtil.generateId());

    }

}
