package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by justin on 16/11/2.
 */
@Data
public class Region {
    private int id; //地区id
    private String name; //地区名称
    private Integer parentId; //父节点id
    private String type; //10-国家,20-省份,30-城市,40-区县

    public static Region defaultRegion(){
        Region def = new Region();
        def.setId(-1);
        def.setName("请选择");
        return def;
    }
}
