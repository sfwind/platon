package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Comment;
import lombok.Data;

import java.util.List;

/**
 * Created by xfduan on 2017/5/16.
 */
@Data
public class AppMsgCommentReplyDto {
    private Integer id; //应用练习id
    private String topic; //标题
    private String description; //描述
    private List<ApplicationCommentDto> comments; //相关回复信息
    private Integer planId;
    private Boolean integrated;
}


