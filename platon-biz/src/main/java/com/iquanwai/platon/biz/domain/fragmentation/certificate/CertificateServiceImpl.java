package com.iquanwai.platon.biz.domain.fragmentation.certificate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.CouponDao;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointManager;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.*;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import sun.misc.BASE64Decoder;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/8/29.
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RiseMemberService riseMemberService;
    @Autowired
    private PointManager pointManager;
    @Autowired
    private ProblemScheduleManager problemScheduleManager;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private FullAttendanceRewardDao fullAttendanceRewardDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RiseCertificateDao riseCertificateDao;

    //优秀学员,优秀团队奖励积分
    private static final int PRIZE_POINT = 200;
    //优秀班长优惠券
    private static final int PRIZE_COUPON_CLASS_LEADER = 200;
    //优秀组长优惠券
    private static final int PRIZE_COUPON_GROUP_LEADER = 100;
    private static final String FULL_ATTENDANCE_COUPON_CATEGORY = "ELITE_RISE_MEMBER";
    private static final String FULL_ATTENDANCE_COUPON_DESCRIPTION = "训练营全勤奖";

    // 正常证书背景
    private static final String RISE_CERTIFICATE_BG_ORDINARY = "https://static.iqycamp.com/images/certificate_normal_bg_5.jpg?imageslim";
    // 优秀证书背景
    private static final String RISE_CERTIFICATE_BG_EXCELLENT = "https://static.iqycamp.com/images/certificate_bg_5.jpg?imageslim";

    private Logger logger = LoggerFactory.getLogger(getClass());
    private static BufferedImage ordinaryImage = null;
    private static BufferedImage excellentImage = null;

    @PostConstruct
    public void init() {

        ordinaryImage = ImageUtils.getBufferedImageByUrl(RISE_CERTIFICATE_BG_ORDINARY);
        excellentImage = ImageUtils.getBufferedImageByUrl(RISE_CERTIFICATE_BG_EXCELLENT);

    }

    @Override
    public RiseCertificate getCertificate(String certificateNo) {
        RiseCertificate riseCertificate = riseCertificateDao.loadByCertificateNo(certificateNo);
        Profile profile = accountService.getProfile(riseCertificate.getProfileId());
        if (profile.getRealName() != null && riseCertificate.getImageUrl() == null) {
            Pair<Boolean, String> pair = drawRiseCertificate(riseCertificate, true);
            if (pair.getLeft()) {
                String imageUrl = ConfigUtils.getPicturePrefix() + pair.getRight();
                riseCertificate.setImageUrl(imageUrl);
                riseCertificateDao.updateImageUrl(riseCertificate.getId(), imageUrl);
            }
        }
        buildDetail(riseCertificate);
        //删除profileId
        riseCertificate.setProfileId(null);
        return riseCertificate;
    }

    @Override
    public RiseCertificate getNextCertificate(Integer certificateId) {
        return riseCertificateDao.loadNextCertificateNoById(certificateId);
    }

    @Override
    public boolean convertCertificateBase64(String base64Str, String imgPath) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(base64Str);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {// 调整异常数据
                    bytes[i] += 256;
                }
            }
            OutputStream out = new FileOutputStream(imgPath);
            out.write(bytes);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public int updateDownloadTime(String certificateNo) {
        return riseCertificateDao.updateDownloadTime(certificateNo);
    }

    @Override
    public void generateCertificate(Integer year, Integer month) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.loadRiseClassMembersByYearMonth(year, month);
        List<Integer> riseClassMemberProfileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());
        logger.info("riseClassMember 人员获取结束: {}", riseClassMemberProfileIds.size());
        List<ImprovementPlan> improvementPlans = Lists.newArrayList();
        riseClassMemberProfileIds.forEach(classMemberProfileId -> {
            logger.info("开始获取证书生成人员信息profileId: {}", classMemberProfileId);
            Integer majorProblemId = problemScheduleManager.getLearningMajorProblemId(classMemberProfileId);
            if (majorProblemId != null) {
                ImprovementPlan selfPlan = improvementPlanDao.loadPlanByProblemId(classMemberProfileId, majorProblemId);
                if (selfPlan != null) {
                    improvementPlans.add(selfPlan);
                }
            }
        });
        logger.info("improvementPlan 获取结束");
        Map<Integer, ImprovementPlan> improvementPlanMap = improvementPlans.stream().collect(Collectors.toMap(ImprovementPlan::getId, improvementPlan -> improvementPlan));
        List<Integer> riseClassMemberPlanIds = improvementPlans.stream().map(ImprovementPlan::getId).collect(Collectors.toList());

        List<Integer> certificateNoSequence = Lists.newArrayList();
        certificateNoSequence.add(1);

        riseClassMemberPlanIds.forEach(planId -> {
            logger.info("开始处理证书学习计划：{}", planId);
            try {
                boolean generateCertificateTag = true;

                List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
                if (practicePlans.size() != 0) {
                    // 应该完成的知识点、选择题中未完成的题数
                    Long unCompleteNecessaryCountLong = practicePlans.stream()
                            .filter(practicePlan ->
                                    PracticePlan.WARM_UP == practicePlan.getType() || PracticePlan.WARM_UP_REVIEW == practicePlan.getType()
                                            || PracticePlan.KNOWLEDGE == practicePlan.getType() || PracticePlan.KNOWLEDGE_REVIEW == practicePlan.getType())
                            .filter(practicePlan ->
                                    practicePlan.getStatus() == 0)
                            .count();
                    if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                        // 必须完成知识点、选择题的题数大于 0，不发结课证书
                        generateCertificateTag = false;
                    } else {
                        // 所有必须完成的知识点、选择题都已经完成
                        // 对应用题完成情况进行复查
                        List<PracticePlan> applicationPracticePlans = practicePlans.stream()
                                .filter(practicePlan -> PracticePlan.APPLICATION_BASE == practicePlan.getType()
                                        || PracticePlan.APPLICATION_UPGRADED == practicePlan.getType())
                                .collect(Collectors.toList());
                        List<Integer> applicationIds = applicationPracticePlans.stream().map(PracticePlan::getPracticeId).map(Integer::parseInt).collect(Collectors.toList());
                        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadApplicationSubmitsByApplicationIds(applicationIds, planId);
                        Map<Integer, ApplicationSubmit> applicationSubmitMap = applicationSubmits.stream().collect(Collectors.toMap(ApplicationSubmit::getApplicationId, applicationSubmit -> applicationSubmit));

                        // 根据 planId 和 practicePlan 中的 PracticeId 来获取应用题完成数据
                        Set<Integer> seriesSet = applicationPracticePlans.stream().map(PracticePlan::getSeries).collect(Collectors.toSet());

                        // Plan 中每节至少优质完成一道应用题的小节数
                        Long planApplicationCheckLong = seriesSet.stream().filter(series -> {
                            List<Integer> practiceIds = applicationPracticePlans.stream()
                                    .filter(practicePlan -> practicePlan.getSeries().equals(series))
                                    .map(PracticePlan::getPracticeId)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                            // 每个 Series 中至少存在一节内容完成
                            Long seriesApplicationCheckLong = practiceIds.stream().filter(practiceId -> {
                                ApplicationSubmit applicationSubmit = applicationSubmitMap.get(practiceId);
                                return applicationSubmit != null && (applicationSubmit.getContent().contains("img") || applicationSubmit.getLength() > 10);
                            }).count();
                            return seriesApplicationCheckLong.intValue() > 0;
                        }).count();

                        if (planApplicationCheckLong.intValue() != seriesSet.size()) {
                            generateCertificateTag = false;
                        }
                    }

                    if (generateCertificateTag) {
                        ImprovementPlan improvementPlan = improvementPlanMap.get(planId);
                        Integer profileId = improvementPlan.getProfileId();

                        RiseClassMember riseClassMember = riseClassMemberDao.loadSingleByProfileId(year, month, profileId);
                        if (riseClassMember != null) {
                            RiseCertificate existRiseCertificate = riseCertificateDao.loadSingleGraduateByProfileId(year, month, profileId);
                            if (existRiseCertificate == null) {
                                Problem problem = cacheService.getProblem(improvementPlan.getProblemId());
                                // 如果允许生成训练营结业证书，则生成证书
                                RiseCertificate riseCertificate = new RiseCertificate();
                                riseCertificate.setProfileId(profileId);
                                riseCertificate.setType(Constants.CERTIFICATE.TYPE.ORDINARY);
                                StringBuilder certificateNoBuilder = new StringBuilder("IQW");
                                certificateNoBuilder.append(String.format("%02d", Constants.CERTIFICATE.TYPE.ORDINARY));
                                certificateNoBuilder.append(riseClassMember.getMemberId());
                                certificateNoBuilder.append(String.format("%02d", month));
                                Integer noSequence = certificateNoSequence.get(0);
                                certificateNoSequence.clear();
                                certificateNoSequence.add(noSequence + 1);
                                certificateNoBuilder.append(String.format("%03d", noSequence));
                                certificateNoBuilder.append(String.format("%02d", RandomUtils.nextInt(0, 100)));
                                riseCertificate.setCertificateNo(certificateNoBuilder.toString());
                                riseCertificate.setYear(year);
                                riseCertificate.setMonth(month);
                                riseCertificate.setGroupNo(Integer.parseInt(riseClassMember.getGroupId()));
                                riseCertificate.setProblemName(problem.getProblem());
                                int generateId = riseCertificateDao.insert(riseCertificate);

                                if (generateId > 0) {
                                    Pair<Boolean, String> pair = drawRiseCertificate(riseCertificate, true);
                                    if (pair.getLeft()) {
                                        riseCertificateDao.updateImageUrl(generateId, ConfigUtils.getPicturePrefix() + pair.getRight());
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("证书处理异常 planId: {}", planId);
                logger.info(e.getLocalizedMessage(), e);
            }
        });
        logger.info("证书生成完毕，停止时间：{}", DateUtils.parseDateTimeToString(new Date()));
    }

    @Override
    public void uploadCertificateToQiNiu(Boolean isOnline) {
        List<RiseCertificate> riseCertificates = riseCertificateDao.loadUnUploadImageCertificates();
        riseCertificates.forEach(riseCertificate -> {
            Pair<Boolean, String> pair = drawRiseCertificate(riseCertificate, isOnline);
            if (pair.getLeft()) {
                // 上传成功，更新 imageUrl
                riseCertificateDao.updateImageUrl(riseCertificate.getId(), ConfigUtils.getPicturePrefix() + pair.getRight());
            }
        });
        logger.info("证书生成完毕，停止时间：{}", DateUtils.parseDateTimeToString(new Date()));
    }

    @Override
    public void generateFullAttendanceCoupon(Integer year, Integer month) {
        List<RiseClassMember> riseClassMembers = riseClassMemberDao.loadRiseClassMembersByYearMonth(year, month);
        List<Integer> riseClassMemberProfileIds = riseClassMembers.stream().map(RiseClassMember::getProfileId).collect(Collectors.toList());

        logger.info("riseClassMember 人员获取结束: {}", riseClassMemberProfileIds.size());
        List<ImprovementPlan> improvementPlans = Lists.newArrayList();
        //根据profileId获得主修课的improvementPlan
        riseClassMemberProfileIds.forEach(classMemberProfileId -> {
            logger.info("开始获取优惠券生成人员信息profileId: {}", classMemberProfileId);
            Integer problemId = problemScheduleManager.getLearningMajorProblemId(classMemberProfileId);
            if (problemId != null) {
                ImprovementPlan selfPlan = improvementPlanDao.loadPlanByProblemId(classMemberProfileId, problemId);
                if (selfPlan != null) {
                    improvementPlans.add(selfPlan);
                }
            }
        });
        logger.info("improvementPlan 获取结束");

        Map<Integer, ImprovementPlan> improvementPlanMap = improvementPlans.stream().collect(Collectors.toMap(ImprovementPlan::getId, improvementPlan -> improvementPlan));
        List<Integer> riseClassMemberPlanIds = improvementPlans.stream().map(ImprovementPlan::getId).collect(Collectors.toList());

        riseClassMemberPlanIds.forEach(planId -> {

            try {
                boolean generateFullAttendanceCoupon = true;

                List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
                if (practicePlans.size() != 0) {
                    // 完成所有练习
                    Long unCompleteNecessaryCountLong = practicePlans.stream()
                            .filter(practicePlan -> PracticePlan.CHALLENGE != practicePlan.getType())
                            .filter(practicePlan -> PracticePlan.STATUS.UNCOMPLETED == practicePlan.getStatus())
                            .count();
                    if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                        // 如果存在有没有完成的题数，则不予发送优惠券
                        generateFullAttendanceCoupon = false;
                    } else {
                        // 完成所有练习之后，对应用题完成情况进行复查
                        List<PracticePlan> applicationPracticePlans = practicePlans.stream()
                                .filter(practicePlan -> PracticePlan.APPLICATION_BASE == practicePlan.getType()
                                        || PracticePlan.APPLICATION_UPGRADED == practicePlan.getType())
                                .collect(Collectors.toList());
                        List<Integer> applicationIds = applicationPracticePlans.stream().map(PracticePlan::getPracticeId).map(Integer::parseInt).collect(Collectors.toList());
                        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadApplicationSubmitsByApplicationIds(applicationIds, planId);
                        Map<Integer, ApplicationSubmit> applicationSubmitMap = applicationSubmits.stream().collect(Collectors.toMap(ApplicationSubmit::getApplicationId, applicationSubmit -> applicationSubmit));

                        // 根据 planId 和 practicePlan 中的 PracticeId 来获取应用题完成数据
                        Set<Integer> seriesSet = applicationPracticePlans.stream().map(PracticePlan::getSeries).collect(Collectors.toSet());

                        // Plan 中每节都是优质完成应用题的小节数
                        Long planApplicationCheckLong = seriesSet.stream().filter(series -> {
                            List<Integer> practiceIds = applicationPracticePlans.stream()
                                    .filter(practicePlan -> practicePlan.getSeries().equals(series))
                                    .map(PracticePlan::getPracticeId)
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                            // 每个 Series 中每一节都是优质完成
                            // 返回不合格完成应用题数，全勤奖去除字数限制
                            Long seriesApplicationCheckLong = practiceIds.stream().filter(practiceId -> {
                                ApplicationSubmit applicationSubmit = applicationSubmitMap.get(practiceId);
                                return applicationSubmit == null;
                            }).count();
                            return seriesApplicationCheckLong.intValue() == 0; // 不合格数为0的话，说明当前小节全部完成，参与计数
                        }).count();

                        if (planApplicationCheckLong.intValue() != seriesSet.size()) {
                            generateFullAttendanceCoupon = false;
                        }
                    }

                    if (generateFullAttendanceCoupon) {
                        ImprovementPlan improvementPlan = improvementPlanMap.get(planId);
                        Integer profileId = improvementPlan.getProfileId();
                        FullAttendanceReward existFullAttendanceReward = fullAttendanceRewardDao.loadSingleByProfileId(year, month, profileId);
                        if (existFullAttendanceReward == null) {
                            // 如果允许生成训练营结业证书，则生成证书
                            FullAttendanceReward fullAttendanceReward = new FullAttendanceReward();
                            fullAttendanceReward.setProfileId(profileId);
                            fullAttendanceReward.setYear(year);
                            fullAttendanceReward.setMonth(month);
                            fullAttendanceReward.setAmount(199.00);
                            fullAttendanceRewardDao.insert(fullAttendanceReward);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
        logger.info("全勤奖优惠券待发人员生成完毕，停止时间：{}", DateUtils.parseDateTimeToString(new Date()));
    }

    /**
     * 判断单个用户是否能够获得全勤券
     */
    @Override
    public void generateSingleFullAttendanceCoupon(Integer practicePlanId) {
        int planId;
        int profileId;
        Integer problemId;
        PracticePlan plan = practicePlanDao.load(PracticePlan.class, practicePlanId);
        if (plan != null) {
            planId = plan.getPlanId();
            ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
            if (improvementPlan != null) {
                profileId = improvementPlan.getProfileId();
                //判断是否是助教
                if (userRoleDao.getAssist(profileId) != null) {
                    return;
                }
                problemId = improvementPlan.getProblemId();
                Integer learningProblemId = problemScheduleManager.getLearningMajorProblemId(profileId);
                logger.info("当前主修的problemId为：" + learningProblemId);
                logger.info("您目前的problemId为：" + problemId);
                //判断是否是当前主修的problemId
                if (problemId.equals(learningProblemId)) {
                    logger.info("当前课程是主修课程");
                    //判断是否应该发送全勤奖
                    boolean isGenerate = true;
                    //获得学习内容完成情况列表
                    List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
                    if (practicePlans.size() != 0) {
                        Long unCompleteNecessaryCountLong = practicePlans.stream()
                                .filter(practicePlan -> PracticePlan.CHALLENGE != practicePlan.getType())
                                .filter(practicePlan -> PracticePlan.STATUS.UNCOMPLETED == practicePlan.getStatus())
                                .count();
                        if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                            //如果存在没有完成的题目，不不予发送优惠券
                            isGenerate = false;
                        }
                        //有发送全勤奖的资格
                        if (isGenerate) {
                            logger.info("进入发送全勤奖流程");
                            int year = ConfigUtils.getLearningYear();
                            int month = ConfigUtils.getLearningMonth();
                            Boolean validElite = riseMemberService.isValidElite(profileId);
                            Boolean validCamp = riseMemberService.isValidCamp(profileId);

                            //判断是否是当月训练营或者商学院用户
                            if (validElite || validCamp) {
                                logger.info("是商学院或者当月训练营用户");
                                FullAttendanceReward existFullAttendanceReward = fullAttendanceRewardDao.loadFullAttendanceRewardByProfileId(year, month, profileId);
                                if (existFullAttendanceReward == null) {
                                    logger.info("开始发送全勤奖");
                                    FullAttendanceReward fullAttendanceReward = new FullAttendanceReward();
                                    fullAttendanceReward.setProfileId(profileId);
                                    fullAttendanceReward.setYear(year);
                                    fullAttendanceReward.setMonth(month);
                                    fullAttendanceReward.setAmount(199.00);
                                    fullAttendanceRewardDao.insert(fullAttendanceReward);
                                    sendSingleFullAttendanceCoupon(year, month, profileId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendCertificate(Integer year, Integer month) {
        List<RiseCertificate> certificateList = riseCertificateDao.loadUnNotifiedByMonthAndYear(year, month);
        certificateList.forEach(riseCertificate -> {
            Integer type = riseCertificate.getType();
            TemplateMessage templateMessage = new TemplateMessage();
            Profile profile = accountService.getProfile(riseCertificate.getProfileId());
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.sendCertificateMsg());
            templateMessage.setUrl(
                    ConfigUtils.domainName() + "/rise/static/customer/certificate?certificateNo="
                            + riseCertificate.getCertificateNo());
            buildGraduateMessage(riseCertificate, type, templateMessage, profile);
            //发送结课消息
            templateMessageService.sendMessage(templateMessage);

            templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());
            templateMessage.setUrl("https://shimo.im/doc/3kL94FYajYgls0Zx?r=GQ373Y/");
            //发送优惠券信息
            sendCouponMessage(riseCertificate, type, templateMessage, profile);

            riseCertificateDao.notify(riseCertificate.getId());
        });
    }

    @Override
    public void sendFullAttendanceCoupon(Integer year, Integer month) {
        List<FullAttendanceReward> fullAttendanceRewards = fullAttendanceRewardDao.loadUnNotifiedByYearMonth(year, month);
        fullAttendanceRewards.forEach(fullAttendanceReward -> {
            Integer profileId = fullAttendanceReward.getProfileId();
            Profile profile = accountService.getProfile(profileId);

            // 添加 coupon 数据记录
            Coupon coupon = new Coupon();
            coupon.setProfileId(profileId);
            coupon.setAmount(fullAttendanceReward.getAmount().intValue());
            coupon.setUsed(0);
            buildCouponExpireDate(coupon, profile);
            coupon.setCategory(FULL_ATTENDANCE_COUPON_CATEGORY);
            coupon.setDescription(FULL_ATTENDANCE_COUPON_DESCRIPTION);
            int couponInsertResult = couponDao.insertCoupon(coupon);


            int notifiedUpdateResult = -1;
            // 成功插入优惠券更新表中字段值
            if (couponInsertResult > 0) {
                notifiedUpdateResult = fullAttendanceRewardDao.updateNotify(fullAttendanceReward.getId(), 1);
            }

            // 发送模板消息
            if (notifiedUpdateResult > 0) {
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());
                templateMessage.setTouser(profile.getOpenid());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);
                data.put("first", new TemplateMessage.Keyword("价值199元的“全勤奖学金”已经放入您的账户！\n"));
                data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToFormat6(new Date())));
                data.put("keyword2", new TemplateMessage.Keyword("奖学金（优惠券）"));
                data.put("keyword3", new TemplateMessage.Keyword("价值199元"));

                templateMessageService.sendMessage(templateMessage);
            }
        });
        logger.info("全勤奖消息通知发送完毕，时间：{}", new Date());
    }

    /**
     * 发送该用户对应的全勤优惠券
     */
    private void sendSingleFullAttendanceCoupon(int year, int month, int profileId) {
        FullAttendanceReward fullAttendanceReward = fullAttendanceRewardDao.loadUnNotifiedByYearMonthProfileId(year, month, profileId);
        Profile profile = accountService.getProfile(profileId);

        // 添加 coupon 数据记录
        int couponInsertResult;
        Coupon coupon = new Coupon();
        coupon.setProfileId(profileId);
        coupon.setAmount(fullAttendanceReward.getAmount().intValue());
        coupon.setUsed(0);
        buildCouponExpireDate(coupon, profile);
        coupon.setCategory(FULL_ATTENDANCE_COUPON_CATEGORY);
        coupon.setDescription(FULL_ATTENDANCE_COUPON_DESCRIPTION);
        couponInsertResult = couponDao.insertCoupon(coupon);

        int notifiedUpdateResult = -1;
        // 成功插入优惠券更新表中字段值
        if (couponInsertResult > 0) {
            notifiedUpdateResult = fullAttendanceRewardDao.updateNotify(fullAttendanceReward.getId(), 1);
        }

        // 发送模板消息
        if (notifiedUpdateResult > 0) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.getAccountChangeMsg());
            templateMessage.setTouser(profile.getOpenid());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            templateMessage.setData(data);
            data.put("first", new TemplateMessage.Keyword("价值199元的“全勤奖学金”已经放入您的账户！\n"));
            data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToFormat6(new Date())));
            data.put("keyword2", new TemplateMessage.Keyword("奖学金（优惠券）"));
            data.put("keyword3", new TemplateMessage.Keyword("价值199元"));

            templateMessageService.sendMessage(templateMessage);
        }
    }

    @Override
    public List<RiseCertificate> getCertificates(Integer profileId) {
        return riseCertificateDao.loadByProfileId(profileId);
    }

    @Override
    public void sendOfferMsg(Integer year, Integer month) {
        List<RiseCertificate> certificateList = riseCertificateDao.loadGraduates(year, month);

        certificateList.stream().map(RiseCertificate::getProfileId).distinct().forEach(profileId -> {
            Profile profile = accountService.getProfile(profileId);
            //只发非会员用户
            if (profile.getRiseMember() != Constants.RISE_MEMBER.MEMBERSHIP) {
                TemplateMessage templateMessage = new TemplateMessage();
                templateMessage.setTouser(profile.getOpenid());
                templateMessage.setTemplate_id(ConfigUtils.productChangeMsg());
                Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                templateMessage.setData(data);

                data.put("first", new TemplateMessage.Keyword("恭喜您" + month + "月训练营结课，" +
                        "您已获得商学院免申请入学资格！办理入学请点击下方“商学院”按钮。\n"));
                data.put("keyword1", new TemplateMessage.Keyword("圈外同学"));
                data.put("keyword2", new TemplateMessage.Keyword("圈外商学院"));
                data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                data.put("keyword4", new TemplateMessage.Keyword("已获得免申请入学资格"));

                //发送结课消息
                templateMessageService.sendMessage(templateMessage);
            }

        });
    }

    private void sendCouponMessage(RiseCertificate riseCertificate, Integer type, TemplateMessage templateMessage, Profile profile) {
        int amount = -1;
        String description = "";
        switch (type) {
            case Constants.CERTIFICATE.TYPE.CLASS_LEADER:
                amount = PRIZE_COUPON_CLASS_LEADER;
                description = "优秀班长奖学金";
                break;
            case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                amount = PRIZE_COUPON_GROUP_LEADER;
                description = "优秀组长奖学金";
                break;
            default:
                break;
        }
        if (amount == -1) {
            return;
        }

        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        data.put("keyword1", new TemplateMessage.Keyword(
                "您的" + riseCertificate.getMonth() + "月训练营" + amount + "元优惠券奖励已到账"));
        data.put("keyword2", new TemplateMessage.Keyword("点击详情，了解优惠券领取位置及使用方式"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));

        Coupon coupon = new Coupon();
        coupon.setProfileId(profile.getId());
        coupon.setDescription(description);
        coupon.setUsed(0);
        buildCouponExpireDate(coupon, profile);
        coupon.setAmount(amount);
        couponDao.insertCoupon(coupon);

        templateMessageService.sendMessage(templateMessage);
    }

    private void buildGraduateMessage(RiseCertificate riseCertificate, Integer type, TemplateMessage templateMessage, Profile profile) {
        switch (type) {
            case Constants.CERTIFICATE.TYPE.CLASS_LEADER:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月训练营优秀班长\n" +
                            "点击详情，领取优秀班长荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀班长的同学，除了荣誉证书外，还将获得圈外200元优惠券（领取及使用方式请见下条消息）" +
                            "\n\n" +
                            "此外，如果您在本次会员期内，\n" +
                            "累计3次荣膺优秀班长：额外获得圈外周边礼物+圈圈签名书\n" +
                            "累计6次荣膺优秀班长：额外获得优秀班长礼包+圈圈1V1咨询半小时"));
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月训练营优秀组长\n" +
                            "点击详情，领取优秀组长荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀组长的同学，除了荣誉证书外，还将获得圈外100元优惠券（领取及使用方式请见下条消息）" +
                            "\n\n" +
                            "此外，如果您在本次会员期内，\n" +
                            "累计3次荣膺优秀组长：额外获得圈外周边礼物\n" +
                            "累计6次荣膺优秀组长：额外获得优秀组长礼包+圈圈1V1咨询半小时"));
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月训练营优秀学员\n" +
                            "点击详情，领取优秀学员荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀学员的同学，除了荣誉证书外，您还将额外获得200个积分" +
                            "\n\n" +
                            "此外，如果您在本次会员期内，\n" +
                            "累计3次荣膺优秀学员：成为助教资格＋圈外同学奖学金\n" +
                            "累计6次荣膺优秀学员：圈圈1V1半小时咨询"));

                    pointManager.riseCustomerPoint(profile.getId(), PRIZE_POINT);
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);

                    data.put("first", new TemplateMessage.Keyword("恭喜您所在的小组荣膺［圈外同学］" + riseCertificate.getMonth() + "月训练营优秀团队\n" +
                            "点击详情，领取优秀团队荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀团队的小组，除了荣誉证书外，小组内的每位小伙伴还将额外获得200个积分"));

                    pointManager.riseCustomerPoint(profile.getId(), PRIZE_POINT);
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.ORDINARY:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    data.put("first", new TemplateMessage.Keyword("恭喜您完成［圈外同学］" + riseCertificate.getMonth() + "月训练营\n" +
                            "点击详情，领取结课证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n荣誉证书也可以在［商学院/训练营］－［我的］－［我的课程］中查询"));

                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.ASST_COACH:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月训练营优秀助教\n" +
                            "点击详情，领取优秀助教荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));

                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            default:
                logger.error("证书类型{}不存在", type);
                break;
        }
    }

    private void buildDetail(RiseCertificate riseCertificate) {
        Integer type = riseCertificate.getType();
        Profile profile = accountService.getProfile(riseCertificate.getProfileId());
        switch (type) {
            case Constants.CERTIFICATE.TYPE.CLASS_LEADER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中担任班长一职，表现突出，荣膺\"优秀班长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.CLASS_LEADER);
                break;
            case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中担任组长一职，表现优异，荣膺\"优秀组长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.GROUP_LEADER);
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中成绩名列前茅，荣膺\"优秀学员\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_MEMBER);
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                String monthStr = NumberToHanZi.formatInteger(riseCertificate.getMonth());
                String groupNoStr = NumberToHanZi.formatInteger(riseCertificate.getGroupNo());
                riseCertificate.setName(monthStr + "月训练营" + groupNoStr + "组");
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中小组表现优异，荣膺\"优秀小组\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_GROUP);
                break;
            case Constants.CERTIFICATE.TYPE.ORDINARY:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中完成课程学习" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.ORDINARY);
                break;
            case Constants.CERTIFICATE.TYPE.ASST_COACH:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月训练营中表现卓越，荣膺\"优秀助教\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.ASST_COACH);
                break;
            default:
                logger.error("证书类型{}不存在", type);
                break;
        }
    }

    private void buildCouponExpireDate(Coupon coupon, Profile profile) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profile.getId());
        if (riseMember != null) {
            if (Constants.RISE_MEMBER.MEMBERSHIP == profile.getRiseMember()) {
                coupon.setExpiredDate(DateUtils.afterYears(new Date(), 1));
            } else {
                coupon.setExpiredDate(DateUtils.afterMonths(riseMember.getExpireDate(), 1));
            }
        }
    }

    /**
     * 将证书上传至七牛云
     * @return 是否上传成功，上传文件名称
     */
    private Pair<Boolean, String> drawRiseCertificate(RiseCertificate riseCertificate, Boolean isOnline) {
        Assert.notNull(riseCertificate, "证件信息不能为空");
        logger.info("正在生成证书：{}", riseCertificate.getCertificateNo());

        Profile profile;
        if (isOnline) {
            // 证书数据准备
            profile = accountService.getProfile(riseCertificate.getProfileId());
            if (profile == null || profile.getRealName() == null) {
                // 没有填写真实姓名
                return new MutablePair<>(false, null);
            }
        } else {
            profile = new Profile();
            if (riseCertificate.getRealName() == null) {
                return new MutablePair<>(false, null);
            }
            profile.setRealName(riseCertificate.getRealName());
        }

        Integer year = riseCertificate.getYear();
        Integer month = riseCertificate.getMonth();
        String problemName = riseCertificate.getProblemName();

        // Integer category = accountService.loadUserScheduleCategory(profile.getId());
        // List<CourseScheduleDefault> courseScheduleDefaults = courseScheduleDefaultDao.loadCourseScheduleDefaultByCategory(category);
        // CourseScheduleDefault courseScheduleDefault = courseScheduleDefaults.stream().filter(schedule -> schedule.getMonth().equals(month)).findAny().orElse(null);
        // Integer problemId = courseScheduleDefault.getProblemId();
        // if (problemId != null) {
        //     Problem problem = cacheService.getProblem(problemId);
        //     if (problem != null) {
        //         problemName = problem.getProblem();
        //     }
        // }

        // if (problemName == null) {
        //     return new MutablePair<>(false, null);
        // }

        Integer groupNo = riseCertificate.getGroupNo();
        String certificateNo = riseCertificate.getCertificateNo();

        // 绘图准备
        BufferedImage inputImage = null;

        InputStream in = ImageUtils.class.getResourceAsStream("/fonts/pfmedium.ttf");
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream inputStream = null;
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            int type = riseCertificate.getType();
            switch (type) {
                case Constants.CERTIFICATE.TYPE.CLASS_LEADER:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀班长", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "担任班长一职，表现突出，荣膺“优秀班长”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀组长", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "担任组长一职，表现优异，荣膺“优秀组长”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀学员", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "成绩名列前茅，荣膺“优秀学员”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀团队", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, NumberToHanZi.formatInteger(month) + "月" + NumberToHanZi.formatInteger(groupNo) + "组", font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "小组表现优异，荣膺“优秀小组”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.ORDINARY:
                    inputImage = ImageUtils.copy(ordinaryImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "结课证书", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "完成课程学习", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.ASST_COACH:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月训练营", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀助教", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月训练营中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "表现卓越，荣膺“优秀助教”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                default:
                    break;
            }

            if (isOnline) {
                String fileName = "certificate-" + CommonUtils.randomString(8) + "-" + certificateNo + ".png";
                // 网页正常显示图片
                outputStream = new ByteArrayOutputStream();
                ImageUtils.writeToOutputStream(inputImage, "png", outputStream);
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                boolean uploadResult = QiNiuUtils.uploadFile(fileName, inputStream);
                return new MutablePair<>(uploadResult, fileName);
            } else {
                StringBuilder builder = new StringBuilder("/Users/xfduan/Downloads/certificate/");

                if (riseCertificate.getMemberTypeId() != null) {
                    if (RiseMember.CAMP == riseCertificate.getMemberTypeId()) {
                        builder.append("camp/");
                    } else {
                        builder.append("rise/");
                    }
                }
                builder.append(riseCertificate.getProfileId()).append("/");

                logger.info(builder.toString());
                File dirPath = new File(builder.toString());
                if (!dirPath.exists()) {
                    dirPath.mkdirs();
                }

                String fileName = "certificate-" + certificateNo + ".png";
                File file = new File(builder.toString() + fileName);
                if (!file.exists()) {
                    ImageUtils.writeToFile(inputImage, "png", file);
                }
                riseCertificateDao.updateDownloadTime(certificateNo);
                // 本地图片文件保存
                return new MutablePair<>(false, null);
            }
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
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("is closed error", e);
            }
        }
        return new MutablePair<>(false, null);
    }

}
