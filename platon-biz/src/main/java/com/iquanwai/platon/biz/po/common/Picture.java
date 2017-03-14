package com.iquanwai.platon.biz.po.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2016/12/15.
 */
@Data
@NoArgsConstructor
public class Picture {
    /**
     * 模块Id
     */
    private Integer moduleId;
    /**
     * 上传者IP
     */
    private String remoteIp;
    /**
     * 依赖Id
     */
    private Integer referencedId;
    /**
     * 图片名(有意义的)
     */
    private String name;
    /**
     * 物理名(唯一)
     */
    private String realName;
    /**
     * 图片大小
     */
    private Long length;
    /**
     * 图片类型
     */
    private String type;
    /**
     * 缩略图
     */
    private String Thumbnail;



    public Picture(Long fileSize, Integer referId, String contentType, String remoteIp) {
        this.length = fileSize;
        this.type = contentType;
        this.remoteIp = remoteIp;
        this.referencedId = referId;
    }
}


