package com.iquanwai.platon.biz.po.common;

import lombok.Data;

/**
 * Created by nethunder on 2016/12/15.
 */
@Data
public class PictureModule {
    /**
     * 模块Id
     */
    private Integer id;
    /**
     * 模块名
     */
    private String moduleName;
    /**
     * 相对地址
     */
    private String path;
    /**
     * 是否有缩略图
     */
    private Integer hasThumbnail;
    /**
     * 大小限制
     */
    private Integer SizeLimit;
    /**
     * 类型限制
     */
    private String typeLimit;
}
