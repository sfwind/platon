package com.iquanwai.platon.biz.domain.fragmentation.plan;

import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文 on 2017/9/11
 */
@Data
public class ForceOpenPlanParams {

    private Integer profileId;
    private Integer problemId;
    private Date startDate;
    private Date closeDate;

}
