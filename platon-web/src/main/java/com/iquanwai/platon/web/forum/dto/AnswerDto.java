package com.iquanwai.platon.web.forum.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/20.
 */
@Data
public class AnswerDto {
    private Integer answerId;
    private Integer questionId;
    private String answer;
}
