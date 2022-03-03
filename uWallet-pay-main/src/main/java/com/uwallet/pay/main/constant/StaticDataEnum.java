package com.uwallet.pay.main.constant;

import com.uwallet.pay.main.exception.SignException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

/**
 *
 * @description: 数据库字段状态
 * @author: Rainc
 * @date: Created in 2019-07-30 09:03:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: Rainc
 */
@Getter
public enum StaticDataEnum {
    /**
     * 卡券状态
     */
    MARKETING_STATE_1(1,"未使用"),
    MARKETING_STATE_2(2,"已使用"),
    /**
     * H5 商户等级
     */
    H5_MERCHANT_CLASS_0(0,"一级"),
    H5_MERCHANT_CLASS_1(1,"二级"),
    /**
     * 订单是否在app上显示
     */
    ORDER_SHOW_STATE_0(0,"不显示"),
    ORDER_SHOW_STATE_1(1,"显示"),

    /**
     * 订单退款状态 0 ：未退款 1：退款处理中 2：部分退款 3：全部退款
     */
    REFUND_STATE_0(0,"未退款"),
    REFUND_STATE_1(1,"退款处理中"),
    REFUND_STATE_2(2,"部分退款"),
    REFUND_STATE_3(3,"全部退款"),

    /**
     * H5支付订单状态   0:pending（已提交未支付） ， 1：confirmed（订单确认） ，2：expired（关单/订单过期） 3:decline(客户拒绝)
     */
    H5_ORDER_TYPE_0(0,"pending"),
    H5_ORDER_TYPE_1(1,"confirmed"),
    H5_ORDER_TYPE_2(2,"expired"),
    H5_ORDER_TYPE_3(3,"decline"),

    /**
     * 商户审核类型
     * 0：开户1：商户入网 2：整体出售
     *
     * */
    MERCHANT_APPLICATION_TYPE_0(0,"开户"),
    MERCHANT_APPLICATION_TYPE_1(1,"商户入网"),
    MERCHANT_APPLICATION_TYPE_2(2,"整体出售"),

    /**
     * illion 是否要求二次验证标识符
     */
    REQUIRES_MFA_YES(1,"要求二次验证"),
    /**
     * 分期付最小实付金额配置
     * */
    INSTALLMENT_MIN_AMT(0, "4.4"),
    /**
     * 分期付最小实付金额
     * */
    CREDIT_MIN_TRANSAMOUNT(10, "10"),

    /**
     * 码类型
     * */
    CODE_TYPE_NFC(0, "NFC码"),
    CODE_TYPE_QR(1, "QR码"),

    /**
     * 操作类型
     * */
    BIND_TYPE_BIND(0,"绑定"),
    BIND_TYPE_UNBIND(1,"解绑"),
    /**
     * 信用卡卡bin长度
     */
    CARD_BIN_LENGTH(6,"信用卡卡bin长度"),
    /**
     * cvc最大位数
     */
    CVC_MAX(4,"cvc最大位数"),
    /**
     * 日期格式
     */
    MONTH_FORMAT_MM(2,"mm格式月份,2位"),
    YEAR_FORMAT_YYYY(4,"yyyy格式年份,4位"),

    APP_TYE(0,"APP_TYE"),
    /**
     * verified 账户在当前支付渠道的验证状态
     */
    VERIFIED_0(0, "未验证"),
    VERIFIED_1(1, "验证通过"),
    VERIFIED_2(2, "验证失败"),
    /**
     * 用户类型
     */
    USER_TYPE_10(10, "用户"),

    USER_TYPE_20(20, "商户"),

    USER_TYPE_30(30, "api商户"),

    /**
     * 商户用户角色
     */
    MERCHANT_ROLE_TYPE_0(0, "商户员工"),

    MERCHANT_ROLE_TYPE_1(1, "商户店长"),

    /**
     * 商户员工信息重置
     */
    RESET_PASSWORD(0, "重置密码"),

    RESET_PHONE(1, "修改手机号"),

    /**
     * 商户可用、禁用
     */
    AVAILABLE_0(0, "不可用"),

    AVAILABLE_1(1, "可用"),

    /**
     * 手机号区号
     */
    CHN(0, "86"),

    AU(1, "61"),

    /**
     * 权限类型
     */
    ACTION_TYPE_1(1, "APP权限"),

    /**
     * 子账户类型
     */
    SUB_ACCOUNT_TYPE_0(0, "客户钱包余额子户，商户清算余额子户"),

    SUB_ACCOUNT_TYPE_1(1, "商户整体出售余额子户"),

    /**
     * 二维码状态
     */
    QRCODE_STATE_0(0, "未关联"),

    QRCODE_STATE_1(1, "已关联"),

    /**
     * NFC状态
     * */
    NFCCODE_STATE_0(0, "未关联"),

    NFCCODE_STATE_1(1, "已关联"),

    /**
     * 开户渠道
     */
    ACCOUNT_CHANNEL_0001(0001, "支付"),

    SYSTEM_ACCESS_MANAGE_STATUS(3213, "是否删除"),

    /**
     * 支付交易支付方式
     */

    PAY_TYPE_0(0, "卡支付"),

    PAY_TYPE_1(1, "余额支付"),

    PAY_TYPE_2(2, "支付宝支付"),

    PAY_TYPE_3(3, "微信支付"),

    PAY_TYPE_4(4, "分期付支付"),

    /**
     * 通道支付类型
     */

    CHANNEL_PAY_TYPE_0(0, "卡支付"),

    CHANNEL_PAY_TYPE_1(1, "余额支付"),

    CHANNEL_PAY_TYPE_2(2, "支付宝支付"),

    CHANNEL_PAY_TYPE_3(3, "微信支付"),

    CHANNEL_PAY_TYPE_4(4, "DD支付"),

    /**
     * 主户与子户通用
     */
    ACCOUNT_STATE_1(1, "正常"),

    ACCOUNT_STATE_2(2, "冻结"),

    ACCOUNT_STATE_3(3, "销户"),

    /**
     * 账户系统交易渠道
     */
    ACCOUNT_CHANNEL_0(0, "pay"),

    /**
     * LATPAY 卡交易状态
     */
    LATPAY_CARD_TRANS_STATUS_SUCCESS(1,"Accepted"),
    LATPAY_CARD_TRANS_STATUS_FAILED(2,"Rejected"),

    /**
     * stripe 卡交易状态
     */
    STRIPE_CARD_TRANS_STATUS_SUCCESS(1,"Paid"),
    STRIPE_CARD_TRANS_STATUS_FAILED(2,"Failed"),
    STRIPE_CARD_TRANS_STATUS_3DS(3,"requires_action"),
    STRIPE_CARD_TRANS_STATUS_OTHERS(4,"Others"),

    /**
     * RefundFlowDTO,WithholdFlowDTO 表 state字段 本地交易状态 0:可疑
     */
    LOCAL_TRANS_STATE_0(0,"可疑"),

