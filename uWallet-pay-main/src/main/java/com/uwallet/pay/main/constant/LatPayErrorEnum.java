package com.uwallet.pay.main.constant;

import lombok.Getter;

/**
 * latpay返回信息错误码转换
 * @author zhoutt
 *
 */
@Getter
public enum  LatPayErrorEnum {
    /**
     * 银行返回错误信息对照
     */
    BANK_CODE_05("05", "Transaction Failed. Card Info Error. Please confirm the Card information or change to another card to pay again."),
    BANK_CODE_06("06", "Transaction failed. Please change to another bank card and try again."),
    BANK_CODE_07("07", "Transaction Failed. This card is not available. Please change to another card to pay again."),
    BANK_CODE_41("41", "Transaction Failed. This card is not available. Please change to  another card to pay again."),
    BANK_CODE_43("43", "Transaction Failed. This card is not available. Please change to  another card to pay again."),
    BANK_CODE_51("51", "Transaction Failed. Your card has insufficient funds. Please change to another card to make a payment."),
    BANK_CODE_OTHER("OTHER", "Transaction failed. Please change to another bank card and try again."),


    /**
     * 反欺诈返回错误信息对照
     */
    SCSS_CODE_1003("1003", "Transaction amount exceeds the per Transaction Limit: Transaction is greater than the allowed value for the day."),
    SCSS_CODE_1004("1004", "Customer has exceeded allowed transactions number for 24 hours: Velocity control on purchases per day reached."),
    SCSS_CODE_1005("1005", "Purchase amount check: Repeated amounts being purchased."),
    SCSS_CODE_1006("1006", "Invalid credit card number: Incorrect digit length for card type."),
    SCSS_CODE_OTHER("OTHER", "Transaction failed. Please change to another bank card and try again.")

    ;

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
    LatPayErrorEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

}
