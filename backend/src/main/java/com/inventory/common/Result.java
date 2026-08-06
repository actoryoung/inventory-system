package com.inventory.common;

import java.io.Serializable;

/**
 * 统一返回体
 *
 * 所有 Controller 与全局异常处理统一使用 {@link Result} 包装响应，
 * 保证成功与失败返回结构一致：{code, message, data}。
 *
 * @param <T> 数据类型
 * @author inventory-system
 * @since 2026-08-06
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功状态码 */
    public static final int SUCCESS_CODE = 200;

    /** 业务/参数错误状态码 */
    public static final int ERROR_CODE = 400;

    /** 系统异常状态码 */
    public static final int SYSTEM_ERROR_CODE = 500;

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功（默认消息 success）
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS_CODE, "success", data);
    }

    /**
     * 成功（自定义消息）
     */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 失败
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
