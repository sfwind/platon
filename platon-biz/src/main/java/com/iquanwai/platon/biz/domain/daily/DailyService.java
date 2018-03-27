package com.iquanwai.platon.biz.domain.daily;



public interface DailyService {

    /**
     * 绘制每日圈语
     * @param profileId
     * @param currentDate
     * @return
     */
    String drawDailyTalk(Integer profileId,String currentDate,Integer loginDay,Integer learnedKnowledge,Integer percent);


}
