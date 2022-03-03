package com.uwallet.pay.main.exception;

import com.uwallet.pay.core.common.PosApiCodeEnum;
import lombok.Data;


@Data
public class PosApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;

    public PosApiException(String message, String code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public PosApiException(PosApiCodeEnum codeEnum) {
        super(codeEnum.getMessage());
        this.message = codeEnum.getMessage();
        this.code = codeEnum.getCode();
    }
}
