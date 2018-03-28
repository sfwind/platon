package com.iquanwai.platon.biz.domain.daily;


import com.iquanwai.platon.biz.po.daily.DailyTalk;

public interface DailyService {

    /**
     * 绘制每日圈语
     * @param profileId
     * @param currentDate
     * @return
     */
    String drawDailyTalk(Integer profileId,String currentDate,Integer loginDay,Integer learnedKnowledge,Integer percent);

    /**
     * 获取当天的圈语内容
     * @param currentDate
     * @return
     */
    DailyTalk loadByTalkDate(String currentDate);


}
