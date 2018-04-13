package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by 三十文 on 2017/10/21
 */
@Data
public class FullAttendanceReward {

    private Integer id;
    private Integer profileId;
    // 课程 id
    private Integer problemId;
    @Deprecated
    private String className;
    @Deprecated
    private String groupId;
    @Deprecated
    private String memberId;
    // 年份
    private Integer year;
    // 月份
    private Integer month;
    // 金额
    private Double amount;
    // 是否通知
    private Integer notified;
    // 是否删除
    private Integer del;

    // 非 db 字段
    // 学员身份类型
    private Integer memberTypeId;

}
