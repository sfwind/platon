package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by 三十文 on 2017/9/11
 */
@Data
public class ForceOpenPlanParams {
    /**
     * 用户id
     */
    private List<Integer> profileIds;
    /**
     * 小课id
     */
    private Integer problemId;
    /**
     * 开课时间
     */
    private Date startDate;
    /**
     * 关闭时间
     */
    private Date closeDate;
    /**
     * 是否发送模板消息，默认不发
     */
    private Boolean sendWelcomeMsg = false;

}
