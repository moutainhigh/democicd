package com.uwallet.pay.core.enumeration;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author
 * @date 2019/04/02
 */
@Getter
public enum StatusEnum {
    /**
     * 用户状态正常
     */
    USER_STT_NORMAL("0", "正常"),
    ;

    private final String code;

    private final String message;

    StatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "code:" + this.code + "| message:" + message;
    }
}