    /**
     * 商户审核状态
     */
    MERCHANT_STATE_(-1, "审核拒绝"),

    MERCHANT_STATE_0(0, "待审核"),

    MERCHANT_STATE_1(1, "合同完成"),

    MERCHANT_STATE_2(2, "入网审核中"),

    MERCHANT_STATE_3(3, "变更审核中"),

    MERCHANT_STATE_4(4, "变更审核拒绝"),

    MERCHANT_STATE_5(5, "补齐资料"),

    MERCHANT_STATE_6(6, "补齐资料提交审核"),

    MERCHANT_STATE_7(7, "待签署合同"),

    MERCHANT_STATE_8(8, "合同签署中"),

    /**
     * 商户变更审核状态
     */
    ACCOUNT_APPLY_STATE_1(1, "无提交或者审核成功"),

    ACCOUNT_APPLY_STATE_3(3, "变更审核中"),

    ACCOUNT_APPLY_STATE_4(4, "变更审核拒绝"),

    /**
     * 商户变更审核状态
     */
    APPROVE_STATE_(-1, "审核拒绝"),

    APPROVE_STATE_0(0, "待审核"),

    APPROVE_STATE_1(1, "审核通过"),

    APPROVE_STATE_2(2, "审核中"),


    /**
     * 是否可用
     */
    MERCHANT_AVAILABLE_0(0, "不可用"),

    MERCHANT_AVAILABLE_1(1, "可用"),

    /**
     * 是否推荐
     */
    MERCHANT_IS_TOP_0(0, "否"),

    MERCHANT_IS_TOP_1(1, "是"),

    /**
     * 审核类型
     */
    APPROVE_LOG_APPROVE_TYPE_0(0, "商户入网审核"),

    APPROVE_LOG_APPROVE_TYPE_1(1, "商户信息修改审核"),

    /**
     * 出账、出账回滚
     */
    AMOUNT_OUT(0, "出账"),

    AMOUNT_OUT_ROLL_BACK(1, "出账回滚"),

    /**
     * 通用交易状态
     */
    TRANS_STATE_0(0, "交易处理中"),

    TRANS_STATE_1(1, "交易成功"),

    TRANS_STATE_2(2, "交易失败"),

    TRANS_STATE_3(3, "交易可疑"),

    TRANS_STATE_4(4, "交易回滚处理中"),

    TRANS_STATE_5(5, "交易回滚失败"),

    TRANS_STATE_6(6, "latpay交易撤销失败"),

    TRANS_STATE_7(7, "API订单未开始"),

    /**
     * 充值/消费交易状态
     */
    TRANS_STATE_10(10, "出账交易处理中"),

    TRANS_STATE_11(11, "出账交易成功"),

    TRANS_STATE_12(12, "出账交易失败"),

    TRANS_STATE_13(13, "出账交易可疑"),

    TRANS_STATE_20(20, "通道交易处理中"),

    TRANS_STATE_21(21, "通道交易成功"),

    TRANS_STATE_22(22, "通道交易失败"),

    TRANS_STATE_23(23, "通道交易可疑"),

    TRANS_STATE_30(30, "入账处理处理中"),

    TRANS_STATE_31(31, "交易成功"),

    TRANS_STATE_32(32, "入账处理失败"),

    TRANS_STATE_33(33, "入账处理可疑"),

    TRANS_STATE_40(40, "退款失败，账户回滚中"),

    TRANS_STATE_41(41, "退款成功"),

    TRANS_STATE_42(42, "退款失败，回滚失败"),

    TRANS_STATE_43(43, "退款失败，回滚处理中、处理可疑"),

    TRANS_STATE_44(44, "交易跳转到三方等待结果"),

    TRANS_STATE_50(50, "生成分期付订单可疑"),

    TRANS_STATE_104(104, "Refund"),

    TRANS_STATE_105(105, "Cancelled"),

    /**
     * 账户交易交易类型
     */
    ACC_FLOW_TRANS_TYPE_0(0, "充值"),

    ACC_FLOW_TRANS_TYPE_1(1, "余额支付"),

    ACC_FLOW_TRANS_TYPE_2(2, "卡支付"),

    ACC_FLOW_TRANS_TYPE_3(3, "余额转账(互转)"),

    ACC_FLOW_TRANS_TYPE_4(4, "卡转账"),

    ACC_FLOW_TRANS_TYPE_5(5, "商户清算出账"),

    ACC_FLOW_TRANS_TYPE_6(6, "出账回滚"),

    ACC_FLOW_TRANS_TYPE_7(7, "系统间调用"),

    ACC_FLOW_TRANS_TYPE_8(8, "支付宝支付"),

    ACC_FLOW_TRANS_TYPE_9(9, "微信支付"),

    ACC_FLOW_TRANS_TYPE_10(10, "支付宝转账"),

    ACC_FLOW_TRANS_TYPE_11(11, "微信转账"),

    ACC_FLOW_TRANS_TYPE_12(12, "卡退款"),

    ACC_FLOW_TRANS_TYPE_13(13, "支付宝退款"),

    ACC_FLOW_TRANS_TYPE_14(14, "微信退款"),

    ACC_FLOW_TRANS_TYPE_15(15, "卡退款出账回滚"),

    ACC_FLOW_TRANS_TYPE_16(16, "支付宝退款出账回滚"),

    ACC_FLOW_TRANS_TYPE_17(17, "微信退款出账回滚"),

    ACC_FLOW_TRANS_TYPE_18(18, "卡交易取消"),

    ACC_FLOW_TRANS_TYPE_19(19, "注册红包入账"),

    ACC_FLOW_TRANS_TYPE_20(20, "消费红包入账"),

    ACC_FLOW_TRANS_TYPE_21(21, "红包消费"),

    ACC_FLOW_TRANS_TYPE_22(22, "分期付订单"),

    ACC_FLOW_TRANS_TYPE_23(23, "整体出售订单入账"),

    ACC_FLOW_TRANS_TYPE_24(24, "整体出售订单"),

    ACC_FLOW_TRANS_TYPE_25(25, "营销红包入账"),

    ACC_FLOW_TRANS_TYPE_26(26, "整体出售订单账户出账"),

    ACC_FLOW_TRANS_TYPE_27(27, "红包消费回滚"),

    ACC_FLOW_TRANS_TYPE_28(28, "整体出售订单账户出账回滚"),

    ACC_FLOW_TRANS_TYPE_29(29, "账户批量出账"),

    ACC_FLOW_TRANS_TYPE_30(30, "账户批量出账回滚"),

    ACC_FLOW_TRANS_TYPE_31(31, "余额入账"),

    ACC_FLOW_TRANS_TYPE_32(32, "系统间调用卡支付"),
    /**
     * 订单列表 使用该状态
     * */
    ACC_FLOW_TRANS_TYPE_33(33, "逾期费"),

