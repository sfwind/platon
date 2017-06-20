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


    // -------------- 业务错误码 -----------------
    // 登陆逻辑错误码
    public static final int SESSION_TIME_OUT = 10001;
    // 未关注公众号
    public static final int NOT_FOLLOW = 10002;
    // 没有购买过碎片化任务
    public static final int NOT_PAY_FRAGMENT = 20001;
    // 没购买过这个任务
    public static final int NOT_PAY_PROBLEM = 20002;
    // 课程未开放
    public static final int COURSE_NOT_OPEN = 20003;
    // 优惠券无效
    public static final int PROMO_CODE_INVALID = 20004;
}
