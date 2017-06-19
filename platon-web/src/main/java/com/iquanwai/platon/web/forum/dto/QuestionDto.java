package com.iquanwai.platon.web.forum.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class QuestionDto {
    private List<Integer> tagIds;
    private String topic;
    private String description;
}
