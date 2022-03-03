package com.uwallet.pay.core.exception;

import lombok.Data;

/**
 * @author faker
 * @since 2019/07/03
 */
@Data
public class LatpayFailedException extends Exception {

    Integer cardCount;

    Integer deleteCardStatus;

    public LatpayFailedException(String message, Integer deleteCardStatus, Integer cardCount) {
        super(message);
        this.cardCount = cardCount;
        this.deleteCardStatus = deleteCardStatus;
    }



}
