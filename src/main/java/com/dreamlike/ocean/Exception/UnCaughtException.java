package com.dreamlike.ocean.Exception;

//当异常未处理时
public class UnCaughtException extends RuntimeException {
    public UnCaughtException(Throwable cause) {
        super(cause);
    }
}

