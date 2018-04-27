package com.iquanwai.platon.biz.po;

import com.iquanwai.platon.biz.domain.fragmentation.manager.Section;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("课程计划")
public class ImprovementPlan {
    private Integer id;
    @ApiModelProperty("问题id")
    private Integer problemId; 
    @ApiModelProperty("开始日期")
    private Date startDate; 
    @ApiModelProperty("课程关闭时间（课程关闭日期）")
    private Date closeDate; 
    @ApiModelProperty("完成时间")
    private Date completeTime;  
    @ApiModelProperty("关闭时间")
    private Date closeTime;  
    @ApiModelProperty("执行状态（1-正在进行, 2-已结束, 3-已过期）")
    private Integer status; 
    @ApiModelProperty("积分")
    private Integer point; 
    @ApiModelProperty("巩固练习完成数量")
    private Integer warmupComplete; 
    @ApiModelProperty("应用练习完成数量")
    private Integer applicationComplete; 
    @ApiModelProperty("总节数")
    private Integer total; 
    @ApiModelProperty("钥匙数量")
    private Integer keycnt; 
    @ApiModelProperty("当前访问的节数")
    private Integer currentSeries; 
    @ApiModelProperty("已完成的节数")
    private Integer completeSeries; 
    @ApiModelProperty("总节数")
    private Integer totalSeries; 
    @ApiModelProperty("是否付费")
    private Boolean riseMember; 
    @ApiModelProperty("求点赞次数")
    private Integer requestCommentCount; 
    @ApiModelProperty("用户id")
    private Integer profileId; 
    @ApiModelProperty("是否删除")
    private Boolean del; 

    @ApiModelProperty("课程")
    private Problem problem; 
    @ApiModelProperty("是否免费")
    private Boolean free; 
    @ApiModelProperty("章节信息")
    private List<Section> sections; 
    @ApiModelProperty("离截止日期天数")
    private Integer deadline; 
    @ApiModelProperty("是否已打分")
    private Boolean hasProblemScore; 
    @ApiModelProperty("之前必做练习未完成,-2 非会员未解锁,-3 课程已过期")
    private Integer lockedStatus = -1;

    /**
     * -1：课程结束，report不能点击 plan的status=3 and 没有完成练习<br/>
     * 1:调用complete事件，plan的status=1时 status=2时 <br/>
     * 3：课程结束，report btn点击后直接跳转到report ， plan.status=3 and 完成练习
     **/
    @ApiModelProperty("report的状态以及点击后的行为")
    private Integer reportStatus;  
    @ApiModelProperty("课程类型描述,默认值课程")
    private String typeDesc = "课程"; 
    @ApiModelProperty("课程类型")
    private Integer type; 
    @ApiModelProperty("几月课程")
    private Integer month; 

    public final static int TYPE_MAJOR = 1;
    public final static int TYPE_MINOR = 2;

    public final static int RUNNING = 1;
    public final static int COMPLETE = 2;
    public final static int CLOSE = 3;

}
