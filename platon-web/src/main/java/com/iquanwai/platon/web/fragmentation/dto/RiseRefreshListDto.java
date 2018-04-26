package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.CommentEvaluation;
import com.iquanwai.platon.biz.util.RefreshListDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
@ApiModel("分页加载数据")
public class RiseRefreshListDto<T> extends RefreshListDto<T> {
    @ApiModelProperty("是否在教练反馈后修改文章")
    private Boolean isModifiedAfterFeedback;
    @ApiModelProperty("消息中心用到，是否已经被评价")
    private Boolean evaluated;
    @ApiModelProperty("评价教练点评model")
    private List<CommentEvaluation> commentEvaluations;
}
