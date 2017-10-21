package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by 三十文 on 2017/10/21
 */
@Data
public class FullAttendanceReward {

    private Integer id;
    private Integer profileId;
    private String className;
    private String groupId;
    private String memberId;
    private Integer year;
    private Integer month;
    private Double Amount;
    private Integer notified;
    private Integer del;

    private Integer problemId;

}
