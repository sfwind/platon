package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by nethunder on 2017/3/23.
 */
@Data
public class ProblemScore {
    private Integer id;
    private String openid;
    private Integer profileId;
    private Integer problemId;
    private Integer question;
    private Integer choice;
}
