package com.uwallet.pay.core.common;

import lombok.Data;

/**
 * stripe 返回结果
 *
 * @author zhangzeyuan
 * @date 2022/1/14 16:42
 */
@Data
public class StripeAPIResponse<T> {

    /**
     * 业务状态码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 状态
     */
    private boolean success;

    /**
     * 返回数据
     */
    private T data;

    public StripeAPIResponse(String code, String message, boolean success, T data) {
        this.code = code;
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public static <T> StripeAPIResponse success(StripeAPICodeEnum codeEnum, T data) {
        return new StripeAPIResponse<>(codeEnum.getCode(), codeEnum.getMessage(), true, data);
    }

    public static <T> StripeAPIResponse fail(String code, String message) {
        return new StripeAPIResponse<>(code, message, false, null);
    }
}
