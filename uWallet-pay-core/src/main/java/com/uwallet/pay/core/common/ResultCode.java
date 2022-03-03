package com.uwallet.pay.core.common;

import lombok.Getter;

/**
 *
 * @author faker
 */
@Getter
public enum ResultCode implements IResultCode {
    /**
     * 成功
     */
    OK("100200", ""),
    /**
     * 失败
     */
    ERROR("100400", "Operate unsuccessfully"),

    /**
     * H5三方成功
     */
    H5OK("201", ""),
    /**
     * H5三方成功
     */
    H5OK200("200", ""),
    /**
     * H5三方成功
     */
    H5ERROR("400", "Operate unsuccessfully");

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
