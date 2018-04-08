package com.iquanwai.platon.biz.domain.fragmentation.practice;

import lombok.Data;

import java.util.List;

/**
 * Created by 三十文
 */
@Data
public class PersonalDiscuss {

    private DiscussElement discuss;
    private List<DiscussElement> comments;

}
