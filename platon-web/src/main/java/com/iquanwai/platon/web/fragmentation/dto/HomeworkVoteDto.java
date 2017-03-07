package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/1/2.
 */
@Data
public class HomeworkVoteDto {
    private Integer type;
    private Integer referencedId;
    private Integer status; // 1 点赞 2 取消
}
