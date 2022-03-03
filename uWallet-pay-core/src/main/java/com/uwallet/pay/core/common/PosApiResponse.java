package com.uwallet.pay.core.common;

import lombok.Data;


/**
 * @author zhangzeyuan
 */
@Data
public class PosApiResponse<T> {
    /**
     * 状态码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;


    public PosApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> PosApiResponse success(T data) {
        return new PosApiResponse<>(PosApiCodeEnum.OK.getCode(), PosApiCodeEnum.OK.getMessage(), data);
    }


    public static <T> PosApiResponse fail(String code, String message) {
        return new PosApiResponse<>(code, message, null);
    }


    public static <T> PosApiResponse fail() {
        return new PosApiResponse<>(PosApiCodeEnum.ERROR.getCode(), PosApiCodeEnum.ERROR.getMessage(), null);
    }

}
