package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author
 * @date 2019/04/08
 */
@Getter
public enum SignErrorCode {

    /**
     * 验签成功
     */
    SIGN_SUCCESS("100200", "Incorrect Information"), //操作成功

    /**
     * 验签失败
     */
    SIGN_ERROR("1004021", "Encryption validation failed"), //

    /**
     * token失效
     */
    SDK_TOKEN_ERROR("100403", "Log in timeout, please log in again"); //

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
     * @param code
     * @param message
     */
    SignErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "code:" + this.code + "| message:" + message;
    }
}
