package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.ProblemPlan;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/8.
 */
@Data
public class ProblemListDto {
    private List<ProblemPlan> problemList;
}
