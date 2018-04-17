package com.iquanwai.platon.biz.domain.common.customer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.apply.BusinessSchoolApplicationDao;
import com.iquanwai.platon.biz.dao.common.*;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PracticePlanDao;
import com.iquanwai.platon.biz.dao.fragmentation.PrizeCardDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.apply.BusinessSchoolApplication;
import com.iquanwai.platon.biz.po.common.CustomerStatus;
import com.iquanwai.platon.biz.po.common.Feedback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.RiseUserLogin;
import com.iquanwai.platon.biz.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private AnnualSummaryDao annualSummaryDao;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private FeedbackDao feedbackDao;
    @Autowired
    private RiseUserLoginDao riseUserLoginDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private AnnounceDao announceDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;
    @Autowired
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private PracticePlanDao practicePlanDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ClassMemberDao classMemberDao;


    // 申请通过 status id
    private static final Integer PASS_STATUS_ID = 3;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String uploadHeadImage(Integer profileId, String fileName, InputStream inputStream) {
        BufferedImage bufferedImage = ImageUtils.getBufferedImageByInputStream(inputStream);
        if (bufferedImage == null) {
            return null;
        }

        int startX, startY, endX, endY;
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();
        if (height > width) {
            startX = 0;
            startY = (height - width) / 2;
            endX = width;
            endY = (height + width) / 2;
        } else {
            startX = (width - height) / 2;
            startY = 0;
            endX = (width + height) / 2;
            endY = height;
        }
        BufferedImage cropBufferedImage = ImageUtils.cropImage(bufferedImage, startX, startY, endX, endY);
        BufferedImage reSizeBufferedImage = ImageUtils.scaleByPercentage(cropBufferedImage, 750, 750);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            if (reSizeBufferedImage != null) {
                ImageIO.write(reSizeBufferedImage, "jpeg", os);
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        InputStream cropInputStream = new ByteArrayInputStream(os.toByteArray());

        String targetFileName = "headImage" + "-" + CommonUtils.randomString(8) + "-" + fileName + ".jpeg";
        boolean uploadResult = QiNiuUtils.uploadFile(targetFileName, cropInputStream);

        return uploadResult ? ConfigUtils.getPicturePrefix() + targetFileName : null;
    }

    @Override
    public int updateHeadImageUrl(Integer profileId, String headImgUrl) {
        return profileDao.updateHeadImgUrl(profileId, headImgUrl);
    }

    @Override
    public int updateNickName(Integer profileId, String nickName) {
        return profileDao.updateNickName(profileId, nickName);
    }

    @Override
    public AnnualSummary loadUserAnnualSummary(String riseId) {
        Profile profile = profileDao.queryByRiseId(riseId);
        AnnualSummary annualSummary = annualSummaryDao.loadUserAnnualSummary(riseId);
        if (annualSummary != null) {
            List<PrizeCard> prizeCards = prizeCardDao.getAnnualPrizeCards(profile.getId());
            annualSummary.setCardCount(prizeCards.size());
        }
        return annualSummary;
    }

    @Override
    public void sendFeedback(Feedback feedback) {
        int result = feedbackDao.insert(feedback);
        if (result != -1) {
            TemplateMessage templateMessage = new TemplateMessage();
            templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());
            templateMessage.setTouser(ConfigUtils.feedbackAlarmOpenId());
            Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
            data.put("first", new TemplateMessage.Keyword("圈外用户问题来了，速速去处理吧"));
            data.put("keyword1", new TemplateMessage.Keyword("圈外用户问题"));
            data.put("keyword2", new TemplateMessage.Keyword("H5个人中心反馈问题需处理"));
            data.put("keyword3", new TemplateMessage.Keyword(
                    (DateUtils.parseDateTimeToString(new Date()))));
            data.put("remark", new TemplateMessage.Keyword("\n" + feedback.getWords()));
            templateMessage.setData(data);
            templateMessageService.sendMessage(templateMessage);
        }
    }

    @Override
    public int loadContinuousLoginCount(Integer profileId) {
        List<RiseUserLogin> riseUserLogins = riseUserLoginDao.loadByProfileId(profileId);

        int dayCount = 1;
        Date compareDate = new Date();
        if (DateTime.now().getHourOfDay() <= 6) {
            compareDate = DateUtils.beforeDays(new Date(), 1);
        }

        for (RiseUserLogin riseUserLogin : riseUserLogins) {
            if (DateUtils.interval(compareDate, riseUserLogin.getLoginDate()) <= 1) {
                dayCount++;
                compareDate = riseUserLogin.getLoginDate();
            } else {
                break;
            }
        }
        return dayCount;
    }

    @Override
    public int loadJoinDays(Integer profileId) {
        List<RiseMember> riseMembers = riseMemberDao.loadRiseMembersByProfileId(profileId);
        RiseMember firstAddRiseMember = riseMembers.stream().min(Comparator.comparing(RiseMember::getOpenDate)).orElse(null);
        if (firstAddRiseMember != null) {
            return DateUtils.interval(new Date(), firstAddRiseMember.getOpenDate());
        } else {
            return 1;
        }
    }

    @Override
    public int loadPersonalTotalPoint(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        return profile.getPoint();
    }

    @Override
    public String loadAnnounceMessage(Integer profileId) {
        // TODO: 待验证,取第一个身份展示信息
        List<RiseMember> riseMembers = riseMemberManager.member(profileId);
        RiseMember riseMember = null;
        if (CollectionUtils.isNotEmpty(riseMembers)) {
            riseMember = riseMembers.get(0);
        }
        int memberTypeId = riseMember == null ? 0 : riseMember.getMemberTypeId();
        List<Announce> announces = announceDao.loadByMemberTypeId(memberTypeId);
        Announce validAnnounce = announces.stream().filter(announce -> announce.getStartTime() != null
                && announce.getEndTime() != null
                && new Date().compareTo(announce.getStartTime()) >= 0
                && announce.getEndTime().compareTo(new Date()) >= 0
        ).findAny().orElse(null);

        // 将已经超时的 announce del 置为 1
        List<Integer> expiredIds = announces.stream().filter(announce -> announce.getStartTime() != null
                && announce.getEndTime() != null
                && new Date().compareTo(announce.getEndTime()) > 0
        ).map(Announce::getId).collect(Collectors.toList());
        if (expiredIds.size() > 0) {
            announceDao.delExpiredAnnounce(expiredIds);
        }

        return validAnnounce == null ? null : validAnnounce.getMessage();
    }

    @Override
    public Pair<Boolean, Long> isAlertApplicationPassMessage(Integer profileId) {
        CustomerStatus customerStatus = customerStatusDao.load(profileId, PASS_STATUS_ID);
        Boolean notifyTag;
        if (customerStatus == null) {
            notifyTag = false;
        } else {
            // TODO: 待验证
            List<Integer> memberTypes = Lists.newArrayList();
            memberTypes.add(RiseMember.ELITE);
            memberTypes.add(RiseMember.BUSINESS_THOUGHT);
            List<RiseMember> riseMember = riseMemberDao.loadValidRiseMemberByMemberTypeId(profileId, memberTypes);
            notifyTag = CollectionUtils.isNotEmpty(riseMember);
        }
        if (notifyTag) {
            BusinessSchoolApplication businessSchoolApplication = businessSchoolApplicationDao.getLastVerifiedByProfileId(profileId);
            if (businessSchoolApplication != null && DateUtils.afterDays(businessSchoolApplication.getDealTime(), 1).compareTo(new Date()) > 0) {
                Long intervalLong = DateUtils.afterDays(businessSchoolApplication.getDealTime(), 1).getTime() -
                        System.currentTimeMillis();
                return new MutablePair<>(true, intervalLong);
            } else {
                return new MutablePair<>(false, null);
            }
        } else {
            return new MutablePair<>(false, null);
        }
    }

    @Override
    public Integer loadLearnedKnowledgesCount(Integer profileId) {
        List<ImprovementPlan> improvementPlanList = improvementPlanDao.loadUserAllPlans(profileId);
        List<Integer> planIds = improvementPlanList.stream().map(ImprovementPlan::getId).collect(Collectors.toList());
        List<PracticePlan> practicePlanList = practicePlanDao.loadByPlanIds(planIds);
        Long result = practicePlanList.stream().filter(practicePlan -> practicePlan.getType() == PracticePlan.KNOWLEDGE && practicePlan.getStatus() == PracticePlan.STATUS.COMPLETED).count();
        return result.intValue();
    }

    @Override
    public Integer calSyncDefeatPercent(RiseMember riseMember) {
        if (riseMember == null) {
            logger.error("该用户不是会员，返回比例为0");
            return 0;
        }
        Integer profileId = riseMember.getProfileId();
        Profile profile = accountService.getProfile(profileId);
        if (profile == null) {
            logger.error("未找到{}用户，返回比例为0", profileId);
            return 0;
        }
        Integer point = profile.getPoint();
        if (point == 0) {
            logger.info("{}用户得0分", profileId);
            return 0;
        }

        Date openDate = riseMember.getOpenDate();
        if (openDate == null || openDate.toString().length() < 7) {
            logger.error("{}用户的入学日期为空或者不规范，返回比例为0", profileId);
            return 0;
        }

        String currentDate = openDate.toString().substring(0, 7);

        List<RiseMember> riseMemberList = riseMemberDao.loadSyncRiseMembers(currentDate, riseMember.getMemberTypeId());
        List<Integer> profileIds = riseMemberList.stream().map(RiseMember::getProfileId).collect(Collectors.toList());

        List<Profile> profiles = accountService.getProfiles(profileIds);

        if (CollectionUtils.isEmpty(profiles)) {
            logger.info("{}用户不存在同期同学，返回比例为100%", profileId);
            return 100;
        }

        Long result = profiles.stream().filter(profile1 -> profile1.getPoint() == null || profile1.getPoint() <= point).count();
        logger.info("超过人数为：" + result + ",总人数为：" + profiles.size());
        int size = profiles.size();
        double realPercent = result.intValue() * 1.0 / size;
        Integer percent;
        // 20%以下分布在0-60%的区间,20%以上分布在60%-100%区间
        if (realPercent <= 0.2) {
            percent = (int) ((realPercent * 5 - realPercent * realPercent * 10) * 100);
            logger.info("计算出的比例为：" + percent + "%");
        } else {
            percent = (result.intValue() + size) * 100 / (size * 2);
            logger.info("计算出的比例为：" + percent + "%");
        }

        return percent;
    }

    @Override
    public List<Pair<String, String>> loadClassGroup(Integer profileId) {
        List<ClassMember> classMembers = classMemberDao.loadActiveByProfileId(profileId);
        if (classMembers.isEmpty()) {
            ClassMember exist = classMemberDao.loadLatestByProfileId(profileId);
            if (exist != null) {
                classMembers = Lists.newArrayList(exist);
            }
        }
        List<Pair<String, String>> propsList = Lists.newArrayList();
        classMembers.forEach(item -> {
            if (item.getClassName() != null) {
                propsList.add(Pair.of(riseMemberManager.classNameKey(item.getMemberTypeId()), item.getClassName()));
            }
            if (item.getGroupId() != null) {
                propsList.add(Pair.of(riseMemberManager.groupIdKey(item.getMemberTypeId()), item.getGroupId()));
            }
        });
        return propsList;
    }

}
