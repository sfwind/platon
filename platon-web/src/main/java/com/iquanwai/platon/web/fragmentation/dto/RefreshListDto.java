package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RefreshListDto<T> {
    private List<T> list;
    private List<T> highlightList;
    private boolean end;
    private Boolean isModifiedAfterFeedback; //是否在教练反馈后修改文章
//    private Boolean feedback; //是否被教练评论


    // 消息中心用到，是否已经被评价
    private Boolean evaluated;
}
