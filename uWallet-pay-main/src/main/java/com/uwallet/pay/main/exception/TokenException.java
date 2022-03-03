package com.uwallet.pay.main.exception;

import lombok.Data;

/**
 * @author faker
 */
@Data
public class TokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String code ;
    private String message;
    
    public TokenException(String message) {
        super(message);
        this.message = message;
    }

    public TokenException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public TokenException(String message, String code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public TokenException(String message, String code, Throwable e) {
        super(message, e);
        this.code = code;
    }

}
