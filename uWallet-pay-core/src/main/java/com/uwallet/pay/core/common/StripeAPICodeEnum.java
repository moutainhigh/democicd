package com.uwallet.pay.core.common;

import lombok.Getter;

/**
 * stripe api 状态码
 *
 * @author zhangzeyuan
 * @date 2022/01/17
 */
@Getter
public enum StripeAPICodeEnum {

    /**
     * 操作成功
     */
    SUCCESS_CODE("100200", "Operate successfully"),

    /**
     * 操作失败
     */
    FAIL_CODE("100400", "Operate unsuccessfully"),


    /**
     * Stripe Status of  PaymentInten
     */
    PAYMENTINTENT_RES_STATUS_REQUIRES_PAYMENT_METHOD("requires_payment_method", "requires_payment_method"),
    PAYMENTINTENT_RES_STATUS_REQUIRES_CONFIRMATION("requires_confirmation", "requires_confirmation"),
    PAYMENTINTENT_RES_STATUS_REQUIRES_ACTION("requires_action", "requires_action"),
    PAYMENTINTENT_RES_STATUS_PROCESSING("processing", "processing"),
    PAYMENTINTENT_RES_STATUS_REQUIRES_CAPTURE("requires_capture", "requires_capture"),
    PAYMENTINTENT_RES_STATUS_CANCELED("canceled", "canceled"),
    PAYMENTINTENT_RES_STATUS_SUCCEEDED("succeeded", "succeeded"),
    PAYMENTINTENT_RES_STATUS_FAILED("failed", "failed"),


    /**
     * The status of the charge
     */
    CHARGE_RES_STATUS_SUCCEEDED("succeeded", "succeeded"),
    CHARGE_RES_STATUS_PENDING("pending", "pending"),
    CHARGE_RES_STATUS_CANCELED("canceled", "canceled"),
    CHARGE_RES_STATUS_FAILED("failed", "failed"),
    /**
     * 返回码
     */

    /**
     * 类型
     * */
    CHARGE_REFUNDED("charge.refunded","退款");

//    pending, succeeded, failed, or canceled
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
    StripeAPICodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "code:" + this.code + "| message:" + message;
    }
}
