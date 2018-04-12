package com.iquanwai.platon.biz.domain.fragmentation.certificate;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ClassMemberDao;
import com.iquanwai.platon.biz.dao.common.CouponDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.ProblemScheduleManager;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
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
    private RiseMemberManager riseMemberManager;
    @Autowired
    private RiseCertificateDao riseCertificateDao;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private ProfileDao profileDao;


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
        riseCertificate.setName(profile.getRealName());
        if (profile.getRealName() != null && riseCertificate.getImageUrl() == null) {
            Pair<Boolean, String> pair = drawRiseCertificate(riseCertificate, true);
            if (pair.getLeft()) {
                String imageUrl = ConfigUtils.getPicturePrefix() + pair.getRight();
                riseCertificate.setImageUrl(imageUrl);
                riseCertificateDao.updateImageUrl(riseCertificate.getId(), imageUrl);
            }
        }
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
    public void generateGraduateCertificateByMemberType(Integer year, Integer month, Integer memberTypeId) {
        // 根据身份，获取所有该身份的人员
        List<ClassMember> classMembers = classMemberDao.loadByMemberTypeId(memberTypeId);
        // 获取身份还在有效期的人员
        List<ClassMember> activeClassMembers = classMembers.stream().filter(ClassMember::getActive).collect(Collectors.toList());

        activeClassMembers.forEach(classMember -> {
            // 对于每笔符合条件的 ClassMember 生成所有当月学习中主修的证书信息
            Integer profileId = classMember.getProfileId();
            List<Integer> majorProblemIds = problemScheduleManager.getMajorProblemIds(profileId, memberTypeId, year, month);
            List<ImprovementPlan> majorPlans = improvementPlanDao.loadPlansByProblemIds(profileId, majorProblemIds);
            majorPlans.forEach(plan -> {
                boolean generatePermission = checkGenerateCertificatePermission(plan);
                if (generatePermission) {
                    insertAndDrawCertificate(classMember, year, month, RiseCertificate.Type.GRADUATE, plan.getProblemId());
                }
            });
        });
    }

    @Override
    public void insertSpecialCertificate(List<String> memberIds, Integer year, Integer month, Integer memberTypeId, Integer type) {
        List<Profile> profiles = profileDao.queryByMemberIds(memberIds);
        List<Integer> profileIds = profiles.stream().map(Profile::getId).collect(Collectors.toList());
        List<ClassMember> allClassMembers = classMemberDao.loadByProfileIds(profileIds);
        List<ClassMember> activeClassMembers = allClassMembers.stream()
                .filter(classMember -> memberTypeId.equals(classMember.getMemberTypeId()))
                .filter(ClassMember::getActive)
                .collect(Collectors.toList());

        activeClassMembers.forEach(classMember -> {
            // 对于每笔符合条件的 ClassMember 生成所有当月学习中主修的证书信息
            Integer profileId = classMember.getProfileId();
            List<Integer> majorProblemIds = problemScheduleManager.getMajorProblemIds(profileId, memberTypeId, year, month);
            majorProblemIds.forEach(problemId -> {
                insertAndDrawCertificate(classMember, year, month, type, problemId);
            });
        });
    }

    @Override
    public void generateBatchFullAttendance(Integer year, Integer month, Integer memberTypeId) {
        // 根据身份，获取所有该身份的人员
        List<ClassMember> classMembers = classMemberDao.loadByMemberTypeId(memberTypeId);
        List<ClassMember> activeClassMembers = classMembers.stream().filter(ClassMember::getActive).collect(Collectors.toList());

        activeClassMembers.forEach(classMember -> {
            // 对于每笔符合条件的 ClassMember 生成所有当月学习中主修课程的全勤奖
            Integer profileId = classMember.getProfileId();
            List<Integer> majorProblemIds = problemScheduleManager.getMajorProblemIds(profileId, memberTypeId, year, month);
            List<ImprovementPlan> majorPlans = improvementPlanDao.loadPlansByProblemIds(profileId, majorProblemIds);
            majorPlans.forEach(plan -> {
                boolean generatePermission = checkGenerateFullAttendancePermission(plan);
                if (generatePermission) {
                    FullAttendanceReward fullAttendanceReward = insertFullAttendance(profileId, plan.getProblemId(), year, month, 50.0);
                    if (fullAttendanceReward != null) {
                        sendSingleFullAttendanceCoupon(year, month, plan.getProfileId());
                    }
                }
            });
        });
    }

    @Override
    public void generatePersonalFullAttendance(Integer planId) {
        ImprovementPlan improvementPlan = improvementPlanDao.load(ImprovementPlan.class, planId);
        if (improvementPlan != null) {
            int profileId = improvementPlan.getProfileId();
            int problemId = improvementPlan.getProblemId();
            int learningYear = ConfigUtils.getLearningYear();
            int learningMonth = ConfigUtils.getLearningMonth();

            boolean generatePermission = checkGenerateFullAttendancePermission(improvementPlan);
            if (generatePermission) {
                FullAttendanceReward fullAttendanceReward = insertFullAttendance(profileId, problemId, learningYear, learningMonth, 50.0);
                if (fullAttendanceReward != null) {
                    sendSingleFullAttendanceCoupon(learningYear, learningMonth, profileId);
                }
            }
        }
    }

    @Override
    public void sendCertificate(Integer year, Integer month, Integer memberTypeId) {
        List<RiseCertificate> certificateList = riseCertificateDao.loadUnNotifiedByMonthAndYearAndMemberTypeId(year, month, memberTypeId);
        certificateList.forEach(riseCertificate -> {
            Integer type = riseCertificate.getType();
            TemplateMessage templateMessage = new TemplateMessage();
            Profile profile = accountService.getProfile(riseCertificate.getProfileId());
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.sendCertificateMsg());
            templateMessage.setUrl(ConfigUtils.domainName() + "/rise/static/customer/certificate?certificateNo=" + riseCertificate.getCertificateNo());
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

                data.put("first", new TemplateMessage.Keyword("恭喜您" + month + "月课程结课，" +
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
            data.put("first", new TemplateMessage.Keyword("价值" + fullAttendanceReward.getAmount().intValue() + "元的“全勤奖学金”已经放入您的账户！\n"));
            data.put("keyword1", new TemplateMessage.Keyword(DateUtils.parseDateToFormat6(new Date())));
            data.put("keyword2", new TemplateMessage.Keyword("奖学金（优惠券）"));
            data.put("keyword3", new TemplateMessage.Keyword("价值" + fullAttendanceReward.getAmount().intValue() + "元"));

            templateMessageService.sendMessage(templateMessage);
        }
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
            case Constants.CERTIFICATE.TYPE.CLASS_NORMAL:
                amount = PRIZE_COUPON_CLASS_LEADER;
                description = "优秀班委奖学金";
            default:
                break;
        }
        if (amount == -1) {
            return;
        }

        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);

        data.put("keyword1", new TemplateMessage.Keyword(
                "您的" + riseCertificate.getMonth() + "月课程" + amount + "元优惠券奖励已到账"));
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

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀班长\n" +
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

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀组长\n" +
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

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀学员\n" +
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

                    data.put("first", new TemplateMessage.Keyword("恭喜您所在的小组荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀团队\n" +
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
                    data.put("first", new TemplateMessage.Keyword("恭喜您完成［圈外同学］" + riseCertificate.getMonth() + "月课程\n" +
                            "点击详情，领取结课证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n荣誉证书也可以在［我的］－［我的课程］中查询"));

                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.ASST_COACH:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀助教\n" +
                            "点击详情，领取优秀助教荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));

                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.CLASS_NORMAL:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);

                    data.put("first", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月课程优秀班委\n" +
                            "点击详情，领取优秀班委荣誉证书\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(riseCertificate.getProblemName()));
                    data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("keyword3", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀班委的同学，除了荣誉证书外，还将获得圈外200元优惠券（领取及使用方式请见下条消息）" +
                            "\n\n" +
                            "此外，如果您在本次会员期内，\n" +
                            "累计3次荣膺优秀班委：额外获得圈外周边礼物+圈圈签名书\n" +
                            "累计6次荣膺优秀班委：额外获得优秀班委礼包"));
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
            default:
                logger.error("证书类型{}不存在", type);
                break;
        }
    }

    private void buildCouponExpireDate(Coupon coupon, Profile profile) {
        //TODO: 有问题
        RiseMember riseMember = riseMemberManager.coreBusinessSchoolUser(profile.getId());
        if (riseMember != null) {
            if (Constants.RISE_MEMBER.MEMBERSHIP == profile.getRiseMember()) {
                coupon.setExpiredDate(DateUtils.afterYears(new Date(), 1));
            } else {
                coupon.setExpiredDate(DateUtils.afterMonths(riseMember.getExpireDate(), 1));
            }
        }
    }

    /**
     * 校验当前学习计划是否有权限生成全勤奖
     * @param improvementPlan 学习计划
     * @return 是否能生成全勤奖
     */
    private boolean checkGenerateFullAttendancePermission(ImprovementPlan improvementPlan) {
        boolean generateFullAttendanceTag = true;
        int planId = improvementPlan.getId();

        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        if (practicePlans.size() != 0) {
            // 完成所有练习
            Long unCompleteNecessaryCountLong = practicePlans.stream()
                    .filter(practicePlan -> PracticePlan.CHALLENGE != practicePlan.getType())
                    .filter(practicePlan -> PracticePlan.STATUS.UNCOMPLETED == practicePlan.getStatus())
                    .count();
            if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                // 如果存在有没有完成的题数，则不予发送优惠券
                generateFullAttendanceTag = false;
            } else {
                // 完成所有练习之后，对应用题完成情况进行复查
                List<PracticePlan> applicationPracticePlans = practicePlans.stream()
                        .filter(practicePlan -> PracticePlan.isApplicationPractice(practicePlan.getType()))
                        .collect(Collectors.toList());
                List<Integer> applicationIds = applicationPracticePlans.stream().map(PracticePlan::getPracticeId).map(Integer::parseInt).collect(Collectors.toList());
                // TODO: 改成不需要加载应用题内容的接口
                List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadApplicationSubmitsByApplicationIds(applicationIds, planId);
                Map<Integer, ApplicationSubmit> applicationSubmitMap = applicationSubmits.stream().collect(Collectors.toMap(ApplicationSubmit::getApplicationId, applicationSubmit -> applicationSubmit, (key1, key2) -> key2));

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
                    generateFullAttendanceTag = false;
                }
            }
        } else {
            generateFullAttendanceTag = false;
        }
        return generateFullAttendanceTag;
    }

    /**
     * 插入全勤奖记录表
     * @param profileId 学员身份 id
     * @param problemId 课程 id
     * @param year 全勤奖对应学习年份
     * @param month 全勤奖对应学习月份
     * @param amount 全勤奖金额
     * @return 全勤奖对象
     */
    private FullAttendanceReward insertFullAttendance(Integer profileId, Integer problemId, Integer year, Integer month, Double amount) {
        FullAttendanceReward fullAttendanceReward = fullAttendanceRewardDao.loadFullAttendanceRewardByProblemId(profileId, problemId, year, month);
        if (fullAttendanceReward == null) {
            FullAttendanceReward attendanceReward = new FullAttendanceReward();
            attendanceReward.setProfileId(profileId);
            attendanceReward.setProblemId(problemId);
            attendanceReward.setYear(year);
            attendanceReward.setMonth(month);
            attendanceReward.setAmount(amount);
            int result = fullAttendanceRewardDao.insert(attendanceReward);
            if (result > 0) {
                return attendanceReward;
            }
        }
        return null;
    }

    /**
     * 校验学习情况，是否有权限生成证书
     * @param improvementPlan 学习记录
     * @return 是否有权限生成证书
     */
    private boolean checkGenerateCertificatePermission(ImprovementPlan improvementPlan) {
        boolean generateCertificateTag = true;

        int planId = improvementPlan.getId();
        List<PracticePlan> practicePlans = practicePlanDao.loadPracticePlan(planId);
        if (practicePlans.size() != 0) {
            // 应该完成的知识点、选择题中未完成的题数
            Long unCompleteNecessaryCountLong = practicePlans.stream()
                    .filter(practicePlan ->
                            PracticePlan.WARM_UP == practicePlan.getType() || PracticePlan.WARM_UP_REVIEW == practicePlan.getType()
                                    || PracticePlan.KNOWLEDGE == practicePlan.getType() || PracticePlan.KNOWLEDGE_REVIEW == practicePlan.getType())
                    .filter(practicePlan -> practicePlan.getStatus() == 0)
                    .count();
            if ((unCompleteNecessaryCountLong.intValue()) > 0) {
                // 必须完成知识点、选择题的题数大于 0，不发结课证书
                generateCertificateTag = false;
            } else {
                // 所有必须完成的知识点、选择题都已经完成
                // 对应用题完成情况进行复查
                List<PracticePlan> applicationPracticePlans = practicePlans.stream()
                        .filter(practicePlan -> PracticePlan.isApplicationPractice(practicePlan.getType()))
                        .collect(Collectors.toList());

                List<Integer> applicationIds = applicationPracticePlans.stream().map(PracticePlan::getPracticeId).map(Integer::parseInt).collect(Collectors.toList());
                // TODO: 改成不需要加载应用题内容的接口
                List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadApplicationSubmitsByApplicationIds(applicationIds, planId);
                Map<Integer, ApplicationSubmit> applicationSubmitMap = applicationSubmits.stream()
                        .collect(Collectors.toMap(ApplicationSubmit::getApplicationId, applicationSubmit -> applicationSubmit, (key1, key2) -> key2));

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
        } else {
            generateCertificateTag = false;
        }
        return generateCertificateTag;
    }

    /**
     * 在 RiseCertificate 表中插入相应数据，并将相应证书图片上传七牛云
     * @param classMember 班级信息对象
     * @param year 生成证书年份
     * @param month 生成证书月份
     * @param type 生成证书类型
     * @param problemId 生成证书的课程 id
     */
    private void insertAndDrawCertificate(ClassMember classMember, Integer year, Integer month, Integer type, Integer problemId) {
        RiseCertificate certificate = insertCertificateRecord(classMember, year, month, type, problemId);
        if (certificate != null) {
            drawRiseCertificate(certificate, true);
        }
    }

    /**
     * 数据库 insert 证书记录
     * @param classMember 班级信息对象
     * @param year 生成证书年份
     * @param month 生成证书月份
     * @param type 生成证书类型
     * @param problemId 生成证书的课程 id
     * @return 是否在数据库中成功 insert 证书数据
     */
    private RiseCertificate insertCertificateRecord(ClassMember classMember, Integer year, Integer month, Integer type, Integer problemId) {
        Integer profileId = classMember.getProfileId();
        Integer memberTypeId = classMember.getMemberTypeId();
        String groupId = classMember.getGroupId();

        Profile profile = accountService.getProfile(profileId);
        Assert.notNull(profile);

        List<RiseCertificate> riseCertificates = riseCertificateDao.loadExistRiseCertificates(profileId, year, month);
        RiseCertificate riseCertificate = riseCertificates.stream()
                .filter(certificate -> classMember.getMemberTypeId().equals(certificate.getMemberTypeId()) && problemId.equals(certificate.getProblemId()))
                .findAny().orElse(null);

        if (riseCertificate == null) {
            // 不存在当前类型的证书
            RiseCertificate certificate = new RiseCertificate();
            certificate.setProfileId(profileId);
            certificate.setMemberTypeId(memberTypeId);
            certificate.setType(type);
            certificate.setCertificateNo(generateCertificateNo(profile.getMemberId(), month));
            certificate.setYear(year);
            certificate.setMonth(month);
            certificate.setGroupNo(Integer.parseInt(groupId));
            certificate.setProblemId(problemId);
            certificate.setProblemName(cacheService.getProblem(problemId).getAbbreviation());
            int result = riseCertificateDao.insert(certificate);
            if (result > 0) {
                return certificate;
            }
        }
        return null;
    }

    /**
     * 生成随机证书号
     * @param memberId 学号
     * @param month 月份
     * @return 证书号
     */
    private String generateCertificateNo(String memberId, Integer month) {
        StringBuilder certificateNoBuilder = new StringBuilder("IQW");
        certificateNoBuilder.append(String.format("%02d", Constants.CERTIFICATE.TYPE.ORDINARY));
        certificateNoBuilder.append(memberId);
        certificateNoBuilder.append(String.format("%02d", month));
        certificateNoBuilder.append(String.format("%03d", RandomUtils.nextInt(0, 1000)));
        certificateNoBuilder.append(String.format("%02d", RandomUtils.nextInt(0, 100)));
        return certificateNoBuilder.toString();
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
        String problemName;
        Integer problemId = riseCertificate.getProblemId();
        if (problemId != null) {
            problemName = cacheService.getProblem(problemId).getAbbreviation();
        } else {
            problemName = riseCertificate.getProblemName();
        }
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
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀班长", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "担任班长一职，表现突出，荣膺“优秀班长”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀组长", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "担任组长一职，表现优异，荣膺“优秀组长”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀学员", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "成绩名列前茅，荣膺“优秀学员”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                    inputImage = ImageUtils.copy(excellentImage);
                    RiseClassMember riseClassMember = riseClassMemberDao.loadActiveRiseClassMember(riseCertificate.getProfileId());
                    String className = riseClassMember.getClassName();
                    String classNumber = className.substring(4);
                    String monthNumber = className.substring(2, 4);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀团队", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, NumberToHanZi.formatInteger(Integer.parseInt(monthNumber)) + "月" + NumberToHanZi.formatInteger(Integer.parseInt(classNumber)) + "班" + NumberToHanZi.formatInteger(groupNo) + "组", font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "小组表现优异，荣膺“优秀小组”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.ORDINARY:
                    inputImage = ImageUtils.copy(ordinaryImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "结课证书", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "完成课程学习", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.ASST_COACH:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀助教", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "表现卓越，荣膺“优秀助教”称号", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 950, "特发此证，以资鼓励", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 1555, "证书编号：" + certificateNo, font.deriveFont(30f), new Color(182, 144, 47));
                    break;
                case Constants.CERTIFICATE.TYPE.CLASS_NORMAL:
                    inputImage = ImageUtils.copy(excellentImage);
                    ImageUtils.writeTextCenter(inputImage, 200, "圈外同学 • " + month + "月课程", font.deriveFont(28f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 265, "《" + problemName + "》", font.deriveFont(42f), new Color(255, 255, 255));
                    ImageUtils.writeTextCenter(inputImage, 450, "优秀班委", font.deriveFont(92f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 650, profile.getRealName(), font.deriveFont(78f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 765, "在【圈外同学】" + year + "年" + month + "月课程中", font.deriveFont(48f), new Color(102, 102, 102));
                    ImageUtils.writeTextCenter(inputImage, 850, "担任班委一职，表现突出，荣膺“优秀班委”称号", font.deriveFont(48f), new Color(102, 102, 102));
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
