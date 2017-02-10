package com.iquanwai.platon.biz.exception;

/**
 * Created by justin on 14-7-22.
 */
public class ErrorConstants {
    // -------------- 错误码 -----------------
    /** 内部错误 */
    public static final int INTERNAL_ERROR = -99;
    /** 微信错误返回 */
    public static final int WEIXIN_RETURN_ERROR = -1;
    /** 没有权限操作 */
    public static final int NO_AUTHORITY = -2;

    // ------------- Mysql错误码 ----------------
    public static final int DUPLICATE_CODE = 1062;

    // -------------- 微信错误码 -----------------
    public static final int ACCESS_TOKEN_EXPIRED = 42001;
    public static final int ACCESS_TOKEN_EXPIRED_NEW = 40014;
    public static final int USER_NO_EXIST = 46004;
    public static final int API_FREQ_OUT_OF_LIMIT = 45009;
    public static final int INVALID_CODE = 40029;
    public static final int ACCESS_TOKEN_INVALID = 40001;


    // -------------- 错误消息 -----------------
    public static final String INTERNAL_ERROR_MSG = "内部错误，亲，烦请联系系统管理员！";

    public static final String NO_AUTHORITY_MSG = "亲，你不能调用当前方法";
}
