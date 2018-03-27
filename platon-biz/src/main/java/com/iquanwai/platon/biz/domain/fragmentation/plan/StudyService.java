package com.iquanwai.platon.biz.domain.fragmentation.plan;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by justin on 2017/12/7.
 */
public interface StudyService {
    StudyLine loadStudyLine(Integer planId);

    /**
     * 查看当前学员点击课程 tab 是否进入倒计时页面
     */
    Pair<Boolean, Integer> shouldGoCountDownPage(Integer profileId);
}
