package com.iquanwai.platon.biz.po.forum;

import lombok.Data;

/**
 * Created by justin on 17/6/19.
 */
@Data
public class ForumTag {
    private int id;
    private String name; //标签名称
    private Boolean del; //是否删除（0-未删除，1-已删除）
}
