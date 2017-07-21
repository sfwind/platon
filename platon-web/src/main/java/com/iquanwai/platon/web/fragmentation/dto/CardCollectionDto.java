package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.EssenceCard;
import lombok.Data;

import java.util.List;

/**
 * Created by xfduan on 2017/7/18.
 */
@Data
public class CardCollectionDto {

    private Integer problemId; // 小课 Id
    private String problem; // 小课名称
    private List<EssenceCard> cards; // 小课信息集合

}
