package com.iquanwai.platon.biz.po;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 17/8/29.
 */
@Data
public class RiseCertificate {
    private int id;
    private Integer profileId; //用户 Id
    private String realName; // 用户真实姓名
    private Integer memberTypeId; // 会员身份 Id
    private Integer type; //证书类型（1-优秀班长，2-优秀组长，3-优秀学员，4-优秀团队, 5-结课证书，6-优秀助教，7-优秀班委）
    /**
     * IQW{Type, 02d}{RiseClassMember.MemberId}{Month, 02d}{序号, 03d}{随机数, 02d}`
     */
    private String certificateNo; //证书id
    private Integer year; //开营年
    private Integer month; //开营月
    private Integer groupNo; //小组号
    private Integer problemId; // 课程 id
    private String problemName; //课程
    private Boolean notified; //是否通知（0-否，1-是）
    private String imageUrl; // 证书图片 url
    private Date downloadTime; // 证书下载时间
    private Boolean del; //是否删除（0-否，1-是）


    // 非 DB 字段
    private String name; //证书获得者 非db字段
    private String typeName; //证书类型名称 非db字段
    private String congratulation; //证书描述 非db字段
    private String nextCertificateNo; // 下一位证书的证书 No，下载证书图片用

    private List<String> memberIds;

    public interface Type {
        /**
         * 优秀班长
         */
        int EXCELLENT_CLASS_LEADER = 1;
        /**
         * 优秀组长
         */
        int EXCELLENT_GROUP_LEADER = 2;
        /**
         * 优秀学员
         */
        int EXCELLENT_STUDENT = 3;
        /**
         * 优秀团队
         */
        int EXCELLENT_TEAM = 4;
        /**
         * 毕业证书
         */
        int GRADUATE = 5;
        /**
         * 优秀教练
         */
        int EXCELLENT_COACH = 6;
    }

}
