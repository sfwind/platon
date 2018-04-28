package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.EssenceCardDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.po.EssenceCard;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.user.StudyInfo;
import com.iquanwai.platon.biz.util.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/8/2.
 */
@Service
public class CardManagerImpl implements CardManager {

    private BufferedImage essenceFreeTop;
    private BufferedImage essenceFreeBottom;
    private BufferedImage essenceNormalTop;
    private BufferedImage caitongHead;
    /**
     * 采铜直播背景图
     */
    private BufferedImage caitongBGImage;

    @Autowired
    private EssenceCardDao essenceCardDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private ProblemScheduleManager problemScheduleManager;

    private static Logger logger = LoggerFactory.getLogger(CardManagerImpl.class);

    private Map<Integer, BufferedImage> bufferedImageMap = Maps.newHashMap();

    private Map<Integer, String> thumbnailMap = Maps.newHashMap();

    private Map<Integer, String> thumbnailLockMap = Maps.newHashMap();

    private static final String CARD_ACTIVITY = PromotionConstants.Activities.FREE_LIMIT;

    private static final Color GREY = new Color(51, 51, 51);

    private static final Integer MAX_LINE_WORDS_LENGTH = 17;

    @PostConstruct
    public void init() {
        // 初始化所有背景图的 bufferedImages 缓存
        JSONObject base64ImageJson = JSONObject.parseObject(ConfigUtils.getEssenceCardBackImgs());
        for (int i = 0; i < base64ImageJson.size(); i++) {
            String url = base64ImageJson.getString(Integer.toString(i + 1));
            bufferedImageMap.put(i + 1, ImageUtils.getBufferedImageByUrl(url));
        }
        JSONObject essenceThumbnail = JSONObject.parseObject(ConfigUtils.getEssenceCardThumbnails());
        for (int i = 0; i < essenceThumbnail.size(); i++) {
            String thumbnail = essenceThumbnail.getString(Integer.toString(i + 1));
            thumbnailMap.put(i + 1, thumbnail);
        }
        JSONObject essenceThumbnailLock = JSONObject.parseObject(ConfigUtils.getEssenceCardThumbnailsLock());
        for (int i = 0; i < essenceThumbnailLock.size(); i++) {
            String thumbnailLock = essenceThumbnailLock.getString(Integer.toString(i + 1));
            thumbnailLockMap.put(i + 1, thumbnailLock);
        }

        essenceFreeTop = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/essence_free_top.png?imageslim");
        essenceFreeBottom = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/essence_free_bottom_4.png?imageslim");
        essenceNormalTop = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/essence_normal_top.png?imageslim");
        caitongBGImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/caitong_background.jpg?imageslim");
        caitongHead = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/caitong_head_image.jpg?imageslim");
        logger.info("pic loading complete");
    }

