package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.domain.fragmentation.practice.DiscussElementsPair;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PersonalDiscuss;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("讨论区评论与回复数据结构")
public class DiscussDistrictDto {

    @ApiModelProperty("自我相关评论")
    List<PersonalDiscuss> personal;
    @ApiModelProperty("精华评论信息")
    List<DiscussElementsPair> priorities;

}
