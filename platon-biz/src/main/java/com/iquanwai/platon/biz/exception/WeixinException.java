package com.iquanwai.platon.biz.exception;

/**
 * Created by justin on 8/22/16.
 */
public class WeixinException extends Exception {
    private String errMsg;
    private int errcode;

    public WeixinException(int errcode, String errMsg) {
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeixinException(String message, String errMsg, int errcode) {
        super(message);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeixinException(String message, Throwable throwable, String errMsg, int errcode) {
        super(message, throwable);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    public WeixinException(Throwable throwable, String errMsg, int errcode) {
        super(throwable);
        this.errMsg = errMsg;
        this.errcode = errcode;
    }

    @Override
    public String getMessage(){
        return errMsg;
    }

    public int getErrcode() {
        return errcode;
    }
}
