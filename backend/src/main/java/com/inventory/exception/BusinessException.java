package com.inventory.exception;

/**
 * 业务异常
 *
 * @author inventory-system
 * @since 2026-01-04
 */
public class BusinessException extends RuntimeException {

    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
