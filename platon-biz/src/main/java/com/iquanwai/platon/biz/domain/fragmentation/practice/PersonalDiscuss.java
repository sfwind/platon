package com.iquanwai.platon.biz.domain.fragmentation.practice;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class PersonalDiscuss {

    // 用户本人评论内容
    private DiscussElement discuss;
    // 针对本人评论的评论集合
    private List<DiscussElement> comments;

}
