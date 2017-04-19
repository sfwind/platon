package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.Knowledge;
import lombok.Data;

/**
 * Created by justin on 17/4/12.
 */
@Data
public class Section {
    private Integer section;  //第几小节
    private Knowledge knowledge; //知识点
    private String name; //小节名称
    private Integer series; //序号
    private Boolean integrated; //是否是综合练习
}
