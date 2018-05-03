package com.iquanwai.platon.biz.po.survey;

import lombok.Data;

@Data
public class SurveyQuestionType {
    private Integer id;
    private String questionCode;
    private Boolean liar;
    private Boolean reverse;
    private Integer category;
    private Integer variable;
    private Integer maxChoice;
    private Boolean del;

    /**
     * 非DB字段，计算后的分数
     */
    private Double point;

    public interface CategoryType {
        int INTERPERSONAL = 1;
        int OPERATIONAL = 2;
        int INTELLECTUAL = 3;
        int DISPOSITIONAL = 4;
        int SPECIAL = 5;
    }

    public interface VariableType {
        int INTERPERSONAL_COMMUNICATION = 1;
        int TEAM_MANAGEMENT = 2;
        int TEAM_COLLABORATION = 3;

        int IMPLEMENTATION_SPEED = 4;
        int PROBLEM_SOLVING = 5;
    }
}
