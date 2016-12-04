package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Scene {
    private int id;
    private String scene; //场景
    private Integer parentId; //父场景id
}
