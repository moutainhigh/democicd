package com.uwallet.pay.main.constant;

import io.swagger.models.auth.In;

import java.math.BigDecimal;

/**
 *
 * @author: Rainc
 * @date: Created in 2019-07-30 09:03:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
public class Constant {

    /**
     * 管理员标识
     */
    public static final String ADMIN = "uWalletPayAdmin";

    /**
     * 供应商标识
     */
    public static final String MERCHANT = "merchant";


    /**
     * postman请求token中携带的标识
     */
    public static final String BEARER = "Bearer ";

    /**
     * 角色Id
     */
    public static final String ROLE = "roleId";

    /**
     * 用户登陆账号锁头前缀
     */
    public static final String LOGIN_LOCK = "loginLock-";

    /**
     * admin中的userName
     */
    public static final String USERNAME = "userName";

    /**
     * admin中的realName
     */
    public static final String REALNAME = "realName";

    /**
     * redis中权限key标识
     */
    public static final String ACTION = "action";

    /**
     * 开始时间
     */
    public static final String START = "start";

    /**
     * 结束时间
     */
    public static final String END = "end";

    /**
     * 邮箱
     */
    public static final String EMAIL = "email";


    /**
     * LatPay银行响应类型 通过
     */
    public static final String LP_BANK_STATUS_00 = "00";

    /**
     * rLatPay银行响应类型 拒绝
     */
    public static final String LP_BANK_STATUS_05 = "05";


    /**
     * LatPay银行响应类型 通讯失败
     */
    public static final String LP_BANK_STATUS_90 = "90";

    /**
     * LatPay银行响应类型 通讯失败
     */
    public static final String LP_BANK_STATUS_91 = "91";

    /**
     * app、系统间验签时长
     */
    public static final Integer THRESHOLD_SECOND = 60000;

    public static final Integer THRESHOLD_SECOND_DOWN = -60000;

    /**
     * OmiPay验签时长
     */
    public static final Integer OMI_PAY_HOLD_SECOND = 50000;

    /**
     * 交易流水号
     */
    public static final String FLOW_ID = "flowId";

    /**
     * 交易流水号
     */
    public static final String ORDRE_NO = "ordreNo";

    /**
     * 分期付系统返回KYC状态
     */
    public static final String TRUE = "true";

    /**
     * 系统间调用取值的key
     */
    public static final String VERIFIED = "verified";

    /**
     * kyc是否满足18岁
     */
    public static final String DATE = "isDateOfBirth";

    /**
     * code
     */
    public static final String CODE = "code";

    /**
     * 商家推荐个数
     */
    public static final Integer MERCHANT_TOP = 2;

    /**
     * null
     */
    public static final String NULL = "null";

    public static final String ONE = "1";
    public static final int ZERO = 0;
    public static final Integer TWO = 2;
    public static final Integer FOUR = 4;
    public static final Integer SIX = 6;
    /**
     * app商户发送到分期付的平台ID
     */
    public static final Long CREDIT_MERCHANT_ID = 1000L;

    /**
     * 用户卡列表在redis中的后缀 前面拼上userId
     */
    public static final String REDIS_USER_CARD_LIST_KEY = "_card";
    public static final Integer THREE = 3;
    public static final Integer DATA_SYSTEM_SERVICE_VALUE = 3003;
    public static final Integer PAY_SYSTEM_CODE = 1;

    /**
     * Redis中 top 10 tag 的key
     */
    public static final String REDIS_TOP10_TAG_KEY = "REDIS_TOP10_TAG";
    /**
     * Redis中 tag全量数据
     */
    public static final String REDIS_FULL_TAG_DATA_KEY = "REDIS_FULL_TAG_DATA";
    public static final String THREE_KM = "3";
    public static final String ONE_POINT_FIVE_KM = "1.5";
    public static final String SIX_KM = "6";
    public static final String FIVE_KM = "5";
    public static final String IP = "0.0.0.1";
    public static final Long ADMIN_ID = 6666L;
    public static final String DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss";
    //public static final String THREE_KM = "3";
    //public static final String THREE_KM = "3";
    /**
     * 单卡交易失败次数 redis key
     */
    public static final String CARD_PAY_FAILED_TIME_COUNT = "CARD_PAY_FAILED_TIME_COUNT";
    /**
     * 用户每天交易失败次数 redis key
     */
    public static final String USER_CARD_PAY_FAILED_TIME_TOTAL = "USER_CARD_PAY_FAILED_TIME_TOTAL";
    /**
     * 系统配置配置的当天最大,单卡最大-->失败次数
     */
    public static final String USER_PAY_FAILED_MAX_CONFIG = "USER_PAY_FAILED_MAX";
    public static final String CARD_PAY_FAILED_SINGLE_CONFIG = "CARD_PAY_FAILED_SINGLE";
    /**
     * 提示信息中的占位符 MESSAGE_MARKER
     */
    public static final String MESSAGE_MARKER = "-{xx}-";

    /**
     * pos验签 随机数最小位数
     */
    public static final Integer POS_SIGN_RANDOM_LENGTH_MIN = 10;

    /**
     * pos验签 随机数最大位数
     */
    public static final Integer POS_SIGN_RANDOM_LENGTH_MAX = 32;

    /**
     * pos请求时间过期范围
     */
    public static final Integer POS_REQUEST_EXPIRED_TIME_MIN = -15;

    /**
     *  0：删除 1：使用
     */

    public static final Integer POS_RECORD_USABLE_STATUS = 1;

    /**
     *  默认分页参数
     */
    public static final Integer POS_RECORD_DEFAULT_START_INDEX = 0;
    public static final Integer POS_RECORD_DEFAULT_PAGE = 1;
    public static final Integer POS_RECORD_DEFAULT_LIMIT = 20;

    /**
     *  回调通知状态 0 未通知 1 通知成功 2 通知失败
     */
    public static final Integer POS_NOTIFY_SUCCESS = 1;
    public static final Integer POS_NOTIFY_FAIL = 2;




    /**
     *
     * 订单类型 1 pos支付订单 2 正常扫码支付订单
     *
     */
    public static final Integer ORDER_TYPE_POS = 1;
    public static final Integer ORDER_TYPE_NORMAL = 2;



    /**
     * 合同类型  1 纸质 0 电子
     */
    public static final Integer CONTRACT_PAPER = 1;
    public static final Integer CONTRACT_ELECTRONIC  = 0;
    /**
      redis 过期时间 15分钟 15 个60秒 minutes
     */
    public static final Long MINUTES_15 = 15*60L;

    public static final String PAY_DISTANCE = "100";

    /**
     * APP banner 更新时间戳 redis key
     */
    public static final String APP_BANNER_UPDATE_TIMESTAMP_REDIS_KEY = "APP_BANNER_UPDATE_TIMESTAMP";

    /**
     * APP banner 限制次数
     */
    public static final String APP_BANNER_DISPLAY_LIMITS = "APP_BANNER_DISPLAY_LIMITS";




    /**
     * APP 市场推广banner 更新时间戳 redis key
     */
    public static final String APP_EXCLUSIVE_BANNER_UPDATE_TIMESTAMP_REDIS_KEY = "APP_EXCLUSIVE_BANNER_UPDATE_TIMESTAMP";


    /**
     * APP 自定义分类 更新时间戳 redis key
     */
    public static final String APP_CUSTOM_CATEGORY_UPDATE_TIMESTAMP_REDIS_KEY = "APP_CUSTOM_CATEGORY_UPDATE_TIMESTAMP";

    /**
     * APP首页获取自定义分类时用户所在州信息 redis key
     */
    public static final String APP_HOMEPAGE_CATEGORY_USER_STATE = "APP_HOMEPAGE_CATEGORY_USER_STATE";
    /**
     * 用户第一笔交易后 2小时候发送邮件redis key
     */
    public static final String USER_FIRST_PAY_AFTER_2HOURS_SEND_EMAIL_REDIS_KEY = "USER_FIRST_PAY_AFTER_2HOURS_SEND_EMAIL_";

    /**
     * 订单详情金额 折扣 等信息 redis key
     */
    public static final String ORDER_AMT_INFO = "ORDER_AMT_INFO";

    /**
     * 分期付 需要进行卡支付 的最小 交易金额
     */
    public static final String PAY_CREDIT_NEED_CARDPAY_MIN_MONEY = "10";

    /**
     * 分期付 需要进行卡支付 比例
     */
    public static final String PAY_CREDIT_NEED_CARDPAY_AMOUNT_RATE = "0.25";

    /**
     * 发送短信验证码手机 redis key
     */
    public static final String SMS_SEND_PHONE_REDIS_KEY_PREFIX = "SMS_PHONE_";


    /**
     * 发送短信验证码 redis key
     */
    public static final String SMS_SEND_CODE_REDIS_KEY_PREFIX = "SMS_CODE_";

    /**
     * 发送短信验证码 校验次数 redis key
     */
    public static final String SMS_SEND_CODE_CHECK_TIME_REDIS_KEY_PREFIX = "SMS_CODE_TIMES";

    /**
     * APP 自定义分类 更新时间戳 redis key
     */
    public static final Integer APP_BANNER_LIMITS_UNLIMITED = 9999999;
    public static final long ONE_DAY_MILS = 86400000L;
    /**
     * 一个星期的 秒 值
     */
    public static final long SEVEN_DAY_SEC = 86400L * 7;


    /**
     * 商户分类EXCLUSIVE_OFFER 6
     */
    public static final String MERCHANT_CATEGORY_EXCLUSIVE_OFFER_VALUE = "6";

    /**
     * 卡支付订单标识
     *
     * */
    public static final String ORDER_TRANS_TYPE_CARD_FLAG = "TPC";
    /**
     * 分期付订单标识
     */
    public static final String ORDER_TRANS_TYPE_CREDIT_FLAG = "INS";
    /**
     * 用户地理位置缓存key
     */
    public static final String USER_LAST_PROVINCE = "USER_LAST_PROVINCE";

    /**
     * 小费清算文件名序号redis key
     */
    public static final String REDIS_KEY_TIP_SETTLEMENT_FILE_NUMBER = "tip_settlement_file_number_";

    /**
     * 小费清算文件名前缀
     */
    public static final String TIP_SETTLEMENT_FILE_NAME_PREFIX = "tip_settlement_";

    /**
     * 卡号前缀
     */
    public static final String STRIPE_CARD_NUMBER_PREFIX = "**** **** **** ";
    /**
     * 获取redis中 banner对应的限制次数 的key
     * @author zhangzeyuan
     * @date 2021/4/23 16:15
     * @param id
     * @param userId
     * @return java.lang.String
     */
    public static String getBannerLimitsRedisKey(Long id, Long userId){
        return Constant.APP_BANNER_DISPLAY_LIMITS + "_" + userId + "_" + id;
    }


    /**
     * 获取app首页banner 更新时间戳 redis key
     * @author zhangzeyuan
     * @date 2021/4/27 19:55
     * @param id
     * @return java.lang.String
     */
    public static String getBannerUpdateTimestampRedisKey(Long id){
        return Constant.APP_BANNER_UPDATE_TIMESTAMP_REDIS_KEY + "_" + id;
    }

    /**
     * 获取app首页自定义分类 更新时间戳 redis key
     * @author zhangzeyuan
     * @date 2021/4/27 19:55
     * @param order
     * @param stateName
     * @return java.lang.String
     */
    public static String getCustomCategoriesUpdateTimestampRedisKey(Integer order, String stateName){
        return Constant.APP_CUSTOM_CATEGORY_UPDATE_TIMESTAMP_REDIS_KEY + "_" + order + "_" + stateName;
    }


    /**
     * 获取APP首页自定义分类传入用户所在州信息
     * @author zhangzeyuan
     * @date 2021/5/20 21:45
     * @param userId
     * @return java.lang.String
     */
    public static String getAppHomepageCategoryUserState(Long userId){
        return Constant.APP_HOMEPAGE_CATEGORY_USER_STATE + "_" + userId;
    }


    /**
     *  获取订单折扣 金额等信息
     * @author zhangzeyuan
     * @date 2021/6/30 18:55
     * @param userId
     * @param transOrderNO
     * @return java.lang.String
     */
    public static String getOrderAmountDetailRedisKey(String userId, String transOrderNO){
        return Constant.ORDER_AMT_INFO + "_" + userId + "_" + transOrderNO;
    }
    /**
     *  用户在redis中缓存的所在州信息
     * @author zhangzeyuan
     * @date 2021/6/30 18:55
     * @param userId
     * @return java.lang.String
     */
    public static String getUserProvinceInfo(Long userId){
        return Constant.USER_LAST_PROVINCE + "_" + userId;
    }



    /**
     * 获取发送验证码的手机redis key
     * @author zhangzeyuan
     * @date 2021/8/30 19:53
     * @param phoneNumber
     * @param nodeType
     * @param userType
     * @return java.lang.String
     */
    public static String getSendMobileRedisKey(String phoneNumber, Integer nodeType, Integer userType){
        return Constant.SMS_SEND_PHONE_REDIS_KEY_PREFIX + phoneNumber + "_"  + nodeType + "_" + userType;
    }


    /**
     * 获取发送验证码redis key
     * @author zhangzeyuan
     * @date 2021/8/30 19:53
     * @param phoneNumber
     * @param nodeType
     * @param userType
     * @return java.lang.String
     */
    public static String getSendCodeRedisKey(String phoneNumber, String expireCode, Integer nodeType, Integer userType){
        return Constant.SMS_SEND_CODE_REDIS_KEY_PREFIX + expireCode + phoneNumber + "_"  + nodeType + "_" + userType;
    }


    /**
     * 短信验证码验证次数
     * @author zhangzeyuan
     * @date 2021/8/30 19:53
     * @param phoneNumber
     * @param nodeType
     * @param userType
     * @return java.lang.String
     */
    public static String getSMSCodeCheckTimesRedisKey(String phoneNumber, Integer nodeType, Integer userType){
        return Constant.SMS_SEND_CODE_CHECK_TIME_REDIS_KEY_PREFIX + phoneNumber + "_"  + nodeType + "_" + userType;
    }




    /**
     *  首次交易2小时后发送邮件redis key
     * @author zhangzeyuan
     * @date 2021/9/26 15:35
     * @param phoneNumber
     * @return java.lang.String
     */
    public static String getFirstPaySendMailAfter2HoursRedisKey(String phoneNumber){
        return Constant.USER_FIRST_PAY_AFTER_2HOURS_SEND_EMAIL_REDIS_KEY + phoneNumber;
    }
}
