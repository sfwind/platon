package com.iquanwai.platon.biz.domain.fragmentation.practice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
@ApiModel("选择题讨论区")
public class WarmupDiscussDistrict {

    @ApiModelProperty("自我相关评论")
    List<PersonalDiscuss> personal;
    @ApiModelProperty("精华评论信息")
    List<DiscussElementsPair> priorities;

}
