package com.iquanwai.platon.biz.po;

import lombok.Data;

@Data
public class CourseScheduleDefault {

    private Integer id;
    private Integer category;
    private Integer problemId;
    private Integer year;
    private Integer month;
    private Integer type; // 1-主修 2-辅修
    private Boolean del;

    public interface Type {
        /**
         * 主修
         */
        int MAJOR = 1;
        /**
         * 辅修
         */
        int MINOR = 2;
    }

}
