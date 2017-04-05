package com.iquanwai.platon.biz.po;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Knowledge {
    private int id;
    private String knowledge; //知识点
    private String step; //知识点所属步骤
    private String analysis; //作用
    private String means; //方法
    private String keynote; //要点
    private String pic; //图片链接
    private String audio; //语音链接
    private Integer appear; //非db字段,是否出现过

    private static List<Integer> REVIEW_KNOWLEDGE = Lists.newArrayList(61,62);

    public static boolean isReview(Integer knowledgeId){
        return REVIEW_KNOWLEDGE.contains(knowledgeId);
    }

}
