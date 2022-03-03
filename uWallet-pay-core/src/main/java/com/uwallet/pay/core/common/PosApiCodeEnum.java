package com.uwallet.pay.core.common;

import lombok.Getter;

/**
 * pos api 状态码
 *
 * @author zhangzeyuan
 * @date 2021/03/19
 */
@Getter
public enum PosApiCodeEnum {

    /**
     * 成功
     */
    OK("300200", "Operate Successfully"),

    /**
     * 失败
     */
    ERROR("300100", "Operate unsuccessfully"),


    /**
     * 验签失败
     */
    VERIFY_SIGN_ERROR("300101", "Verify Sign Error"),

    /**
     * POS商户未注册
     */
    POS_NOT_REGISTERED("300201", "Pos Do Not Has Permission"),


    /**
     *  pos二维码生成异常
     */
    QR_CODE_GENERATE_ERROR("300301", "Qr Code Generate Error"),


    /**
     * 生成订单异常
     */
    ORDER_CREATE_ERROR("300401", "Order Create Error"),


    /**
     * 回调通知异常
     */
    NOTIFY_ERROR("300501", "Pay Success Notify Error"),


    /**
     * 参数不能为空
     */
    PARAM_NOT_NULL("300601", "Param Not Null");


    /**
     * 返回码
     */
    private final String code;

    /**
     * 返回信息
     */
    private final String message;

    /**
     * 错误码枚举
     *
     * @param code
     * @param message
     */
    PosApiCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "code:" + this.code + "| message:" + message;
    }
}
