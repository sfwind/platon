package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.CommentEvaluation;
import com.iquanwai.platon.biz.util.RefreshListDto;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RiseRefreshListDto<T> extends RefreshListDto<T> {
    private List<T> highlightList;
    private Boolean isModifiedAfterFeedback; //是否在教练反馈后修改文章
    // private Boolean feedback; //是否被教练评论


    // 消息中心用到，是否已经被评价
    private Boolean evaluated;

    // 应用题评论页面
    private List<CommentEvaluation> commentEvaluations;
}