    /**
     * 捐赠订单
     * */
    ACC_FLOW_TRANS_TYPE_34(34, "捐赠订单"),
    /**
     * 小费订单
     * */
    ACC_FLOW_TRANS_TYPE_35(35, "小费订单"),
    ACC_FLOW_TRANS_TYPE_36(36, "分期付退款"),


    /**
     * 订单来源 3 pos订单
     */
    ORDER_SOURCE_0(0, "APP"),

    ORDER_SOURCE_1(1, "API"),

    ORDER_SOURCE_POS(3, "POS"),

    /**
     * 销售类型 0：正常销售 1：整体销售 2：混合销售
     */
    SALE_TYPE_0(0, "正常销售"),

    SALE_TYPE_1(1, "整体销售"),

    SALE_TYPE_2(2, "混合销售"),

    /**
     * 是否需要支付系统清算
     */

    NEED_CLEAR_TYPE_0(0, "不需要"),

    NEED_CLEAR_TYPE_1(1, "需要"),

    /**
     * 扫码支付交易流水表 清算状态
     */
    CLEAR_STATE_TYPE_0(0,"未清算"),

    CLEAR_STATE_TYPE_1(1,"已清算"),

    CLEAR_STATE_TYPE_2(2,"清算处理中"),

    CLEAR_STATE_TYPE_3(3,"延迟清算"),

    /**
     * 清算记录状态
     */
    CLEAR_BATCH_STATE_0(0, "处理中"),

    CLEAR_BATCH_STATE_1(1, "处理成功"),

    CLEAR_BATCH_STATE_2(2, "失败"),

    /**
     * 补款状态
     */
    MAKE_UP_STATE_0(0, "未补款"),

    MAKE_UP_STATE_2(2, "补款中"),

    MAKE_UP_STATE_1(1, "已补款"),


    /**
     * 充值转账路由
     */
    RECHARGE_ROUTE_TYPE_0(0, "代扣"),

    /**
     * 卡解绑状态
     */
    CARD_UNBUNDLING_STATE_0(0, "解绑中"),

    CARD_UNBUNDLING_STATE_1(1, "理财解绑失败"),

    CARD_UNBUNDLING_STATE_2(2, "分期付解绑失败"),

    CARD_UNBUNDLING_STATE_3(3, "账户系统解绑失败"),

    CARD_UNBUNDLING_STATE_4(4, "账户系统解绑成功"),

    CARD_UNBUNDLING_STATE_5(5, "理财、分期付解绑异常"),

    CARD_UNBUNDLING_STATE_6(6, "三方解绑失败"),
    /**
     * 卡信息更新状态
     */

    CARD_UPDATE_STATE_PROCESSING(10, "更新中"),

    CARD_UPDATE_STATE_FAIL(11, "更新失败"),
    CARD_UPDATE_STATE_SUCCESS(12, "更新成功"),
    //CARD_UPDATE_STATE_NO_UPDATE(13, "无操作"),

    /**
     * LatPay卡解绑返回类型
     */
    CARD_UNBUNDLING_STATUS(0, "0"),
    CARD_LATPAY_NOT_FOUND(9010,"SCSS unique identifier incorrect or not found.卡token不存在"),
    /**
     * LatPay卡信息修改接口成功返回结果
     */
    CARD_UPDATE_CARD_INFO_SUCCESS_STATUS(0, "0"),

    /**
     * LatPay卡解绑响应乐星
     */
    CARD_MANAGE_CARD_RESPONSE(7,"LatPay卡解绑响应乐星"),
    /**
     * LatPay卡查询卡类型详情响应码
     */
    CARD_TYPE_INFO_RESPONSE_TYPE(5,"LatPay卡查询卡类型详情响应码"),
    /**
     *  LatPay卡查询卡类型 成功码
     */
    CARD_TYPE_INFO_SUCCESS_STATUS(0,"LatPay卡查询卡类型 成功码"),


    /**
     * LatPay交易响应类型
     */

    LP_RESPONSE_TYPE_0(0, "0"),//"欺诈筛选响应"

    LP_RESPONSE_TYPE_1(1, "银行的响应"),

    LP_RESPONSE_TYPE_4008(4008, "取消订单，直接进行全额退款"),

    /**
     * LatPay银行交易结果相应, 00 成功，05 拒绝，90 通讯异常
     */
    LAT_PAY_BANK_STATUS_0(0, "00"),

    LAT_PAY_BANK_STATUS_5(5, "05"),

    LAT_PAY_BANK_STATUS_7(7, "07"),

    LAT_PAY_BANK_STATUS_9(9, "90"),

    /**
     * LatPay退款请求结果相应, 0 成功 4003 无交易记录
     */
    LAT_PAY_REFUND_REQUEST_STATUS_0(0, "0"),
    LAT_PAY_REFUND_REQUEST_STATUS_4003(4003, "4003"),

    /**
     * 通知响应
     */
    LAT_PAY_DD_RESPONSE_TYPE(4, "Notification Response"),

    LAT_PAY_DD_STATUS_CHECK_TYPE(6, "Status Check"),

    LAT_PAY_DD_RESPONSE_RETURN_TYPE(11, "Return Response"),

    /**
     * latpay DD操作
     */
    LAT_PAY_DD_0(0, "Approved"),

    LAT_PAY_DD_1(1, "Rejected"),

    LAT_PAY_DD_2(2, "Pending"),

    /**
     * latpay DD状态查询可以被认定交易失败错误码
     */
    LAT_PAY_DD_CHECK_9001(2, "9001"),

    LAT_PAY_DD_CHECK_6003(3, "6003"),

    LAT_PAY_DD_CHECK_5011(4, "5011"),

    LAT_PAY_DD_CHECK_5012(5, "5012"),

    LAT_PAY_DD_CHECK_5013(6, "5013"),

    LAT_PAY_DD_CHECK_5021(7, "5021"),

    /**
     * latpay退款类型
     */
    LAT_PAY_REFUND_TYPE_1(1, "Standard Refund"),

    LAT_PAY_REFUND_TYPE_3(3, "Standard Cancellation"),

    /**
     * integraPay 交易状态
     */
    INTEGRAPAY_STATUS_S(1, "S"),

    INTEGRAPAY_STATUS_F(2, "F"),

    INTEGRAPAY_STATUS_R(2, "R"),

    INTEGRAPAY_STATUS_N(3, "N"),

    /**
     * OmiPay查询结果
     */
    OMI_PAY_SUCCESS(200, "SUCCESS"),

    OMI_PAY_FAIL(500, "FAIL"),

    /**
     * OmiPay订单状态
     */

    OMI_PAY_PAID(1, "PAID"),

    OMI_PAY_CLOSED(1, "CLOSED"),

    OMI_PAY_FAILED(2, "FAILED"),

    OMI_PAY_CANCELLED(2, "CANCELLED"),

    OMI_PAY_READY(3, "READY"),

    OMI_PAY_PAYING(3, "PAYING"),

    /**
     * OmiPay退款单状态
     */
    OMI_REFUND_PAYMENT_CHANNEL_CONFIRMED(1, "PaymentChannelConfirmed"),

