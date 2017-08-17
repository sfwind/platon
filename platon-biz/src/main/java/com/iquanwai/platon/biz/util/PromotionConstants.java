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
        int InitState = 10; // 初始状态
        int ScanCode = 11; // 扫码关注
        int FinishEvaluate = 12; // 完成测评
        int AccessTrial = 13; // 完成推广人数要求，获得学习限免小课资格
    }

    interface CourseReductionAction {
        int ScanCode = 20; // 扫二维码
    }

}
