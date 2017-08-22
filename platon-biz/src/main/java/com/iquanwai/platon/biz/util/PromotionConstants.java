package com.iquanwai.platon.biz.util;

/**
 * 所有推广活动相关参数
 */
public interface PromotionConstants {

    interface Activities {
        String FreeLimit = "freeLimit";
        String Evaluate = "evaluate";
        String CourseReduction = "courseReduction";
    }

    interface FreeLimitAction {
        int InitState = 0; // 初始状态
        int TrialCourse = 1; // 限免试用
        int PayCourse = 2; // 付费购买
    }

    interface EvaluateAction {
        int ScanCard = 11; // 扫测试卡，入 level 表
        int ClickHref = 12; // 点击链接，入 level 表
        int FinishEvaluate = 13; // 完成测评
        int BuyCourse = 14; // 付费购买小课
        int AccessTrial = 15; // 完成推广人数要求，获得学习限免小课资格
    }

    interface CourseReductionAction {
        int ScanCode = 20; // 扫二维码
        int PayCourse = 21; // 购买小课
        int PayMember = 22; // 购买会员
        // TODO 会删除
        int PayZhangPeng = 23; // 购买张鹏小课
    }

}
