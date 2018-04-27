package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by 三十文
 */
@Data
@ApiModel("直播预约情况")
public class LivesOrder {

    private Integer id;
    @ApiModelProperty("预约人id")
    private Integer orderId;
    @ApiModelProperty("推广人id")
    private Integer promotionId;
    @ApiModelProperty("直播id")
    private Integer liveId;
    @ApiModelProperty("预约时间")
    private Date orderTime;
    private Boolean del;

    @ApiModelProperty("推广人 riseId")
    private String promotionRiseId;

}
