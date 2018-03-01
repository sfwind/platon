package com.iquanwai.platon.biz.exception;


/**
 * @author nethunder
 */
public class CreateCourseException extends Exception {
    private String errMsg;

    public CreateCourseException(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String getMessage() {
        return this.errMsg;
    }
}
