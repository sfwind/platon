package com.iquanwai.platon.web.fragmentation.controller.bible.dto;

import com.iquanwai.platon.biz.po.bible.SubscribePointCompare;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/9/7.
 */
@Data
public class BibleScore {
    private List<SubscribePointCompare> compareGroup;
    private String riseId;
    private Integer totalWords;
    private String qrCode;
    private String nickName;
    private String headImage;
    private String totalScore;
    private String readTime;
}
