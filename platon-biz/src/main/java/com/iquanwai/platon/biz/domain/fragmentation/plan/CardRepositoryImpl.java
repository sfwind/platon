package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.EssenceCardDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.EssenceCard;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.ImageUtils;
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
import java.util.Map;

/**
 * Created by justin on 17/8/2.
 */
@Service
public class CardRepositoryImpl implements CardRepository {

    private BufferedImage essenceFreeTop;
    private BufferedImage essenceFreeBottom;
    private BufferedImage essenceNormalTop;
    private BufferedImage pandaCard;

    @Autowired
    private EssenceCardDao essenceCardDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private CacheService cacheService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 课程小结卡的序号
    private static final Integer REVIEW_IMAGE_INDEX = 6;

    private Map<Integer, BufferedImage> bufferedImageMap = Maps.newHashMap();

    private Map<Integer, String> thumbnailMap = Maps.newHashMap();

    private Map<Integer, String> thumbnailLockMap = Maps.newHashMap();

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
        essenceFreeBottom = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/essence_free_bottom_2.png?imageslim");
        essenceNormalTop = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/essence_normal_top.png?imageslim");
        pandaCard = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/panda_card_1.jpg?imageslim");
        logger.info("图片加载完毕");
    }

    @Override
    public String loadEssenceCardImg(Integer profileId, Integer problemId, Integer chapterId) {
        InputStream in = getClass().getResourceAsStream("/fonts/pfmedium.ttf");
        Integer totalSize;
        Problem problem = cacheService.getProblem(problemId);
        if (problem != null) {
            totalSize = problem.getChapterList().size();
        } else {
            return null;
        }

        Profile profile = accountService.getProfile(profileId);
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
        // TargetImage
        BufferedImage targetImage = loadTargetImageByChapterId(chapterId, totalSize);
        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
        // QrImage
        BufferedImage qrImage = loadQrImage("freeLimit_" + profile.getId() + "_" + problemId);
        qrImage = ImageUtils.scaleByPercentage(qrImage, 220, 220);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 34, 1092);
        // HeadImage
        BufferedImage headImg = loadHeadImage(profile);
        headImg = ImageUtils.scaleByPercentage(headImg, 102, 102);
        headImg = ImageUtils.convertCircular(headImg);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 601, 1141);

        EssenceCard essenceCard = essenceCardDao.loadEssenceCard(problemId, chapterId);
        if (essenceCard == null) {
            return null;
        }
        // NickName
        String nickName = CommonUtils.filterEmoji(profile.getNickname());
        if (nickName == null || nickName.length() == 0) {
            targetImage = ImageUtils.writeText(targetImage, 330, 1230, "你的好友邀请你学习，",
                    font.deriveFont(24f), new Color(51, 51, 51));
        } else {
            targetImage = ImageUtils.writeText(targetImage, 330, 1230, subByteString(nickName, 10) + "邀请你学习，",
                    font.deriveFont(24f), new Color(51, 51, 51));
        }
        targetImage = ImageUtils.writeText(targetImage, 330, 1270, "成为" + essenceCard.getTag() + "爆表的人",
                font.deriveFont(24f), new Color(51, 51, 51));
        // 课程标题
        String[] titleArr = essenceCard.getEssenceTitle().split("\\|");
        targetImage = ImageUtils.writeText(targetImage, 330, 320, titleArr[0],
                font.deriveFont(60f), new Color(51, 51, 51));
        targetImage = ImageUtils.writeText(targetImage, 245, 420, titleArr[1],
                font.deriveFont(60f), new Color(255, 255, 255));
        // 渲染课程精华卡片文本
        String[] contentArr = essenceCard.getEssenceContent().split("\\|");
        targetImage = writeContentOnImage(targetImage, contentArr, 404, 500);
        // 限免 非限免 图片区分
        if (problemId.equals(ConfigUtils.getTrialProblemId())) {
            targetImage = ImageUtils.overlapImage(targetImage, essenceFreeTop, 542, 113);
            targetImage = ImageUtils.overlapImage(targetImage, essenceFreeBottom, 306, 1114);
        } else {
            targetImage = ImageUtils.overlapImage(targetImage, essenceNormalTop, 542, 113);
            targetImage = ImageUtils.writeText(targetImage, 306, 1133, "长按识别二维码",
                    font.deriveFont(28f), new Color(0, 0, 0));
            targetImage = ImageUtils.writeText(targetImage, 306, 1169, "查看课程详情",
                    font.deriveFont(24f), new Color(51, 51, 51));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageUtils.writeToOutputStream(targetImage, "jpg", outputStream);
        BASE64Encoder encoder = new BASE64Encoder();
        return "data:image/jpg;base64," + encoder.encode(outputStream.toByteArray());
    }

    // 获取二维码，场景值变化
    private BufferedImage loadQrImage(String scene) {
        // 绘图数据
        QRResponse response = qrCodeService.generateTemporaryQRCode(scene, null);
        InputStream inputStream = qrCodeService.showQRCode(response.getTicket());
        return ImageUtils.getBufferedImageByInputStream(inputStream);
    }

    // 获取用户头像
    private BufferedImage loadHeadImage(Profile profile) {
        // 获取用户头像图片
        String headImgUrl = profile.getHeadimgurl();
        BufferedImage headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        // 如果用户头像过期，则拉取实时新头像
        if (headImg == null) {
            Account realProfile;
            try {
                realProfile = accountService.getAccount(profile.getOpenid(), true);
                headImgUrl = realProfile.getHeadimgurl();
                headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
            } catch (NotFollowingException e) {
                // ignore
            }
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
//                logger.error(e.getLocalizedMessage());
            }
        }
        return builder.toString();
    }

    // 获取绘图底层背景
    private BufferedImage loadTargetImageByChapterId(int chapterId, int totalSize) {
        if (chapterId == totalSize) {
            return bufferedImageMap.get(REVIEW_IMAGE_INDEX);
        } else {
            chapterId = chapterId % bufferedImageMap.size() == 0 ? bufferedImageMap.size() : chapterId % bufferedImageMap.size();
            return bufferedImageMap.get(chapterId);
        }
    }

    @Override
    public String loadTargetThumbnailByChapterId(int chapterId, int totalSize) {
        if (chapterId == totalSize) {
            return thumbnailMap.get(REVIEW_IMAGE_INDEX);
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
    public BufferedImage loadDefaultCardImg(Profile profile) {
        InputStream in = getClass().getResourceAsStream("/fonts/pfmedium.ttf");
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException | IOException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }
        // TargetImage
        BufferedImage targetImage = pandaCard;
        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
        // QrImage
        BufferedImage qrImage = loadQrImage("freeLimit_" + profile.getId() + "_" + ConfigUtils.getTrialProblemId());
        qrImage = ImageUtils.scaleByPercentage(qrImage, 220, 220);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 34, 1092);
        // HeadImage
        BufferedImage headImg = loadHeadImage(profile);
        headImg = ImageUtils.scaleByPercentage(headImg, 102, 102);
        headImg = ImageUtils.convertCircular(headImg);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 578, 1134);
        // NickName
        String nickName = CommonUtils.filterEmoji(profile.getNickname());
        if (nickName == null || nickName.length() == 0) {
            targetImage = ImageUtils.writeText(targetImage, 330, 1230, "你的好友邀请你学习，",
                    font.deriveFont(24f), new Color(51, 51, 51));
        } else {
            targetImage = ImageUtils.writeText(targetImage, 330, 1230, subByteString(nickName, 10) + "邀请你学习，",
                    font.deriveFont(24f), new Color(51, 51, 51));
        }
        targetImage = ImageUtils.writeText(targetImage, 330, 1270, "成为洞察力爆表的人",
                font.deriveFont(24f), new Color(51, 51, 51));

        return targetImage;
    }

}
