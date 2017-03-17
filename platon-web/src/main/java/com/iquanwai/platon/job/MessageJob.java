package com.iquanwai.platon.job;

import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.po.HomeworkVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by justin on 17/3/1.
 */
@Component
public class MessageJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private MessageService messageService;

    @Scheduled(cron="${messageJob.cron}")
    public void work(){
        logger.info("MessageJob start");
        //发送点赞数统计
//        likeMessage();
        logger.info("MessageJob end");
    }


    private void likeMessage(){
        List<HomeworkVote> homeworkVotes = practiceService.loadVoteYesterday();

        messageService.sendLikeMessage(homeworkVotes);
    }
}
