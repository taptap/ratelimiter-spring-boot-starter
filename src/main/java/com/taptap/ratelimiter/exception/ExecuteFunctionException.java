package com.taptap.ratelimiter.exception;

public class ExecuteFunctionException extends RuntimeException {

    public ExecuteFunctionException(String message, Throwable cause) {
        super(message, cause);
    }
}