    @Override
    public String loadEssenceCardImg(Integer profileId, Integer problemId, Integer chapterId, Integer planId, StudyInfo studyInfo) {
        InputStream in = getClass().getResourceAsStream("/fonts/pfmedium.ttf");
        List<Chapter> list = problemScheduleManager.loadRoadMap(planId);
        Integer totalSize = list.size();

        Profile profile = accountService.getProfile(profileId);
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("is closed error", e);
            }
        }
        // TargetImage
        BufferedImage targetImage = loadTargetImageByChapterId(chapterId, totalSize);
        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);

        //绘制头像
        // HeadImage
        BufferedImage headImg = loadHeadImage(profile);
        headImg = ImageUtils.scaleByPercentage(headImg, 77, 77);
        headImg = ImageUtils.convertCircular(headImg);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 163, 78);

        Integer learnedDay = studyInfo.getLearnedDay();
        Integer learnedKnowledge = studyInfo.getLearnedKnowledge();
        Integer defeatPercent = studyInfo.getDefeatPercent();

        //绘制学习相关信息
        targetImage = ImageUtils.writeText(targetImage, 118, 285, learnedDay.toString(), font.deriveFont(Font.BOLD, 30f), GREY);
        targetImage = ImageUtils.writeText(targetImage, 123 + 18 * learnedDay.toString().length(), 285, "天", font.deriveFont(20f), GREY);

        targetImage = ImageUtils.writeText(targetImage, 322, 285, learnedKnowledge.toString(), font.deriveFont(Font.BOLD, 30f), GREY);
        targetImage = ImageUtils.writeText(targetImage, 326 + 18 * learnedKnowledge.toString().length(), 285, "个", font.deriveFont(20f), GREY);
        targetImage = ImageUtils.writeText(targetImage, 546, 285, defeatPercent.toString() + "%", font.deriveFont(Font.BOLD, 30f), GREY);
        targetImage = ImageUtils.writeText(targetImage, 555 + 21 * (defeatPercent.toString().length() + 1), 285, "的同学", font.deriveFont(20f), GREY);

        EssenceCard essenceCard = essenceCardDao.loadEssenceCard(problemId, chapterId);
        if (essenceCard == null) {
            return null;
        }

        String[] titleArr = essenceCard.getEssenceTitle().replaceAll(" ", "").split("\\|");
        for (int i = 0; i < titleArr.length; i++) {
            String title = titleArr[i];
            for (int j = 0; j < title.length(); j++) {
                targetImage = ImageUtils.writeText(targetImage, 282 - 20 * title.length() + 40 * j, 412 + i * 53, title.substring(j, j + 1), font.deriveFont(34f), GREY);
            }
        }
        String[] contentArr = essenceCard.getEssenceContent().split("\\|");
        Integer currentHeight = 566;
        for (int i = 0; i < contentArr.length; i++) {
            List<String> strs = getByLineWords(contentArr[i]);
            for (int j = 0; j < strs.size(); j++) {
                targetImage = ImageUtils.writeText(targetImage, 124, currentHeight, strs.get(j), font.deriveFont(18f), GREY);
                currentHeight += 30;
            }
            currentHeight += 15;
        }

        //昵称和日期
        String date = DateUtils.parseDateToFormat5(new Date());
        targetImage = ImageUtils.writeText(targetImage, 320, 1032, date, font.deriveFont(18f), GREY);
        targetImage = ImageUtils.writeText(targetImage, 320, 1058, profile.getNickname(), font.deriveFont(18f), GREY);

        //二维码
        BufferedImage qrImage = loadQrImage(CARD_ACTIVITY + "_" + profile.getId() + "_" + problemId);
        qrImage = ImageUtils.scaleByPercentage(qrImage, 85, 85);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 324, 1185);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtils.writeToOutputStream(targetImage, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.error("os close failed", e);
            }
        }
    }

    // 获取二维码，场景值变化
    private BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = qrCodeService.generateTemporaryQRCode(scene, null);
        InputStream inputStream = qrCodeService.showQRCode(response.getTicket());
        try {
            return ImageUtils.getBufferedImageByInputStream(inputStream);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("is close failed", e);
            }
        }
    }

    // 获取用户头像
    private BufferedImage loadHeadImage(Profile profile) {
        // 获取用户头像图片
        String headImgUrl = profile.getHeadimgurl();
        BufferedImage headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        // 如果用户头像过期，则拉取实时新头像
        if (headImg == null) {
            Account realProfile = accountService.getAccountByUnionId(profile.getUnionid());
            headImgUrl = realProfile.getHeadimgurl();
            headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        }
        // 修复两次都没有头像的用户，使用默认头像
        if (headImg == null) {
            String defaultImageUrl = "https://static.iqycamp.com/images/fragment/headimg_default_1.jpg?imageslim";
            headImg = ImageUtils.getBufferedImageByUrl(defaultImageUrl);
        }
        return headImg;
    }

    // 将段落正文填充到图像中
    private BufferedImage writeContentOnImage(BufferedImage targetImage, String[] contentArr, Integer x, Integer y) {
        for (String content : contentArr) {
            Pair<BufferedImage, Integer> pair = writeSinglePara(targetImage, content, x, y + 35);
            if (pair != null) {
                targetImage = pair.getLeft();
                y = pair.getRight();
            }
        }
        return targetImage;
    }

    /**
     * @return 修改之后的 BufferedImage + 底层高度
     */
    private Pair<BufferedImage, Integer> writeSinglePara(BufferedImage targetImage, String text, Integer x, Integer y) {
        InputStream in = getClass().getResourceAsStream("/fonts/pfmedium.ttf");
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("is closed error", e);
            }
        }
        java.util.List<String> textArr = splitLinesByBytes(text, 24);
        for (int i = 0; i < textArr.size(); i++) {
            targetImage = ImageUtils.writeText(targetImage, x, y + i * 35, textArr.get(i),
                    font.deriveFont(24f), new Color(51, 51, 51));
        }
        Integer endY = y + textArr.size() * 35;
        return new MutablePair<>(targetImage, endY);
    }


    private java.util.List<String> splitLinesByBytes(String str, Integer lineByteLength) {
        java.util.List<String> list = Lists.newArrayList();
        int tempLength = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            try {
                tempLength += str.substring(i, i + 1).getBytes("gbk").length;
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getLocalizedMessage());
            }
            stringBuilder.append(str.substring(i, i + 1));
            if (tempLength >= lineByteLength - 1) {
                list.add(stringBuilder.toString());
                tempLength = 0;
                stringBuilder = new StringBuilder("");
            } else if (i == str.length() - 1) {
                list.add(stringBuilder.toString());
            }
        }
        return list;
    }

    private static String subByteString(String str, Integer byteLength) {
        StringBuilder builder = new StringBuilder("");
        for (int i = 0; i < str.length(); i++) {
            builder.append(str.substring(i, i + 1));
            try {
                if (builder.toString().getBytes("gbk").length >= byteLength - 1) {
                    String targetStr = builder.toString();
                    return str.equals(targetStr) ? targetStr : targetStr + "...";
                }
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return builder.toString();
    }

    // 获取绘图底层背景
    private BufferedImage loadTargetImageByChapterId(int chapterId, int totalSize) {
        if (chapterId == totalSize) {
            return bufferedImageMap.get(bufferedImageMap.size());
        } else {
            chapterId = chapterId % bufferedImageMap.size() == 0 ? bufferedImageMap.size() : chapterId % bufferedImageMap.size();
            return bufferedImageMap.get(chapterId);
        }
    }

    @Override
    public String loadTargetThumbnailByChapterId(int chapterId, int totalSize) {
        if (chapterId == totalSize) {
            return thumbnailMap.get(thumbnailMap.size());
        } else {
            chapterId = chapterId % thumbnailMap.size() == 0 ? thumbnailMap.size() : chapterId % thumbnailMap.size();
            return thumbnailMap.get(chapterId);
        }
    }

    @Override
    public String loadTargetThumbnailLockByChapterId(int chapterId, int totalSize) {
        if (chapterId == totalSize) {
            return thumbnailLockMap.get(thumbnailLockMap.size());
        } else {
            chapterId = chapterId % thumbnailLockMap.size() == 0 ? thumbnailLockMap.size() : chapterId % thumbnailLockMap.size();
            return thumbnailLockMap.get(chapterId);
        }
    }

    @Override
    public BufferedImage loadCaitongBgImage() {
        return caitongBGImage;
    }

    @Override
    public BufferedImage loadCaitongHead() {
        return caitongHead;
    }


    private List<String> getByLineWords(String words) {
        List<String> strs = Lists.newArrayList();
        int size = words.length() / MAX_LINE_WORDS_LENGTH;

        for (int i = 1; i <= size; i++) {
            strs.add(words.substring(MAX_LINE_WORDS_LENGTH * (i - 1), MAX_LINE_WORDS_LENGTH * i));
        }
        if (size * MAX_LINE_WORDS_LENGTH < words.length()) {
            strs.add(words.substring(size * MAX_LINE_WORDS_LENGTH, words.length()));
        }
        return strs;
    }


    public static void main(String[] args) {
        String title = "如何发现 | 错误假设的谬误";
        System.out.println(title);
        System.out.println(title.replaceAll(" ", ""));
    }
}
