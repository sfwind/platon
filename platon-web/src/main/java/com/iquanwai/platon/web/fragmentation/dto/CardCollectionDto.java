package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.EssenceCard;
import lombok.Data;

import java.util.List;

/**
 * Created by xfduan on 2017/7/18.
 */
@Data
public class CardCollectionDto {

    private Integer problemId; // 课程 Id
    private String problem; // 课程名称
    private Boolean isRiseMember; // 是否会员
    private List<EssenceCard> cards; // 精华卡信息集合

}
