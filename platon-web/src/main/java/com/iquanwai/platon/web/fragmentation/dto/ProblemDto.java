package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.Problem;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/8.
 */
@Data
public class ProblemDto {
    private String name;
    private List<Problem> problemList;
}
