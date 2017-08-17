package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.iquanwai.platon.biz.po.common.SubscribeEvent;

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
}
