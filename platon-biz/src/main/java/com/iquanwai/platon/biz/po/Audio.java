package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by nethunder on 2017/8/25.
 * Description 音频
 */
@Data
public class Audio {
    private Integer id;
    private String name;
    private String url;
    private String words;
    private Integer category;
    private Boolean del;
}
