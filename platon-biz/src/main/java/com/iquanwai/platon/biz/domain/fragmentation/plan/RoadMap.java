package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.Knowledge;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/4/5.
 */
@Data
public class RoadMap {
    private String intro;
    private Integer series;
    private List<Knowledge> knowledgeList;
    private String step;
    private boolean isIntegrated;
}
