package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 * DocuSign枚举
 *
 * @author aaron
 * @date 2020/04/10
 */

@Getter
public enum DocuSignEnum {
    /**
     * authorisedTitle 签署人分类
     */
    AUTHORISED_TITLE_SOLE_TRADE("1","/individual_sole_trader"),
    AUTHORISED_TITLE_DIRECTOR("2","/director"),
    AUTHORISED_TITLE_ATTORNEY("3","/attorney"),
    /**
       信封(Envelop)状态
     */
    ENVELOP_STATUS_SENT("sent", "已经发送,只有被标记为sent才能发送"),
    ENVELOP_STATUS_CREATED("created", "仅创建信封,随时可以发送"),
    /**
     * event 向我们推送信封状态的节点
     */
    ENVELOP_EVENT_DRAFT("Draft","草稿"),
    ENVELOP_EVENT_SENT("Sent","已经发送"),
    ENVELOP_EVENT_DELIVERED("Delivered","送达"),
    ENVELOP_EVENT_COMPLETED("Completed","完成"),
    ENVELOP_EVENT_DECLINED("Declined","拒绝"),
    ENVELOP_EVENT_VOIDED("Voided","无效");

    private final String code;

    private final String message;

    DocuSignEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
