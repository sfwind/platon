package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 17/2/8.
 */
@Data
@ApiModel("知识点讨论")
public class KnowledgeDiscuss extends AbstractComment {
    @ApiModelProperty("知识点id")
    private Integer knowledgeId;
}

