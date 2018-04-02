package com.iquanwai.platon.biz.domain.daily;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.SubscribePushDao;
import com.iquanwai.platon.biz.dao.daily.DailyTalkDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.daily.DailyTalk;
import com.iquanwai.platon.biz.util.*;
import com.sun.imageio.plugins.common.ImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@Service
public class DailyServiceImpl implements DailyService {
    @Autowired
    private DailyTalkDao dailyTalkDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private SubscribePushDao subscribePushDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String DAILY_TALK_BACKEND = ConfigUtils.getPicturePrefix()+"images/dailytalk/source/daily_talk_backend_0402.jpg";
    private static final String DAILY_TALK_TITLE = ConfigUtils.getPicturePrefix()+"images/dailytalk/source/daily_talk_title.png";
    private static final String DAILY_TALK_AUTHOR =ConfigUtils.getPicturePrefix()+ "images/dailytalk/source/daily_talk_author.png";
    private static final String DAILY_TALK_LINE = ConfigUtils.getPicturePrefix()+"images/dailytalk/source/daily_talk_line.png";
    private static BufferedImage talkImg = null;
    private static BufferedImage titleImg = null;
    private static BufferedImage authorImg = null;
    private static BufferedImage lineImg = null;
    private static final String SUBSCRIBE_PUSH_PREFIX = "subscribe_push_";

