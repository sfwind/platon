package com.iquanwai.platon.biz.domain.daily;

import com.iquanwai.platon.biz.dao.daily.DailyTalkDao;
import com.iquanwai.platon.biz.po.daily.DailyTalk;
import com.iquanwai.platon.biz.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class DailyServiceImpl implements DailyService{
    @Autowired
    private DailyTalkDao dailyTalkDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DAILY_TALK_BACKEND = "http://static.iqycamp.com/images/dailytalk/daily_talk_backend.png";
    private static BufferedImage talkImg = null;

    @PostConstruct
    public void init(){
        talkImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_BACKEND);
    }


    @Override
    public String drawDailyTalk(String currentDate) {
      DailyTalk dailyTalk =  dailyTalkDao.loadByShowDate(currentDate);
      return drawTalk(dailyTalk);
    }


    /**
     * 绘制每日圈语图片
     * @param dailyTalk
     * @return
     */
    private String drawTalk(DailyTalk dailyTalk){

        // 绘图准备
        ByteArrayOutputStream  outputStream = new ByteArrayOutputStream();


        BufferedImage inputImage = ImageUtils.copy(talkImg);

        ImageUtils.writeToOutputStream(inputImage, "png", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());
        } finally {
            try {
                if(outputStream!=null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                logger.error("os close failed", e);
            }
        }


    }
}