    OMI_REFUND_ORGANIZATION_PAYBACK(1, "OrganizationPayback"),

    OMI_REFUND_CLOSED(1, "Closed"),

    OMI_MERCHANT_REJECTED(2, "MerchantRejected"),

    OMI_TIME_OUT_CLOSED(2, "TimeoutClosed"),

    OMI_ORGANIZATION_FAILED(2, "OrganizationFailed"),

    OMI_CUSTOMER_CANCELLED(2, "CustomerCancelled"),

    OMI_REFUND_APPLIED(3,  "Applied"),

    OMI_REFUND_MERCHANT_CONFIRMED(3, "MerchantConfirmed"),

    /**
     * 对账交易通道类型
     */
    ALI_PAY(1, "ALIPAY-ONLINE"),

    WECHAT_PAY(2, "WECHATPAY"),

    /**
     * 对账交易类型
     */
    PAY(0, "支付"),

    REFUND(1, "退款"),
    REPAY(2, "还款"),

    /**
     * 对账退款交易方式
     */
    RECONCILIATION_OMI_PAY_REFUND(0, "omipay退款交易"),

    RECONCILIATION_INTEGRA_PAY_REFUND(1, "integraPay退款交易"),


    /**
     * 是否对账
     */
    IS_CHECK_0(0, "未对账"),

    IS_CHECK_1(1, "已对账"),

    IS_CHECK_2(2, "无需对账"),

    IS_CHECK_3(3, "系统对账可疑"),

    /**
     * 对账状态
     */
    CHECK_PROCESSING(0, "处理中"),

    CHECK_SUCCESS(1, "成功"),

    CHECK_FAIL(2, "失败"),

    CHECK_DEAL(3, "处理可疑"),

    /**
     * 手续费收取方向 0：收款方 ，1：付款方
     */

    FEE_DIRECTION_0(0, "收款方"),

    FEE_DIRECTION_1(1, "付款方"),

    /**
     * 通道手续费类型 0：固定费率 ，1：百分比
     */

    CHANNEL_RATE_TYPE_0(0, "固定费率"),

    CHANNEL_RATE_TYPE_1(1, "百分比"),

    /**
     * 出入账方向 0：入账 ，1：出账
     */

    DIRECTION_0(0, "入账"),

    DIRECTION_1(1, "出账"),

    /**
     * 清算状态改变标识
     */
    CHANGE_CLEAR_STATE_TYPE_0(0, "未清算转延迟"),

    CHANGE_CLEAR_STATE_TYPE_1(1, "延迟转未清算"),


    /**
     * 渠道类型
     */
    ROUTE_GATEWAY_TYPE_0(0, "代扣"),

    /**
     * 三方渠道，与channel表id相对应
     */
    GATEWAY_TYPE_0(0, "LatPay卡支付"),

    GATEWAY_TYPE_1(1, "OmiPay支付宝"),

    GATEWAY_TYPE_2(2, "OmiPay微信"),

    GATEWAY_TYPE_3(3, "LatPayAccount支付"),

    GATEWAY_TYPE_4(4, "integrapay卡支付"),

    GATEWAY_TYPE_5(5, "integrapayAccount支付"),

    GATEWAY_TYPE_6(6, "splitAccount支付"),

    GATEWAY_TYPE_7(7, "china-unionpay"),

    GATEWAY_TYPE_8(8, "stripe卡支付"),

    /**
     * 绑卡类型 0：绑账户 ，1：绑卡
     */
    TIE_CARD_0(0, "绑账户"),

    TIE_CARD_1(1, "绑卡"),

    /**
     * app banner数量上限
     */
    APP_BANNER_UPPER_LIMIT(5, "banner数量上限"),

    /**
     * top deal 数量上限
     */
    TOP_DEAL_UPPER_LIMIT(4, "topDeal数量上限"),

    /**
     * 货币标准编码
     */
    CURRENCY_TYPE(1, "AUD"),

    /**
     * 系统代码
     */
    SYSTEM_ID_10(10, "PAY"),

    SYSTEM_ID_20(20, "INVEST"),

    SYSTEM_ID_30(30, "CREDIT"),

    SYSTEM_ID_40(40, "ACCOUNT"),

    /**
     * 国籍
     */
    USER_CITIZEN_SHIP_0(0, "澳洲"),

    USER_CITIZEN_SHIP_1(1, "中国"),

    USER_CITIZEN_SHIP_2(2, "其他"),

    /**
     * 证件类型
     */
    ID_TYPE_0(0, "护照"),

    ID_TYPE_1(1, "驾照"),

    /**
     * 风控检查类型
     */
    RISK_CHECK_TYPE_0(0, "支付"),

    RISK_CHECK_TYPE_1(1, "理财"),

    RISK_CHECK_TYPE_2(2, "分期"),

    /**
     * 风控检查结果
     */
    RISK_CHECK_STATE_0(0, "ACCEPT"),

    RISK_CHECK_STATE_1(1, "REJECT"),

    RISK_CHECK_STATE_2(2, "REVIEW"),
    // todo
    RISK_CHECK_STATE_3(3, "DataSource not available"),
    /**
     * user表分期付开通结果
     */
    INSTALLMENT_NOT_ACTIVE(0,"未开通分期付"),

    /**
     * watchList人工审核状态
     */
    RISK_MANUAL_CHECK_0(0, "待审核"),

    RISK_MANUAL_CHECK_1(1, "审核通过"),

    RISK_MANUAL_CHECK_2(2, "审核拒绝"),

    /**
     * 登陆错误次数
     */
    LOGIN_MISS_TIME(5, "登陆错误次数"),

    LOGIN_MISS_TIME_LEFT(1, "登陆次数为1"),

    /**
     * 支付业务状态,分期付业务状态,理财业务状态
     */
    USER_BUSINESS_0(0, "不可用"),

    USER_BUSINESS_1(1, "可用"),

    USER_BUSINESS_2(2, "禁用"),

    USER_BUSINESS_3(3, "线下审核中"),

    USER_BUSINESS_4(4, "人工审核拒绝"),

    USER_BUSINESS_5(5, "人工审核通过"),

    /**
     * 二维码创建上限
     */
    QRCODE_CREATE_LIMIT(100, "二维码创建上限"),

    /**
     * LATPAY 绑卡返回码
     */
    LAT_PAY_SUCCESS_CODE_0(0, "成功"),

    LAT_PAY_SUCCESS_CODE_9011(9011, "成功"),

    /**
     * 发送节点
     */
    SEND_NODE_0(0, "商户注册发送验证码"),

    SEND_NODE_1(1, "用户注册发送验证码"),

    SEND_NODE_2(2, "用户注册成功"),

    SEND_NODE_3(3, "用户、商户找回密码验证码"),

    SEND_NODE_4(4, "KYC验证成功"),

    SEND_NODE_5(5, "KYC验证失败"),

    SEND_NODE_7(7, "商户用户册成功"),

