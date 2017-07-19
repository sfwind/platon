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
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
        System.out.println("point1 " + DateUtils.parseDateTimeToString(new Date()));
        // 根据 planId 获取 improvement 中的 problemId
        ImprovementPlan plan = improvementPlanDao.load(ImprovementPlan.class, planId);
        Integer problemId = plan.getProblemId();
        Integer profileId = plan.getProfileId(); // 用来获取用户新的 profileId
        Problem problem = cacheService.getProblem(problemId);
        // 获取 essenceCard 所有与当前小课相关的数据
        List<EssenceCard> essenceCards = essenceCardDao.loadEssenceCards(problemId);
        System.out.println("point2 " + DateUtils.parseDateTimeToString(new Date()));
        Map<Integer, EssenceCard> essenceCardMap = Maps.newHashMap();
        for (EssenceCard essenceCard : essenceCards) {
            essenceCardMap.put(essenceCard.getChapterId(), essenceCard);
        }
        Integer completeSeries = plan.getCompleteSeries();
        List<Chapter> chapters = problem.getChapterList();
        // 目标 essenceList
        List<EssenceCard> cards = Lists.newArrayList();
        Integer tempChapter = 0;
        System.out.println("point3 " + DateUtils.parseDateTimeToString(new Date()));
        for (Chapter chapter : chapters) {
            Integer chapterId = chapter.getChapter();
            EssenceCard essenceCard = new EssenceCard();
            essenceCard.setProblemId(problemId);
            essenceCard.setChapterId(chapterId);
            if (essenceCardMap.get(chapterId) != null) {
                essenceCard.setEssenceContent(essenceCardMap.get(chapterId).getEssenceContent());
            }
            essenceCard.setChapter(chapter.getName());
            System.out.println("paintImage1 " + DateUtils.parseDateTimeToString(new Date()));
            // essenceCard.setEssenceImgBase(getEssenceCardImg(essenceCard, profileId));
            System.out.println("paintImage2 " + DateUtils.parseDateTimeToString(new Date()));
            cards.add(essenceCard);
            // 计算已完成的章节号
            List<Section> sections = chapter.getSections();
            for (Section section : sections) {
                if (section.getSeries().equals(completeSeries)) {
                    tempChapter = section.getChapter();
                }
            }
        }
        System.out.println("point4 " + DateUtils.parseDateTimeToString(new Date()));
        Integer completedChapter = 0;
        for (Chapter chapter : chapters) {
            if (chapter.getChapter().equals(tempChapter)) {
                List<Section> sections = chapter.getSections();
                Long resultCnt = sections.stream().filter(section -> section.getSeries() > completeSeries).count();
                completedChapter = resultCnt > 0 ? chapter.getChapter() - 1 : chapter.getChapter();
            }
        }
        System.out.println("point5 " + DateUtils.parseDateTimeToString(new Date()));
        for (EssenceCard essenceCard : cards) {
            essenceCard.setCompleted(essenceCard.getChapterId() <= completedChapter);
        }
        System.out.println("point6 " + DateUtils.parseDateTimeToString(new Date()));
        // 最后添加章总结数据
        EssenceCard lastCard = new EssenceCard();
        EssenceCard tempEssenceCard = essenceCardMap.get(0);
        lastCard.setProblemId(problemId);
        lastCard.setChapterId(0);
        lastCard.setCompleted(completedChapter.equals(chapters.size()));
        if (essenceCardMap.get(0) != null) {
            lastCard.setEssenceContent(tempEssenceCard.getEssenceContent());
            // lastCard.setEssenceImgBase(getEssenceCardImg(tempEssenceCard, profileId));
        }
        lastCard.setChapter("章总结");
        cards.add(lastCard);
        return new MutablePair<>(problem.getProblem(), cards);
    }

    private String getEssenceCardImg(EssenceCard essenceCard, Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        System.out.println("imageInner1 " + DateUtils.parseDateTimeToString(new Date()));
        Integer chapterId = essenceCard.getChapterId();

        // 获取绘图背景图片
        JSONObject backImgUrlJson = JSONObject.parseObject(ConfigUtils.getEssenceCardBackImgs());
        String backImgUrl = backImgUrlJson.getString(chapterId.toString());
        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl(backImgUrl);
        System.out.println("imageInner2 " + DateUtils.parseDateTimeToString(new Date()));

        // 获取二维码图片
        QRResponse response = qrCodeService.generateTemporaryQRCode("freeLimit" + profileId, null);
        BufferedImage qrImage = null;
        try {
            qrImage = ImageIO.read(qrCodeService.showQRCode(response.getTicket()));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        System.out.println("imageInner3 " + DateUtils.parseDateTimeToString(new Date()));

        // 获取用户头像图片
        String headImgUrl = profile.getHeadimgurl();
        if (headImgUrl == null) {
            // 如果用户头像为空，则拉取实时新头像
            Profile realProfile = accountService.getProfile(profile.getOpenid(), true);
            headImgUrl = realProfile.getHeadimgurl();
        }
        BufferedImage headImg = ImageUtils.getBufferedImageByUrl(headImgUrl);
        System.out.println("imageInner4 " + DateUtils.parseDateTimeToString(new Date()));

        qrImage = ImageUtils.scaleByPercentage(qrImage, 40, 40);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 150, 150);
        targetImage = ImageUtils.scaleByPercentage(targetImage, 338, 600);
        headImg = ImageUtils.convertCircular(headImg);
        headImg = ImageUtils.scaleByPercentage(headImg, 60, 60);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 10, 10);
        targetImage = ImageUtils.writeText(targetImage, 60, 10, profile.getNickname(),
                new Font("微软雅黑", Font.BOLD, 18), new Color(102, 102, 102));
        targetImage = ImageUtils.writeText(targetImage, 40, 50, "理清问题需求，澄清偏差",
                new Font("微软雅黑", Font.BOLD, 24), new Color(51, 51, 51));
        System.out.println("imageInner5 " + DateUtils.parseDateTimeToString(new Date()));
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(targetImage, "png", outputStream);
            // String imagePath = "/Users/xfduan/Pictures/a.png";
            // ImageIO.write(targetImage, imagePath.substring(imagePath.lastIndexOf(".") + 1), new File(imagePath));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        System.out.println("imageInner6 " + DateUtils.parseDateTimeToString(new Date()));
        BASE64Encoder encoder = new BASE64Encoder();
        return "data:image/png;base64," + encoder.encode(outputStream.toByteArray());
    }

}
