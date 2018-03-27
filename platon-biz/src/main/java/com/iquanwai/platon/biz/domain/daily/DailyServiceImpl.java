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
    private static final String DAILY_TALK_TITLE = "http://static.iqycamp.com/images/dailytalk/daily_talk_title.png";
    private static final String DAILY_TALK_AUTHOR = "http://static.iqycamp.com/images/dailytalk/daily_talk_author.png";
    private static final String DAILY_TALK_LINE = "http://static.iqycamp.com/images/dailytalk/daily_talk_line.png";
    private static BufferedImage talkImg = null;
    private static BufferedImage titleImg = null;
    private static BufferedImage authorImg = null;
    private static BufferedImage lineImg = null;

    private static final Color grey = new Color(51,51,51);

    @PostConstruct
    public void init() {
        talkImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_BACKEND);
        titleImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_TITLE);
        authorImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_AUTHOR);
        lineImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_LINE);
    }


    @Override
    public String drawDailyTalk(Integer profileId, String currentDate,Integer loginDay,Integer learnedKnowledge,Integer percent) {
        DailyTalk dailyTalk = dailyTalkDao.loadByShowDate(currentDate);
        Profile profile = accountService.getProfile(profileId);
        return drawTalk(profile, dailyTalk,loginDay,learnedKnowledge,percent);
    }


    /**
     * 绘制每日圈语图片
     *
     * @param profile
     * @param dailyTalk
     * @return
     */
    private String drawTalk(Profile profile, DailyTalk dailyTalk,Integer loginDay,Integer learnKnowledge,Integer percent) {
        if (dailyTalk != null) {
            String welcome = ConfigUtils.getDailyTalkWelcome();
            logger.info("dailyTalk:" + dailyTalk.toString());
            String url = dailyTalk.getImgUrl();
            String content = dailyTalk.getContent();
            String author = dailyTalk.getAuthor();

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

                inputImage = ImageUtils.writeText(inputImage, 128, 64, nickName, font.deriveFont(34f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage,128,102,welcome,font.deriveFont(22f),grey);

                if (url != null) {
                    BufferedImage contentImg = ImageUtils.copy(ImageUtils.getBufferedImageByUrl(url));
                    contentImg = ImageUtils.overlapFixImage(contentImg,lineImg,126,64,500,5);
                    contentImg = ImageUtils.writeTextCenter(contentImg,84,"每日圈语",font.deriveFont(30f),Color.WHITE);

                    String[] strs = content.split("\\|");
                    for(int i = 0 ; i<strs.length;i++){
                    contentImg = ImageUtils.writeText(contentImg,50,210+i*100,strs[i],font.deriveFont(60f),Color.WHITE);
                    }
                    contentImg = ImageUtils.overlapFixImage(contentImg,authorImg,460,380,64,1);
                    contentImg = ImageUtils.writeText(contentImg,540,390,author,font.deriveFont(30f),Color.WHITE);
                    inputImage = ImageUtils.overlapFixImage(inputImage, contentImg, 0, 400, 750, 504);
                }

                inputImage = ImageUtils.writeText(inputImage,70,292,loginDay.toString(),font.deriveFont(45f),Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage,70+30*loginDay.toString().length(),292,"天",font.deriveFont(22f),grey);

                inputImage = ImageUtils.writeText(inputImage,280,292,learnKnowledge.toString(),font.deriveFont(45f),Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage,280+30*learnKnowledge.toString().length(),292,"个",font.deriveFont(22f),grey);
                inputImage = ImageUtils.writeText(inputImage,522,292,percent+"%",font.deriveFont(45f),Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage,522+45*percent.toString().length(),292,"的同学",font.deriveFont(22f),grey);
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