    SEND_NODE_9(9, "商户入网申请成功"),

    SEND_NODE_10(10, "商户入网申请失败"),

    SEND_NODE_11(11, "商户银行卡信息变更成功"),

    SEND_NODE_12(12, "商户银行卡信息变更失败"),

    SEND_NODE_13(13, "商户订单支付成功"),

    SEND_NODE_14(14, "商户退款成功"),

    SEND_NODE_15(15, "用户修改手机号旧手机"),

    SEND_NODE_16(16, "用户修改手机号新手机"),

    SEND_NODE_18(18, "发送清算文件"),

    SEND_NODE_19(19, "合同签署通知"),

    SEND_NODE_20(20, "资料补齐"),

    SEND_NODE_21(21, "整体出售意向提交"),

    SEND_NODE_22(22, "整体出售审核通过"),

    SEND_NODE_23(23, "整体出售意向审核拒绝"),

    SEND_NODE_24(24, "整体出售审核拒绝"),

    SEND_NODE_25(25, "新订单"),

    SEND_NODE_35(35, "入网审核提交成功"),
    SEND_NODE_36(36, "qld用户注册成功"),
    SEND_NODE_37(37, "非qld用户注册成功"),
    SEND_NODE_39(39, "用户注册成功新"),
    SEND_NODE_40(40, "email新用户day1"),
    SEND_NODE_41(41, "email新用户day3"),
    SEND_NODE_45(45, "email新用户day8"),
    SEND_NODE_47(47, "email新用户有交易day13"),
    SEND_NODE_48(48, "email新用户没有交易day13"),
    SEND_NODE_49(49, "email新用户day20"),
    SEND_NODE_50(50, "email新用户第一次交易2小时后"),
    SEND_NODE_42(42, "sms新用户day4"),
    SEND_NODE_43(43, "push新用户day6"),
    SEND_NODE_44(44, "push新用户day7"),
    SEND_NODE_46(46, "push新用户day10"),


    SEND_NODE_27(27, "KYC验证成功PUSH"),
    SEND_NODE_28(28, "KYC验证成功站内信"),

    SEND_NODE_29(29, "KYC验证失败PUSH"),
    SEND_NODE_30(30, "KYC验证失败站内信"),
    SEND_NODE_31(31, "交易发票信息"),
    SEND_NODE_32(32, "有过交易但一一个月已上未有交易"),
    SEND_NODE_33(33, "新用户2个周后还未消费 邮件"),
    SEND_NODE_34(34, "新用户2个周后还未消费 站内信"),
    SEND_NODE_38(38,"用户修改pin码"),

//    SEND_NODE_17(17, "商户退款失败"),

    /**
     * 所有表通用
     */
    STATUS_0(0, "删除"),

    STATUS_1(1, "使用"),

    /**
     * 渠道绑卡状态
     */
    CHANNEL_BIND_STATUS_0(0, "未绑定"),

    CHANNEL_BIND_STATUS_1(1, "成功"),

    CHANNEL_BIND_STATUS_2(2, "失败"),
    /**
     * google map area level
     */
    ADMINISTRATIVE_AREA_LEVEL_1(1, "administrative_area_level_1"),

    LOCALITY(2, "locality"),

    /**
     * 消息开头
     */
    NOTICE_PREFIX(0, "Notice:"),

    /**
     * 群发消息类型
     */
    NOTICE_TYPE_0(0, "默认"),

    NOTICE_TYPE_1(1, "商户"),

    NOTICE_TYPE_2(2, "理财"),

    NOTICE_TYPE_3(3, "H5"),

    /**
     * 群发消息方式
     */
    NOTICE_SEND_MODE_0(0, "站内信"),

    NOTICE_SEND_MODE_1(1, "短信"),

    NOTICE_SEND_MODE_2(2, "邮箱"),

    NOTICE_SEND_MODE_3(3, "PUSH"),

    /**
     * 商户类型
     */
    MERCHANT_ENTITY_1(1, "Company"),

    MERCHANT_ENTITY_2(2, "Sole Trader"),

    MERCHANT_ENTITY_3(3, "Partnership"),

    MERCHANT_ENTITY_4(4, "Trust-individuals Trustee"),

    MERCHANT_ENTITY_5(5, "Trust-CorporateTrustee"),

    MERCHANT_ENTITY_6(6, "Other"),

    /**
     * integraPay对账文件交易类型
     */
    INTEGRA_PAY_RECONCILIATION_STATUS_0(0, "Realtime Payment - Website"),

    /**
     * latpay对账文件状态
     */
    LAT_PAY_RECONCILIATION_STATUS_0(0, "Rejected"),

    LAT_PAY_RECONCILIATION_STATUS_1(1, "Accepted"),

    /**
     * 用户认证节点
     */
    USER_STEP_1(1, "KYC"),

    USER_STEP_2(2, "Illion"),

    USER_STEP_3(3, "Installment Risk"),

    /**
     * 认证节点状态
     */
    USER_STEP_STATE_0(0, "未开始"),

    USER_STEP_STATE_1(1, "成功"),

    USER_STEP_STATE_2(2, "失败"),

    USER_STEP_STATE_3(3, "进行中"),

    USER_STEP_STATE_4(4, "分期付风控落地审核成功，分期付阶段跳回illion认证"),

    USER_STEP_STATE_5(5, "Failed"),

    /**
     * 认证节点记录状态
     */
    USER_STEP_LOG_STATE_11(11, "kyc通过"),

    USER_STEP_LOG_STATE_12(12, "kyc失败"),

    USER_STEP_LOG_STATE_13(13, "kyc审核"),

    USER_STEP_LOG_STATE_14(14, "kyc Filed"),

    USER_STEP_LOG_STATE_21(21, "illion通过"),

    USER_STEP_LOG_STATE_22(22, "illion失败"),

    USER_STEP_LOG_STATE_31(31, "分期付风控通过"),

    USER_STEP_LOG_STATE_32(32, "分期付风控拒绝"),

    USER_STEP_LOG_STATE_33(33, "分期付风控审核"),

    /**
     * docusign签署成功
     */
    DOCUSIGN_COMPLETE(1, "signing_complete"),

    /**
     * docusign合同类型
     */
    DOCUSIGN_CONTRACT_TYPE_0(0, "09bf9295-cde4-4c06-9e7e-a1390f667f5b"),

    DOCUSIGN_CONTRACT_TYPE_1(1, "effe911e-4bbc-45e9-9ed5-133605472d63"),

    DOCUSIGN_CONTRACT_TYPE_X(3, "1d288fd8-9899-401a-bc1d-2437f9ac2f97"),

    DOCUSIGN_CONTRACT_TYPE_Y(4, "160d6669-ece4-4630-8881-5c7f4183f47d"),

    DOCUSIGN_CONTRACT_TYPE_FORMAL(5, "891d75c8-8d10-4a67-af47-25d1c937ae53"),

    /**
     * api订单状态
     */
    API_ORDER_STATE_1(1, "SUCCESS"),

