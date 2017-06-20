package com.iquanwai.platon.web.forum.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/20.
 */
@Data
public class AnswerCommentDto {
    private Integer answerId;
    private Integer repliedCommentId;
    private String comment;
}
