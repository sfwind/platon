package com.iquanwai.platon.biz.po;


import lombok.Data;

@Data
public class RiseClassMember {

    private Integer id;
    @Deprecated
    private String classId; // classId 2017 年份
    private String className;
    private String groupId; // 班级 01
    private String memberId;
    private Integer profileId; // 用户 id
    private Integer year;
    private Integer month;
    private Integer active; // 是否参与本次学习
    private Integer del;
}
