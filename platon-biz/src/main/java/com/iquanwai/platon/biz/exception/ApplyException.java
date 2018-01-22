package com.iquanwai.platon.biz.exception;

public class ApplyException extends Exception {
    private String errMsg;

    public ApplyException(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String getMessage() {
        return this.errMsg;
    }
}
