package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("活动对象")
public class ActivitiesFlow extends FlowData {

    private Integer id;
    @ApiModelProperty("活动名称")
    private String name;
    @ApiModelProperty("活动举办人")
    private String holder;
    @ApiModelProperty("活动举办地点")
    private String location;
    @ApiModelProperty("缩略图")
    private String thumbnail;
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("活动状态")
    private Integer status;
    @ApiModelProperty("会员售卖链接")
    private String vipSaleLinkUrl;
    @ApiModelProperty("普通用户售卖链接")
    private String guestSaleLinkUrl;
    @ApiModelProperty("活动链接")
    private String linkUrl;
    @ApiModelProperty("目标链接地址")
    private String targetUrl;
    @ApiModelProperty("链接参数")
    private String linkParam;

    private String startTimeStr;

    public interface Status {
        /** 准备中 */
        int PREPARE = 1;
        /** 已关闭报名 */
        int CLOSED = 2;
        /** 有回顾链接 */
        int REVIEW = 3;
    }

}