    API_ORDER_STATE_0(0, "FAILED"),

    API_ORDER_STATE_3(3, "SUSPICIOUS"),

    /**
     * 清算类型
     */
    CLEAR_TYPE_0(0, "支付交易资金清算"),

    CLEAR_TYPE_1(1, "api商户服务费"),

    CLEAR_TYPE_2(2, "整体出售清算"),

    CLEAR_TYPE_3(3, "分期付清算同步"),

    CLEAR_TYPE_4(4, "捐赠清算"),

    CLEAR_TYPE_5(5, "小费清算"),

    CLEAR_TYPE_6(6, "H5商户资金清算"),

    /**
     * 分期付清算状态
     */
    CREDIT_CLEAR_STATE_0(10,"未清算"),

    CREDIT_CLEAR_STATE_1(20,"已清算"),



    /**
     * 整体出售状态
     */
    WHOLE_SALE_STATE_0(0, "审核中"),

    WHOLE_SALE_STATE_1(1, "审核成功"),

    WHOLE_SALE_STATE_2(2, "审核失败"),

    WHOLE_SALE_STATE_3(3, "结算完成"),

    WHOLE_SALE_STATE_4(4, "入账处理中"),

    /**
     * 订单类型
     */
    WHOLE_SALE_ORDER_TYPE_0(0, "意向订单"),

    WHOLE_SALE_ORDER_TYPE_1(1, "订单类型"),

    /**
     * 日期类型
     */
    DATE_TYPE_YEAR(0, "年"),

    DATE_TYPE_MONTH(1, "月"),


    /**
     * split支付请求状态
     */
    SPLIT_PAY_REQUEST_APPROVED(0, "approved"),

    /**
     * split订单状态
     */
    SPLIT_ORDER_STATE_CLEARED(0, "cleared"),

    SPLIT_ORDER_STATE_REJECTED(1, "rejected"),

    SPLIT_ORDER_STATE_RETURNED(2, "returned"),

    SPLIT_ORDER_STATE_VOIDED(3, "voided"),

    SPLIT_ORDER_STATE_PREFAILED(4, "prefailed"),

    /**
     * uwallet邮件标签
     */
    U_WALLET(0, "Payo"),

    U_BIZ(1, "Payo-Biz"),

    /**
     * push是否语音播报
     */
    VOICE_0(0, "否"),

    VOICE_1(1, "是"),

    /**
     * push跳转页面
     */
    PUSH_ROUTE_1(1, "还款页面"),

    PUSH_ROUTE_2(2, "主页面"),
    /**
     * 用户卡支付状态, 0:不可用,1:可用
     */
    USER_CARD_STATE_0(0,"不可用"),
    USER_CARD_STATE_1(1,"可用"),
    /**
     * 商户列表-带搜索表示位
     * 1: Discount,2: nearest, 3: New Venues
     */
    ORDER_BY_DISCOUNT(1,"折扣排序"),
    ORDER_BY_DISTANCE(2,"地理位置排序"),
    ORDER_BY_SEARCH_KEYWORD(0,"关键字查找"),
    ORDER_BY_NEW_VENUES(3,"New Venues"),
    /**
     * 是否是触发搜索按钮
     */
    UPDATE_TAG_POPULAR(1,"是否是触发搜索按钮,是, 增加tag的popular字段"),
    /**
     * 2021-01-10 商户表添加字段=>标识位:整体出售额度是否已经消耗
     * 商户整体额度不为0
     */
    MERCHANT_WHOLE_SELL_AMOUNT_NOT_ZERO(1,"商户整体销售额度不为0"),
    MERCHANT_WHOLE_SELL_AMOUNT_ZERO(0,"商户整体销售额度为0"),

    /**
     * KYC异常信息
     * */
    STATUS_SUCCESS(0,"请求成功"),
    STATUS_SYSTEM_ERROR(1,"系统错误"),
    STATUS_GENERAL_ERROR(2,"通用失败"),
    STATUS_ONE_MORE(3,"仅剩一次尝试次数"),
    STATUS_UPPER_LIMIT(4,"错误次数达到上限"),
    STATUS_MANUAL_AUDIT(5,"人工审核"),
    STATUS_REPEAT_USER(6,"重复用户"),



    /**
     * POS订单状态信息
     * */

    /**
     *
     * 订单状态 0:处理中， 1：交易成功 ，2：交易失败 ，3：交易可疑  4 创建订单未处理  默认状态  5 已经扫描过二维码
     *
     */
    POS_ORDER_STATUS_SCANNED(5, "已经扫描过二维码"),
    POS_ORDER_STATUS_CREATE_ORDER(4, "创建订单未处理"),
    POS_ORDER_STATUS_SUSPICIOUS(3, "交易可疑"),
    POS_ORDER_STATUS_FAIL(2, "交易失败"),
    POS_ORDER_STATUS_SUCCESS(1, "交易成功"),
    POS_ORDER_STATUS_INPROCESS(0, "处理中"),


    /**
     *报告运营审查
     */
    AUDIT_USER_STATE_WAITING_FOR_MANUAL_REVIEW(30, "待人工复审"),
    AUDIT_USER_STATE_WAITING_FOR_REVIEW(40, "待落地复核"),

    /**
     * kyc是否请求三方成功
     * 0未请求三方 1请求成功 2请求失败
     *
     * */
    KYC_SUBMIT_STATUS0(0,"未请求"),
    KYC_SUBMIT_STATUS1(1,"请求成功"),
    KYC_SUBMIT_STATUS2(2,"请求失败"),

    /**
     * 是否调用watchlist
     * 1 是，2不是
     * */
    WATCHLIST_STATUS_1(1,"是"),
    WATCHLIST_STATUS_2(2,"不是"),

    /**
     * illion状态 0加密失败,1可用,2未获取到,3获取成功
     * */
    ILLION_STATUS_0(0,"Encryption failed"),
    ILLION_STATUS_1(1,"Available"),
    ILLION_STATUS_2(2,"Not obtained"),
    ILLION_STATUS_3(3,"Obtained"),

    /**
     * illion上送状态 0失败,1成功
     * */
    ILLION_SUBMIT_STATUS_0(0,"失败"),
    ILLION_SUBMIT_STATUS_1(1,"成功"),


    /**
     * 三方kyc结果 0完全匹配 1 部分匹配 2 拒绝 3三方数据源异常 4系统异常 5 kyc异常
     */
    KYC_CHECK_STATE_0(0, "Full match"),

    KYC_CHECK_STATE_1(1, "Partial match"),

    KYC_CHECK_STATE_2(2, "Full incorrect"),

    KYC_CHECK_STATE_3(3, "Source Failure"),

    KYC_CHECK_STATE_4(4, "System error"),

    KYC_CHECK_STATE_5(5, "kyc System error"),

    /**
     * 发送类型: 1 push 2 短信
     * */
    SEND_TYPE_1(1,"push"),
    SEND_TYPE_2(2,"短信"),

