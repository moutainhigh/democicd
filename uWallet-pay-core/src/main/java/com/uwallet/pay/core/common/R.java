package com.uwallet.pay.core.common;

import lombok.Data;

/**
 *
 * @param <T>
 */
@Data
public class R<T> {
    /**
     * 状态码
     */
    private String code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 状态
     */
    private boolean status;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 分页参数
     */
    private PagingContext pc;

    public R(String code, String message, boolean status, T data) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public R(IResultCode resultCode, boolean status, T data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.status = status;
        this.data = data;
    }
    public R(IResultCode resultCode, boolean status, T data,PagingContext pc) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.status = status;
        this.data = data;
        this.pc = pc;
    }

    public R(IResultCode resultCode, boolean status) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.status = status;
        this.data = null;
    }

    public static <T> R success() {
        return new R<>(ResultCode.OK, true);
    }

    public static <T> R message(String message) {
        return new R<>(ResultCode.OK.getCode(), message, true, null);
    }

    public static <T> R success(T data) {
        return new R<>(ResultCode.OK, true, data);
    }
    public static <T> R success(T data,PagingContext pc) {
        return new R<>(ResultCode.OK, true, data, pc);
    }

    public static <T> R fail() {
        return new R<>(ResultCode.ERROR, false);
    }

    public static <T> R fail(IResultCode resultCode) {
        return new R<>(resultCode, false);
    }

    public static <T> R fail(String code, String message) {
        return new R<>(code, message, false, null);
    }

    public static <T> R fail(IResultCode resultCode, T data) {
        return new R<>(resultCode, false, data);
    }

    public static <T> R fail(String code, String message, T data) {
        return new R<>(code, message, false, data);
    }

    public static <T> R h5Success(T data) {
        return new R<>(ResultCode.H5OK, true, data);
    }
    public static <T> R h5Success(T data,PagingContext pc) {
        return new R<>(ResultCode.H5OK, true, data, pc);
    }
    public static <T> R h5Success() {
        return new R<>(ResultCode.H5OK, true);
    }

    public static <T> R h5Message(String message) {
        return new R<>(ResultCode.H5OK.getCode(), message, true, null);
    }

    public static <T> R h5Fail() {
        return new R<>(ResultCode.H5ERROR, false);
    }

    public static <T> R h5Fail(IResultCode resultCode) {
        return new R<>(resultCode, false);
    }

    public static <T> R h5Fail(String code, String message) {
        return new R<>(code, message, false, null);
    }

    public static <T> R h5Fail(IResultCode resultCode, T data) {
        return new R<>(resultCode, false, data);
    }

    public static <T> R h5Fail(String code, String message, T data) {
        return new R<>(code, message, false, data);
    }

}
