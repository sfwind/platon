package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by justin on 2017/9/14.
 */
@Data
public class RelevantTag {
    private int id;
    private Integer tagId;//标签id
    private Integer relevantTagId; //关联标签id
}
