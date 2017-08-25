package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.CourseReductionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by nethunder on 2017/8/16.
 * Description: 优惠课程推广
 */
public interface CourseReductionService {
    /**
     * 扫描了优惠课程推广二维码
     * @param subscribeEvent 扫描二维码事件的数据
     */
    void scanCourseReductionQR(SubscribeEvent subscribeEvent);

    void saveCourseReductionPayedLog(QuanwaiOrder quanwaiOrder);

    // TODO 8.31日0点删除
    Boolean isPayZhangPeng(Integer profileId);

    /**
     * 获取用户最近参加的推广活动
     * @param profileId 用户id
     * @return 推广活动
     */
    Pair<CourseReductionActivity,PromotionLevel> loadRecentCourseReduction(Integer profileId, Integer problemId);
}
