package com.iquanwai.platon.biz.util;

/**
 * 所有推广活动相关参数
 */
public interface PromotionConstants {

    interface Activities {
        String FREE_LIMIT = "freeLimit";
        String EVALUATE = "evaluate";
        String COURSE_REDUCTION = "courseReduction";
        String CAITONG_LIVE = "caitonglive";
        String BIBLE = "bible";
    }

    interface FreeLimitAction {
        int INIT_STATE = 0; // 初始状态
        int TRIAL_COURSE = 1; // 限免试用
        int PAY_COURSE = 2; // 付费购买
    }

    interface EvaluateAction {
        int SCAN_CARD = 11; // 扫测试卡，入 level 表
        int CLICK_HREF = 12; // 点击链接，入 level 表
        int FINISH_EVALUATE = 13; // 完成测评
        int BUY_COURSE = 14; // 付费购买小课
        int ACCESS_TRIAL = 15; // 完成推广人数要求，获得学习限免小课资格
    }

    interface CourseReductionAction {
        int SCAN_CODE = 20; // 扫二维码
        int PAY_COURSE = 21; // 购买小课
        int PAY_MEMBER = 22; // 购买会员
        // TODO 会删除
        int PAY_ZHANG_PENG = 23; // 购买张鹏小课
    }

    interface CaitongLiveAction {
        int SCAN_CODE = 30; // 扫采铜二维码
        int QUESTION1 = 31; // 开始做第一题
        int QUESTION2 = 32;
        int QUESTION3 = 33;
        int QUESTION4 = 34;
        int QUESTION5 = 35;
        int QUESTION6 = 36;
        int QUESTION7 = 37;
        int QUESTION8 = 38;
        int QUESTION9 = 39;
        int QUESTION10 = 40; // 开始做第十题

        int BACKPACK = 44; // 背包
        int COMPLETE = 45; // 完成所有题目
        int GO_DIE = 46; // 当前进行的题做错了
        int CLOSE_GAME = 47; // 结束游戏
        int MANUAL_START = 48; // 二层用户手动开始游戏
        int CLOSE_TIP = 49; // 关闭提示
    }

    interface BibleAction {
        int SCAN_CODE = 31; // 扫二维码
    }

}