    private static final Color grey = new Color(51, 51, 51);
    private static final Integer CONTENTSIZE = 24;
    private static final String PRESCENE = "daily_talk_";
    private final static String FTP_TALK_STORE = "/data/static/images/dailytalk/";
    private final static String PREFIX = "/images/dailytalk/";
    @PostConstruct
    public void init() {
        talkImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_BACKEND);
        titleImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_TITLE);
        authorImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_AUTHOR);
        lineImg = ImageUtils.getBufferedImageByUrl(DAILY_TALK_LINE);
    }


    @Override
    public String drawDailyTalk(Integer profileId, String currentDate, Integer loginDay, Integer learnedKnowledge, Integer percent) {
        DailyTalk dailyTalk = dailyTalkDao.loadByShowDate(currentDate);
        Profile profile = accountService.getProfile(profileId);
        return drawTalk(profile, dailyTalk, loginDay, learnedKnowledge, percent);
    }

    @Override
    public void sendMsg(String openid) {

        String templateMsg = "小小的感动，每日的点滴，你的进步就在圈外～赶快加入我们一起学习吧～\n\n<a href='"+ConfigUtils.domainName()+"/rise/static/home'>点击加入商学院</a>";

        customerMessageService.sendCustomerMessage(openid, templateMsg, Constants.WEIXIN_MESSAGE_TYPE.TEXT);
    }

    /**
     * 绘制每日圈语图片
     *
     * @param profile
     * @param dailyTalk
     * @return
     */
    private String drawTalk(Profile profile, DailyTalk dailyTalk, Integer loginDay, Integer learnKnowledge, Integer percent) {
        if (dailyTalk != null) {
            String welcome = ConfigUtils.getDailyTalkWelcome();
            String url = dailyTalk.getImgUrl();
            String content = dailyTalk.getContent();
            String author = dailyTalk.getAuthor();

            String nickName = profile.getNickname();
            String headImg = profile.getHeadimgurl();
            InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
            InputStream simSunIn = ImageUtils.class.getResourceAsStream("/fonts/simsun.ttf");
            // 绘图准备
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                String scene = PRESCENE + profile.getId();
                String callback = ConfigUtils.domainName()+"/rise/static/home";
                Integer result = subscribePushDao.insert(profile.getOpenid(), callback, scene);
                BufferedImage qrImg = qrCodeService.loadQrImage(SUBSCRIBE_PUSH_PREFIX+result+"_"+scene);


                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                Font simsunFont = Font.createFont(Font.TRUETYPE_FONT,simSunIn);
                BufferedImage inputImage = ImageUtils.copy(talkImg);
                //绘制头像
                if (headImg != null) {
                    BufferedImage headImgBuffer = ImageUtils.getBufferedImageByUrl(headImg);
                    if(headImgBuffer!=null) {
                        BufferedImage headBuffer = ImageUtils.copy(headImgBuffer);
                        //圆形
                        headBuffer = ImageUtils.convertCircular(headBuffer);
                        inputImage = ImageUtils.overlapFixImage(inputImage, headBuffer, 70, 69, 120, 120);
                    }
                }
                inputImage = ImageUtils.writeText(inputImage, 235, 124, nickName, font.deriveFont(51f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage, 235, 184, welcome, font.deriveFont(33f), grey);

                if (url != null) {
                    BufferedImage contentImg = ImageUtils.copy(ImageUtils.getBufferedImageByUrl(url));
                    contentImg = ImageUtils.overlapFixImage(contentImg, lineImg, 126, 68, 500, 5);
                    contentImg = ImageUtils.writeTextCenter(contentImg, 84, "每日圈语", simsunFont.deriveFont(30f), Color.WHITE);

                    String[] strs = content.split("\\|");

                    if (content.length() <= CONTENTSIZE) {
                        for (int i = 0; i < strs.length; i++) {
                            contentImg = ImageUtils.writeText(contentImg, 50, 210 + i * 100, strs[i], simsunFont.deriveFont(Font.BOLD,56f), Color.WHITE);
                        }
                    } else {
                        for (int i = 0; i < strs.length; i++) {
                            contentImg = ImageUtils.writeText(contentImg, 50, 190 + i * 60, strs[i], simsunFont.deriveFont(Font.BOLD,40f), Color.WHITE);
                        }
                    }
                    contentImg = ImageUtils.overlapFixImage(contentImg, authorImg, 460, 380, 64, 1);
                    contentImg = ImageUtils.writeText(contentImg, 540, 390, author, simsunFont.deriveFont(30f), Color.WHITE);
                    inputImage = ImageUtils.overlapFixImage(inputImage, contentImg, 0, 600, 1125, 756);
                }

                inputImage = ImageUtils.writeText(inputImage, 70, 442, loginDay.toString(), font.deriveFont(102f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage, 70 + 60 * loginDay.toString().length(), 442, "天", font.deriveFont(33f), grey);

                inputImage = ImageUtils.writeText(inputImage, 370, 442, learnKnowledge.toString(), font.deriveFont(102f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage, 370 + 60 * learnKnowledge.toString().length(), 442, "个", font.deriveFont(33f), grey);
                inputImage = ImageUtils.writeText(inputImage, 745, 442, percent + "%", font.deriveFont(102f), Color.BLACK);
                inputImage = ImageUtils.writeText(inputImage, 845 + 60 * percent.toString().length(), 442, "的同学", font.deriveFont(33f), grey);

                inputImage = ImageUtils.overlapFixImage(inputImage,qrImg,790,1460,220,220);

                Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("jpg");
                ImageWriter writer=null;
                while(it.hasNext()) {
                    writer=it.next();
                    break;
                }
                ImageOutputStream output = ImageIO.createImageOutputStream(outputStream);
                if(writer!=null) {
                    ImageWriteParam params = writer.getDefaultWriteParam();
                    params.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

                    writer.setOutput(output);
                    writer.write(null,new IIOImage(inputImage,null,null), params);
                    output.flush();
                    writer.dispose();
                }
                logger.info("生成模糊图片结束");
                InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                String dailyUrl = PRESCENE+ CommonUtils.randomString(8)+".jpg";
                logger.info("开始通过sftp传输");

                SFTPUtil sftpUtil = new SFTPUtil();

                sftpUtil.upload(FTP_TALK_STORE, dailyUrl, inputStream);

                logger.info("通过sftp传输结束");
                return ConfigUtils.getResourceDomain()+PREFIX+dailyUrl;
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
