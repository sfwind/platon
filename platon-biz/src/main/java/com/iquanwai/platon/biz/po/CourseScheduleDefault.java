package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * @author nethunder
 * @version 2017-11-04
 */
@Data
public class CourseScheduleDefault {

    private Integer id;
    private Integer category;
    private Integer problemId;
    private Integer year;
    private Integer month;
    private String monthTopic;
    /**
     * 1-主修 2-辅修
     */
    private Integer type;
    private Boolean del;
    /**
     * 初始化时使用的题目和序号id
     */
    private String initChoice;

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

    public interface CategoryType {
        /**
         * 新用户
         */
        int NEW_STUDENT = 1;
        /**
         * 老用户
         */
        int OLD_STUDENT = 2;
    }

}
