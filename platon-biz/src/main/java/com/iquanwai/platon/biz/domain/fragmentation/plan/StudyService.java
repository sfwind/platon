package com.iquanwai.platon.biz.domain.fragmentation.plan;

/**
 * Created by justin on 2017/12/7.
 */
public interface StudyService {
    StudyLine loadStudyLine(Integer planId);

    /**
     * 获取最近将要生效的一个会员身份信息
     */
    CountDownElement loadLatestCountDownElement(Integer profileId);
}