    /**
     * 后台发送的消息类型
     * */
    SEND_TYPE_PUSH(2,"push"),
    SEND_TYPE_APP_MESSAGE(1,"app消息"),
    SEND_TYPE_ALL(-1,"全部"),

    /**
     * 批量发送短信redis key
     * */

    MESSAGE_PREFIX(0,"Message:"),
    /**
     * 批量发送状态 1 未发送 2 发送成功 3 不可用
     * */
    SEND_MESSAGE_STATE_1(1,"未发送"),
    SEND_MESSAGE_STATE_2(2,"发送成功"),
    SEND_MESSAGE_STATE_3(3,"不可用"),

    /**
     * 消息用户类型
     * */
    MESSAGE_ACCOUNT_STATUS_1(1,"未绑卡也未开通分期付"),
    MESSAGE_ACCOUNT_STATUS_2(2,"未申请过分期付"),
    MESSAGE_ACCOUNT_STATUS_3(3,"未添加过银行卡"),
    MESSAGE_ACCOUNT_STATUS_4(4,"分期付开通拒绝 "),
    MESSAGE_ACCOUNT_STATUS_5(5,"处于KYC失败状态的用户"),
    MESSAGE_ACCOUNT_STATUS_6(6,"用户未获取到illion报告"),
    MESSAGE_ACCOUNT_STATUS_7(7,"有红包的用户"),
    MESSAGE_ACCOUNT_STATUS_8(8,"未有任何交易的用户"),
    MESSAGE_ACCOUNT_STATUS_9(9,"消费过但已一个月以上未有新交易"),
    MESSAGE_ACCOUNT_STATUS_10(10,"分期付已逾期一周以上的用户"),
    MESSAGE_ACCOUNT_STATUS_11(11,"已产生逾期费的用户"),

    /**
     * 是否是省会城市
     * */
    ACTIVITY_CITY_STATUS_1(1,"是"),
    ACTIVITY_CITY_STATUS_2(2,"否"),



    /**
     * 卡支付是否绑卡 1 绑定 2 未绑定
     * */
    USER_CARD_STATE_BINDED(1,"已经绑卡"),
    USER_CARD_STATE_NOT_BINDED(0,"未绑卡"),


    /**
     * 邀请记录表状态 1已读 2未读
     * */
    MARKETING_IS_SHOW_1(1,"已读"),
    MARKETING_IS_SHOW_2(2,"未读"),


    /**
     * 分期付用户是否绑卡
     * 0未绑卡 1已绑卡
     * */
    CREAT_BIND_CARD_0(0,"未绑卡"),
    CREAT_BIND_CARD_1(1,"已绑卡"),

    /**
     * 分期付用户状态
     *
     * */
    CREAT_USER_STATE_11(11,"冻结"),
    CREAT_USER_STATE_20(20,"正常"),

    /**
     * 用户illion链接状态
     * */
    ILLION_SUBMIT_LOG_STATUS_0(0,"未输入信息"),
    ILLION_SUBMIT_LOG_STATUS_1(1,"输入信息正确"),
    ILLION_SUBMIT_LOG_STATUS_2(2,"银行链接错误"),
    ILLION_SUBMIT_LOG_STATUS_3(3,"银行链接成功"),
    ILLION_SUBMIT_LOG_STATUS_4(4,"未返回报告"),
    ILLION_SUBMIT_LOG_STATUS_5(5,"加密报告成功"),
    ILLION_SUBMIT_LOG_STATUS_6(6,"加密报告失败"),
    ILLION_SUBMIT_LOG_STATUS_7(7,"发送分期付申请额度"),
    ILLION_SUBMIT_LOG_STATUS_8(8,"额度开通成功"),
    ILLION_SUBMIT_LOG_STATUS_9(9,"风控拒绝"),
    ILLION_SUBMIT_LOG_STATUS_10(10,"额度开通失败"),
    ILLION_SUBMIT_LOG_STATUS_11(11,"开通分期付成功"),


    /**
     * 创建分期付订单流水状态
     *  0 处理中 1 成功 2 失败 3 可疑
     * */
    CREATE_CREDIT_ORDER_FLOW_STATE_0(0,"处理中"),
    CREATE_CREDIT_ORDER_FLOW_STATE_1(1,"成功"),
    CREATE_CREDIT_ORDER_FLOW_STATE_2(2,"失败"),
    CREATE_CREDIT_ORDER_FLOW_STATE_3(3,"可疑"),


    /**
     * 支付额度变更流水操作类型
     *  1 冻结 2 解冻
     * */
    PAY_BALANCE_FLOW_OPERATE_TYPE_1(1,"冻结"),
    PAY_BALANCE_FLOW_OPERATE_TYPE_2(2,"解冻"),

    /**
     * 支付额度变更流水状态
     * 0 处理中 1 成功 2 失败 3 解冻可疑
     * */
    PAY_BALANCE_FLOW_STATE_0(0,"处理中"),
    PAY_BALANCE_FLOW_STATE_1(1,"成功"),
    PAY_BALANCE_FLOW_STATE_2(2,"失败"),
    PAY_BALANCE_FLOW_STATE_3(3,"解冻可疑"),

    /**
     * 捐赠结算状态
     * 0：未结算 1：已结算 2：结算中 3：延迟结算
     * */
    DONATION_CLEAR_STATUS_0(0,"Unsettled"),
    DONATION_CLEAR_STATUS_1(1,"Settled"),
    DONATION_CLEAR_STATUS_2(2,"In Settlement"),
    DONATION_CLEAR_STATUS_3(3,"Delay Settle"),
    /**
     * 银行卡类型
     */
    CARD_TYPE_10(10, "VISA"),
    CARD_TYPE_20(20, "MAST"),
    CARD_TYPE_30(30, "SWITCH"),
    CARD_TYPE_40(40, "SOLO"),
    CARD_TYPE_50(50, "DELTA"),
    CARD_TYPE_60(60, "AMEX"),
    /**
     * 新老用户
     * */
    USER_TYPE_OLD(0,"老用户"),
    USER_TYPE_NEW(1,"新用户"),
    /**
     * 注册来源Register
     * */
    USER_REGISTER_APP(0,"app"),
    USER_REGISTER_H5(1,"H5"),
    USER_REGISTER_BACK_SYSTEM(2,"后台"),

    /**
     *
     *  是否有用户数据
     *  1 有
     *  2 无
     * */
    NEED_UPDATE_USER_DATA_STATE(1,"有"),
    NOT_NEED_UPDATE_USER_DATA_STATE(0,"无"),

    /**
     * 冻结用户类型
     * */
    USER_FROZEN_TYPE_0(0,"账户冻结"),
    USER_FROZEN_TYPE_1(1,"分期付冻结"),
    USER_FROZEN_TYPE_2(2,"协助注册"),
    USER_FROZEN_TYPE_3(3,"协助KYC"),
    USER_FROZEN_TYPE_4(4,"延迟还款"),
    USER_FROZEN_TYPE_5(5,"修改用户信息"),
    USER_FROZEN_TYPE_6(6,"提额"),
    USER_FROZEN_TYPE_7(7,"降额"),


