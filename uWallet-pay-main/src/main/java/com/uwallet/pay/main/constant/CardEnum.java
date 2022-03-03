package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 * 卡相关枚举
 *
 * @author aaron
 * @date 2020/11/10
 */

@Getter
public enum CardEnum {
    /**
     * 卡类型
     * 卡类型 1：卡 0：账户
     */
    CARD_TYPE_ACCOUNT(0,"账户"),
    CARD_TYPE_CARD(1,"卡");

    private final Integer code;

    private final String message;

    CardEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
