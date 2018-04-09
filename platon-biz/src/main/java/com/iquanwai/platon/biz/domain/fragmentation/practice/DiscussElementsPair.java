package com.iquanwai.platon.biz.domain.fragmentation.practice;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class DiscussElementsPair {

    // 精华内容针对的评论
    private DiscussElement originDiscuss;
    // 精华评论
    private DiscussElement priorityDiscuss;

    // 多条评论（针对应用题回复）
    private List<DiscussElement> multiComments;

}
