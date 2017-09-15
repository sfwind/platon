package com.iquanwai.platon.biz.po.bible;

import lombok.Data;

/**
 * Created by justin on 2017/9/13.
 */
@Data
public class SubscribeUserTag {
    private int id;
    private Integer profileId;
    private Integer tagId;
    private Boolean del;
}
