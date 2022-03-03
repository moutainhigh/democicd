package com.uwallet.pay.main.exception;

/**
 * @author baixinyue
 * @date 2019/11/06
 * @description 用户sdk和外部接口访问验证错误信息返回
 */

public class SignException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String code ;
    private String message;

    public SignException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
