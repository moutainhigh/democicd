package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 *
 * @description: 版本控制枚举类
 * @author: Rainc
 * @date: Created in 2019-07-30 09:03:29
 * @copyright: Copyright (c) 2019
 * @version: V1.0
 * @modified: aaronS
 */
@Getter
public enum VersionEnum {
    /**
     * 人工干预,触发强制更新 ManualForceUpdate
     * 人工触发强制更新: 1:强制更新 0: 非强制更新
     */
    MANUAL_FORCE_UPDATE_NEED(1,"1:强制更新"),
    MANUAL_FORCE_UPDATE_NOT_NEED(0,"0: 非强制更新"),
    /**
     * verified 账户在当前支付渠道的验证状态
     */
    NEED_UPDATE(1, "需要强制升级"),
    DO_NOTHING(2, "无需任何操作"),
    NOT_NEED_UPDATE(0, "不需要强制升级");
    private final int code;

    private final String message;

    VersionEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
