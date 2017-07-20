package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.ImageUtils;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/12/8.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

    @Autowired
    private CacheService cacheService;
    @Autowired
    private ProblemScoreDao problemScoreDao;
    @Autowired
    private ProblemExtensionDao problemExtensionDao;
    @Autowired
    private ProblemActivityDao problemActivityDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private ProblemDao problemDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private EssenceCardDao essenceCardDao;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private AccountService accountService;

    // bufferedImages缓存
    private List<BufferedImage> bufferedImages;

    // TODO
    @PostConstruct
    public void init() {
        bufferedImages = Lists.newArrayList();
        JSONObject base64ImageJsonArr = JSONObject.parseObject(ConfigUtils.getEssenceCardBackImgs());
        List<String> imageUrlArr = Lists.newArrayList();
        for (int i = 0; i < base64ImageJsonArr.size(); i++) {
            imageUrlArr.add(base64ImageJsonArr.getString(Integer.toString(i + 1)));
        }
        imageUrlArr.forEach(item -> {
            bufferedImages.add(ImageUtils.getBufferedImageByUrl(item));
        });
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public List<Problem> loadProblems() {
        //去除已删除的小课
        return cacheService.getProblems().stream().
                filter(problem -> !problem.getDel()).collect(Collectors.toList());
    }

    @Override
    public Problem getProblem(Integer problemId) {
        return cacheService.getProblem(problemId);
    }

    @Override
    public List<ProblemCatalog> getProblemCatalogs() {
        return cacheService.loadProblemCatalogs();
    }

    @Override
    public ProblemCatalog getProblemCatalog(Integer catalogId) {
        return cacheService.getProblemCatalog(catalogId);
    }

    @Override
    public ProblemSubCatalog getProblemSubCatalog(Integer subCatalogId) {
        return cacheService.getProblemSubCatalog(subCatalogId);
    }

    @Override
    public void gradeProblem(Integer problem, String openId, Integer profileId, List<ProblemScore> problemScores) {
        problemScores.forEach(item -> {
            item.setOpenid(openId);
            item.setProfileId(profileId);
            item.setProblemId(problem);
        });
        problemScoreDao.gradeProblem(problemScores);
    }

    @Override
    public boolean hasProblemScore(Integer profileId, Integer problemId) {
        return problemScoreDao.userProblemScoreCount(profileId, problemId) > 0;
    }

    @Override
    public Integer insertProblemExtension(ProblemExtension problemExtension) {
        ProblemExtension extensionTarget = new ProblemExtension();
        BeanUtils.copyProperties(problemExtension, extensionTarget);

        Integer problemId = problemExtension.getProblemId();
        Problem cacheProblem = cacheService.getProblem(problemId);
        if (cacheProblem == null) {
            return -1;
        }
        extensionTarget.setProblem(cacheProblem.getProblem());
        if (cacheProblem.getCatalogId() != null) {
            String problemCatalogName = cacheService.getProblemCatalog(cacheProblem.getCatalogId()).getName();
            if (problemCatalogName != null) {
                extensionTarget.setCatalog(problemCatalogName);
            }
        }
        if (cacheProblem.getSubCatalogId() != null) {
            String problemSubCatalogName = cacheService.getProblemSubCatalog(cacheProblem.getSubCatalogId()).getName();
            if (problemSubCatalogName != null) {
                extensionTarget.setSubCatalog(problemSubCatalogName);
            }
        }
        Integer result1 = problemExtensionDao.insert(extensionTarget);
        Integer result2 = problemDao.insertRecommendationById(problemId, problemExtension.getRecommendation());
        return result1 < result2 ? result1 : result2;
    }

    @Override
    public Integer insertProblemActivity(ProblemActivity problemActivity) {
        return problemActivityDao.insertProblemActivity(problemActivity);
    }

    @Override
    public ProblemExtension loadProblemExtensionByProblemId(Integer problemId) {
        return problemExtensionDao.loadByProblemId(problemId);
    }

    @Override
    public List<ProblemActivity> loadProblemActivitiesByProblemId(Integer problemId) {
        return problemActivityDao.loadProblemActivitiesByProblemId(problemId);
    }

    @Override
    public Pair<String, List<EssenceCard>> loadProblemCards(Integer planId) {
        // 根据 planId 获取 improvement 中的 problemId
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Integer problemId = plan.getProblemId();
        Integer profileId = plan.getProfileId(); // 用来获取用户新的 profileId
        Profile profile = profileDao.load(Profile.class, profileId);
        // 绘图数据
        QRResponse response = qrCodeService.generateTemporaryQRCode("freeLimit_" + profileId, null);
        BufferedImage qrImage = null;
        try {
            qrImage = ImageIO.read(qrCodeService.showQRCode(response.getTicket()));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        // 获取用户头像图片
        String headImgUrl = profile.getHeadimgurl();
        if ("/0".equals(headImgUrl.substring(headImgUrl.length() - 2))) {
            headImgUrl = headImgUrl.substring(0, headImgUrl.length() - 2) + "/64";
        }
        BufferedImage headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        // 如果用户头像过期，则拉取实时新头像
        if (headImg == null) {
            Profile realProfile = accountService.getProfile(profile.getOpenid(), true);
            headImgUrl = realProfile.getHeadimgurl();
            headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        }
        Problem problem = cacheService.getProblem(problemId);
        // 获取 essenceCard 所有与当前小课相关的数据
        List<EssenceCard> essenceCards = essenceCardDao.loadEssenceCards(problemId);
        Map<Integer, EssenceCard> essenceCardMap = Maps.newHashMap();
        for (EssenceCard essenceCard : essenceCards) {
            essenceCardMap.put(essenceCard.getChapterId(), essenceCard);
        }
        Integer completeSeries = plan.getCompleteSeries();
        List<Chapter> chapters = problem.getChapterList();
        // 目标 essenceList
        List<EssenceCard> cards = Lists.newArrayList();
        Integer tempChapter = 0;
        for (Chapter chapter : chapters) {
            Integer chapterId = chapter.getChapter();
            EssenceCard essenceCard = new EssenceCard();
            essenceCard.setProblemId(problemId);
            essenceCard.setChapterId(chapterId);
            if (essenceCardMap.get(chapterId) != null) {
                essenceCard.setEssenceContent(essenceCardMap.get(chapterId).getEssenceContent());
            }
            essenceCard.setChapterNo("第" + NumberToHanZi.formatInteger(chapterId) + "章");
            if (chapterId == chapters.size()) {
                essenceCard.setChapter("小课知识清单");
                essenceCard.setEssenceImgBase(getEssenceCardImg(profileId, loadBufferedImageByChapterId(6), qrImage, headImg, essenceCardMap.get(chapterId)));
            } else {
                essenceCard.setChapter(chapter.getName());
                essenceCard.setEssenceImgBase(getEssenceCardImg(profileId, loadBufferedImageByChapterId(chapterId), qrImage, headImg, essenceCardMap.get(chapterId)));
            }
            cards.add(essenceCard);
            // 计算已完成的章节号
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                if (section.getSeries().equals(completeSeries)) {
                    tempChapter = section.getChapter();
                }
            }
        }
        Integer completedChapter = 0;
        for (Chapter chapter : chapters) {
            if (chapter.getChapter().equals(tempChapter)) {
                List<Section> sections = chapter.getSections();
                Long resultCnt = sections.stream().filter(section -> section.getSeries() > completeSeries).count();
                completedChapter = resultCnt > 0 ? chapter.getChapter() - 1 : chapter.getChapter();
            }
        }
        for (EssenceCard essenceCard : cards) {
            essenceCard.setCompleted(essenceCard.getChapterId() <= completedChapter);
        }
        return new MutablePair<>(problem.getProblem(), cards);
    }

    /**
     * @param profileId
     * @param targetImage 背景图片
     * @param qrImage     二维码图片
     * @param headImg     头像图片
     */
    @Override
    public String getEssenceCardImg(Integer profileId, BufferedImage targetImage, BufferedImage qrImage, BufferedImage headImg, EssenceCard essenceCard) {
        Profile profile = profileDao.load(Profile.class, profileId);
        // TargetImage
        targetImage = ImageUtils.scaleByPercentage(targetImage, 750, 1334);
        // QrImage
        qrImage = ImageUtils.scaleByPercentage(qrImage, 220, 220);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 34, 1087);
        // HeadImage
        headImg = ImageUtils.scaleByPercentage(headImg, 102, 102);
        headImg = ImageUtils.convertCircular(headImg);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 497, 1147);
        // NickName
        targetImage = ImageUtils.writeText(targetImage, 278, 1230, profile.getNickname() + "邀请你，",
                new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
        targetImage = ImageUtils.writeText(targetImage, 278, 1265, "成为" + essenceCard.getTag() + "力爆表的人",
                new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
        // 课程标题
        String[] titleArr = essenceCard.getEssenceTitle().split("\\|");
        targetImage = ImageUtils.writeText(targetImage, 380, 320, titleArr[0],
                new Font("Helvetica", Font.PLAIN, 60), new Color(51, 51, 51));
        targetImage = ImageUtils.writeText(targetImage, 264, 420, titleArr[1],
                new Font("Helvetica", Font.PLAIN, 60), new Color(255, 255, 255));
        // 渲染课程精华卡片文本
        String[] contentArr = essenceCard.getEssenceContent().split("\\|");
        targetImage = generateThreeContent(targetImage, contentArr, contentArr.length);
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(targetImage, "png", outputStream);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return "data:image/png;base64," + encoder.encode(outputStream.toByteArray());
    }

    @Override
    public BufferedImage loadBufferedImageByChapterId(Integer chapterId) {
        chapterId = chapterId % bufferedImages.size() == 0 ? bufferedImages.size() : chapterId % bufferedImages.size();
        return bufferedImages.get(chapterId - 1);
    }

    private BufferedImage generateThreeContent(BufferedImage targetImage, String[] contentArr, Integer paraSize) {
        int splitNum = 12;
        // 精华内容
        String content1 = contentArr[0];
        int content1Size = content1.length() / splitNum == 0 ? 1 : content1.length() / splitNum + 1;
        for (int i = 0; i < content1Size; i++) {
            String writeText;
            if (i == 0) {
                writeText = "-  " + (content1.length() > splitNum ? content1.substring(0, splitNum - 1) : content1);
            } else if (i == content1Size - 1) {
                writeText = content1.substring(splitNum * i - 1);
            } else {
                writeText = content1.substring(i * splitNum - 1, (i + 1) * splitNum - 1);
            }
            targetImage = ImageUtils.writeText(targetImage, 404, 520 + i * 35, writeText,
                    new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
        }
        String content2 = contentArr[1];
        int content2Size = content2.length() / splitNum == 0 ? 1 : content2.length() / splitNum + 1;
        for (int i = 0; i < content2Size; i++) {
            String writeText;
            if (i == 0) {
                writeText = "-  " + (content2.length() > splitNum ? content2.substring(0, splitNum - 1) : content2);
            } else if (i == content2Size - 1) {
                writeText = content2.substring(splitNum * i - 1);
            } else {
                writeText = content2.substring(i * splitNum - 1, (i + 1) * splitNum - 1);
            }
            targetImage = ImageUtils.writeText(targetImage, 404, 680 + i * 35, writeText,
                    new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
        }
        String content3 = contentArr[2];
        int content3Size = content3.length() / splitNum == 0 ? 1 : content3.length() / splitNum + 1;
        for (int i = 0; i < content3Size; i++) {
            String writeText;
            if (i == 0) {
                writeText = "-  " + (content3.length() > splitNum ? content3.substring(0, splitNum - 1) : content3);
            } else if (i == content3Size - 1) {
                writeText = content3.substring(splitNum * i - 1);
            } else {
                writeText = content3.substring(i * splitNum - 1, (i + 1) * splitNum - 1);
            }
            targetImage = ImageUtils.writeText(targetImage, 404, 840 + i * 35, writeText,
                    new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
        }
        if (paraSize == 4) {
            String content4 = contentArr[3];
            int content4Size = content4.length() / splitNum == 0 ? 1 : content4.length() / splitNum + 1;
            for (int i = 0; i < content4Size; i++) {
                String writeText;
                if (i == 0) {
                    writeText = "-  " + (content4.length() > splitNum ? content4.substring(0, splitNum - 1) : content4);
                } else if (i == content4Size - 1) {
                    writeText = content4.substring(splitNum * i - 1);
                } else {
                    writeText = content4.substring(i * splitNum - 1, (i + 1) * splitNum - 1);
                }
                targetImage = ImageUtils.writeText(targetImage, 404, 1000 + i * 35, writeText,
                        new Font("Helvetica", Font.PLAIN, 24), new Color(51, 51, 51));
            }
        }
        return targetImage;
    }


}