    /**
     * 支付绑卡状态
     * */
    PAY_BIND_CARD_STATE_1(1,"有卡不需要绑卡"),
    PAY_BIND_CARD_STATE_0(0,"没有卡 先绑卡在支付"),


    /**
     * 营销活动状态活动状态0 未开始 1 进行中 2 已结束 3 终止
     * */
    MARKETING_ACTIVITY_STATE_0(0,"未开始"),
    MARKETING_ACTIVITY_STATE_1(1,"进行中"),
    MARKETING_ACTIVITY_STATE_2(2,"已结束"),
    MARKETING_ACTIVITY_STATE_3(3,"终止"),

    /**
     * 券码状态
     * */
    USER_MARKETING_COUPON_STATE_USED(2,"已使用"),
    USER_MARKETING_COUPON_STATE_NOT_USED(1,"未使用"),
    USER_MARKETING_COUPON_STATE_NOT_ACTIVATED(99,"未激活"),


    /**
     * 营销码类型 1:营销  2.邀请码
     * */
    MARKETING_TYPE_1(1,"营销"),
    MARKETING_TYPE_2(2,"邀请码"),


    /**
     * 营销码创建方式 0：系统生成 1：自定义
     * */
    MARKETING_CREATE_METHOD_0(0,"系统生成"),
    MARKETING_CREATE_METHOD_2(1,"自定义"),

    /**
     * 是否限制金额 0 不限制 1 限制
     * */
    MARKETING_AMOUNT_LIMIT_STATE_1(1,"限制"),
    MARKETING_AMOUNT_LIMIT_STATE_0(0,"不限制"),


    /**
     * 是否限制时间 0 不限制 1 限制
     * */
    MARKETING_VALIDITY_LIMIT_STATE_1(1,"限制"),
    MARKETING_VALIDITY_LIMIT_STATE_0(0,"不限制"),


    /**
     * app展示券码状态 1可用 2 已使用  3 过期 4 终止
     * */
    MARKETING_APP_SHOW_STATE_1(1,"可用"),
    MARKETING_APP_SHOW_STATE_2(2,"已使用"),
    MARKETING_APP_SHOW_STATE_3(3,"过期"),
    MARKETING_APP_SHOW_STATE_4(4,"终止"),
    MARKETING_APP_SHOW_STATE_5(5,"未激活"),

    /**
     * app支付展示券码状态 1 可用 黄us   2 不可用 灰use
     * */
    MARKETING_APP_PAY_SHOW_STATE_1(1,"可用 黄use"),
    MARKETING_APP_PAY_SHOW_STATE_2(2,"不可用 灰use"),

    /**
     * 添加邀请券 类型 1 给被邀请人加  2 给邀请人发放
     * */
    ADD_INVITATION_CODE_TYPE_1(1, "给被邀请人加"),

    ADD_INVITATION_CODE_TYPE_2(2, "给邀请人发放"),

    /**
     * 注册状态
     *
     * */
    USER_REGISTER_STATUS_REGISTERED(1,"已经注册"),
    USER_REGISTER_STATUS_UNREGISTERED(0,"未注册"),

    /**
     *
     * 1州 2城市 3地址 4etc 5邮编 6手机 7邮箱 8冻结分期付 9冻结账户 10解结账户 解冻分期付 12提额 13降额 14延迟还款
     * */
    //   11解冻分期付
    USER_INFO_UPDATE_1(1,"州"),
    USER_INFO_UPDATE_2(2,"城市"),
    USER_INFO_UPDATE_3(3,"地址"),
    USER_INFO_UPDATE_4(4,"etc"),
    USER_INFO_UPDATE_5(5,"邮编"),
    USER_INFO_UPDATE_6(6,"手机"),
    USER_INFO_UPDATE_7(7,"邮箱"),
    USER_INFO_UPDATE_8(8,"冻结账户"),
    USER_INFO_UPDATE_9(9,"冻结账户"),
    USER_INFO_UPDATE_10(10,"解结账户"),
    USER_INFO_UPDATE_11(11,"解冻分期付"),
    USER_INFO_UPDATE_12(12,"提额"),
    USER_INFO_UPDATE_13(13,"降额"),
    USER_INFO_UPDATE_14(14,"延迟还款"),
    USER_INFO_UPDATE_15(15,"协助注册"),
    USER_INFO_UPDATE_16(16,"协助KYC"),


    /**
     * stripe card brand
     * American Express, Diners Club, Discover, JCB, MasterCard, UnionPay, Visa, or Unknown.
     *
     * */
    STRIPE_CARD_BRAND_VISA(10,"Visa"),
    STRIPE_CARD_BRAND_AMEX(60,"American Express"),
    STRIPE_CARD_BRAND_MAST(20,"MasterCard"),
    STRIPE_CARD_BRAND_DC(201,"Diners Club"),
    STRIPE_CARD_BRAND_JCB(202,"JCB"),
    STRIPE_CARD_BRAND_DISCOVER(203,"Discover"),
    STRIPE_CARD_BRAND_CUP(204,"UnionPay"),
    STRIPE_CARD_BRAND_UNKNOWN(999,"Unknown"),

    /**
     * stripe card funding
     * credit, debit, prepaid, or unknown.
     *
     * */
    STRIPE_CARD_FUNDING_CREDIT(1,"credit"),
    STRIPE_CARD_FUNDING_DEBIT(2,"debit"),
    STRIPE_CARD_FUNDING_PREPAID(3,"prepaid"),
    STRIPE_CARD_FUNDING_UNKNOWN(4,"unknown"),

    /**
     * stripe订单描述类型  pay 、repayment 、refund
     *
     * */
    STRIPE_ORDER_DESC_PAY(1,"pay"),
    STRIPE_ORDER_DESC_REPAYMENT(2,"repayment"),
    STRIPE_ORDER_DESC_REFUND(3,"refund"),

    /**
     * stripe老用户标识 0 ：否 1：是
     *
     * */
    USER_STRIPE_STATE_0(0, "否"),
    USER_STRIPE_STATE_1(1, "是"),

    /**
     * braze发送消息类型
     * */
    BRAZE_MESSAGE_NODE_1(1,"apple_push"),
    BRAZE_MESSAGE_NODE_2(2,"android_push"),
    BRAZE_MESSAGE_NODE_3(3,"email"),
    BRAZE_MESSAGE_NODE_4(4,"sms"),
    /**
     * braze 返回错误类型
     * */
    BREAZE_REAPONSES_SUCCESS(0,"success"),
    BREAZE_REAPONSES_QUEUED(1,"queued"),

    /**
     * 性别
     * */
    GENDER_Female(1,"F"),
    GENDER_Male(2,"M"),
    GENDER_Prefer(3,"P");

    private final int code;

    private final String message;

    StaticDataEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
