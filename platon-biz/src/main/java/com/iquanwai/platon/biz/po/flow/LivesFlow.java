package com.iquanwai.platon.biz.po.flow;

import com.iquanwai.platon.biz.po.FlowData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("直播对象")
public class LivesFlow extends FlowData {

    private Integer id;
    @ApiModelProperty("直播名称")
    private String name;
    @ApiModelProperty("主讲人")
    private String speaker;
    @ApiModelProperty("主讲人描述")
    private String speakerDesc;
    @ApiModelProperty("直播描述")
    private String liveDesc;
    @ApiModelProperty("缩略图")
    private String thumbnail;
    @ApiModelProperty("大展示图")
    private String banner;
    @ApiModelProperty("开始时间")
    private Date startTime;
    @ApiModelProperty("结束时间")
    private Date endTime;
    @ApiModelProperty("链接地址")
    private String linkUrl;
    @ApiModelProperty("开始时间字符串形式表示")
    private String startTimeStr;
    @ApiModelProperty("直播状态, 1-倒计时,2-直播中,3-回看")
    private Integer status;
    @ApiModelProperty("排序")
    private Integer sequence;
    @ApiModelProperty("是否已经预约")
    private Boolean isOrdered;
    @ApiModelProperty("个人 riseId")
    private String riseId;

}
