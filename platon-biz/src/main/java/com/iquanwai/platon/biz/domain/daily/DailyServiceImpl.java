package com.iquanwai.platon.biz.domain.daily;

import com.iquanwai.platon.biz.dao.daily.DailyTalkDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.daily.DailyTalk;
import com.iquanwai.platon.biz.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class DailyServiceImpl implements DailyService {
    @Autowired
    private DailyTalkDao dailyTalkDao;
    @Autowired
    private AccountService accountService;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DAILY_TALK_BACKEND = "http://static.iqycamp.com/images/dailytalk/daily_talk_backend.png";
    private static BufferedImage talkImg = null;

    @PostConstruct
    public void init() {
        talkImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_BACKEND);
    }


    @Override
    public String drawDailyTalk(Integer profileId, String currentDate) {
        DailyTalk dailyTalk = dailyTalkDao.loadByShowDate(currentDate);
        Profile profile = accountService.getProfile(profileId);
        return drawTalk(profile, dailyTalk);
    }


    /**
     * 绘制每日圈语图片
     *
     * @param profile
     * @param dailyTalk
     * @return
     */
    private String drawTalk(Profile profile, DailyTalk dailyTalk) {
        if (dailyTalk != null) {
            logger.info("dailyTalk:" + dailyTalk.toString());
            String url = dailyTalk.getImgUrl();
            String content = dailyTalk.getContent();

            String nickName = profile.getNickname();
            String headImg = profile.getHeadimgurl();
            InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
            // 绘图准备
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                BufferedImage inputImage = ImageUtils.copy(talkImg);
                //绘制头像
                if (headImg != null) {
                    BufferedImage headBuffer = ImageUtils.copy(ImageUtils.getBufferedImageByUrl(headImg));
                    //圆形
                    headBuffer = ImageUtils.convertCircular(headBuffer);
                    inputImage = ImageUtils.overlapFixImage(inputImage, headBuffer, 40, 32, 74, 74);
                }

                inputImage = ImageUtils.writeText(inputImage, 128, 200, nickName, font.deriveFont(42f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage,128,300,"又在圈外商学院学习一天",font.deriveFont(42f),Color.red);

                if (url != null) {
                    BufferedImage contentImg = ImageUtils.copy(ImageUtils.getBufferedImageByUrl(url));
                    inputImage = ImageUtils.overlapFixImage(inputImage, contentImg, 0, 400, 750, 504);
                }

                ImageUtils.writeToOutputStream(inputImage, "png", outputStream);

                BASE64Encoder encoder = new BASE64Encoder();
                return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("os close failed", e);
                }
            }
        }
        return null;
    }
}