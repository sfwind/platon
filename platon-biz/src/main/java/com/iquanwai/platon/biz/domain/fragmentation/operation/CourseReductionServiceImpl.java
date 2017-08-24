package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.CourseReductionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionActivityDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.CourseReductionActivity;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.PromotionActivity;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.QuanwaiOrder;
import com.iquanwai.platon.biz.po.common.SubscribeEvent;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.PromotionConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/8/16.
 */
@Service
public class CourseReductionServiceImpl implements CourseReductionService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private PromotionActivityDao promotionActivityDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CourseReductionActivityDao courseReductionActivityDao;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private TemplateMessageService templateMessageService;

    @Override
    public void scanCourseReductionQR(SubscribeEvent subscribeEvent) {
        logger.info("处理课程优惠mq;{}", subscribeEvent);
        if (!StringUtils.startsWith(subscribeEvent.getScene(), PromotionConstants.Activities.CourseReduction)
                || subscribeEvent.getOpenid() == null) {
            logger.error("扫描优惠推广课程的事件处理异常,{}", subscribeEvent);
            return;
        }
        Profile profile = accountService.getProfile(subscribeEvent.getOpenid());
        if (profile == null) {
            logger.error("扫描推广课程的事件处理异常，没有该用户:{}", subscribeEvent.getOpenid());
            return;
        }
        List<String> list = Lists.newArrayList();
        list.add(subscribeEvent.getScene().split("_")[0]);
        CourseReductionActivity activity = courseReductionActivityDao.loadReductions(list).stream().findFirst().orElse(null);
        if (activity == null) {
            logger.error("没有活动信息:{}", subscribeEvent);
            return;
        }
        String sendMsg;
        if (activity.getProblemId() != null) {
            Problem problem = cacheService.getProblem(activity.getProblemId());

            sendMsg = profile.getNickname() + "，果然只有机智的人才能成为张良计的真爱粉[嘿哈]\n" +
                    "\n" +
                    "赶紧点击下方链接购买\uD83D\uDC47\n" +
                    "\n" +
                    "<a href='" + ConfigUtils.adapterDomainName() +
                    "/rise/static/plan/view?id=" +
                    problem.getId() +
                    "&free=true'>『" + problem.getProblem() + "』</a>\n" +
                    "\n" +
                    "凡是购买Boy小课的真爱粉丝，我们为你准备了一个粉丝大礼包\uD83C\uDF81\n" +
                    "\n" +
                    "礼包内含：张良计主讲小课+思考力小课+课程群服务\n" +
                    "\n" +
                    "心动啦？以上福利仅在23号~30号赠送哦~赶快购买吧！";
        } else {
            sendMsg = profile.getNickname() + "，果然只有机智的人才能成为张良计的真爱粉[嘿哈]\n" +
                    "\n" +
                    "凡是购买Boy小课的真爱粉丝，我们为你准备了一个粉丝大礼包\uD83C\uDF81\n" +
                    "\n" +
                    "礼包内含：张良计主讲小课+思考力小课+课程群服务\n" +
                    "\n" +
                    "心动啦？以上福利仅在23号~30号赠送哦~赶快购买吧！\n" +
                    "点击\"上课啦\"开始上课";
        }

        customerMessageService.sendCustomerMessage(subscribeEvent.getOpenid(), sendMsg,
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        // 发送海报
//        String mediaId = ConfigUtils.isDevelopment() == null || !ConfigUtils.isDevelopment() ? "oNP9rE2TKhmfaLkbdss_lK5OuF5bADXqYrx1wjyFWVE" : "DKejbjbUawA773Mq37YnIRIHbTMlMEQT_WTTuWYab4M17KELKS6Cwtguk5pLWnS4";
//        customerMessageService.sendCustomerMessage(subscribeEvent.getOpenid(), mediaId, Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        //直接入activity
        PromotionActivity promotionActivity = new PromotionActivity();
        promotionActivity.setAction(PromotionConstants.CourseReductionAction.ScanCode);
        promotionActivity.setActivity(subscribeEvent.getScene());
        promotionActivity.setProfileId(profile.getId());
        promotionActivityDao.insertPromotionActivity(promotionActivity);
        // 查看在不在level表
        PromotionLevel promotionLevel = promotionLevelDao.loadByProfileId(profile.getId(), subscribeEvent.getScene());
        if (promotionLevel == null) {
            // level表没有该数据
            PromotionLevel level = new PromotionLevel();
            level.setProfileId(profile.getId());
            level.setActivity(subscribeEvent.getScene());
            level.setLevel(1);
            level.setPromoterId(null);
            level.setValid(1);
            promotionLevelDao.insertPromotionLevel(level);
        }
    }

    @Override
    public void saveCourseReductionPayedLog(QuanwaiOrder quanwaiOrder) {
        Profile profile = accountService.getProfile(quanwaiOrder.getOpenid());
        Pair<CourseReductionActivity, PromotionLevel> pair = this.loadRecentCourseReduction(profile.getId(), Integer.parseInt(quanwaiOrder.getGoodsId()));
        if (pair != null) {
            logger.info("记录优惠使用情况:{}", quanwaiOrder);
            PromotionLevel promotionLevel = pair.getRight();

            PromotionActivity activity = new PromotionActivity();
            activity.setProfileId(profile.getId());
            activity.setActivity(promotionLevel.getActivity());
            if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_MEMBER)) {
                // 会员
                activity.setAction(PromotionConstants.CourseReductionAction.PayMember);
            } else if (quanwaiOrder.getGoodsType().equals(QuanwaiOrder.FRAGMENT_RISE_COURSE)) {
                // 小课
                activity.setAction(PromotionConstants.CourseReductionAction.PayCourse);
                // TODO 8.31日0点删除
                if ("19".equals(quanwaiOrder.getGoodsId())) {
                    PromotionActivity temp = new PromotionActivity();
                    temp.setProfileId(profile.getId());
                    temp.setActivity(promotionLevel.getActivity());
                    temp.setAction(PromotionConstants.CourseReductionAction.PayZhangPeng);
                    promotionActivityDao.insertPromotionActivity(temp);
                    TemplateMessage templateMessage = new TemplateMessage();
                    templateMessage.setTouser(profile.getOpenid());
                    String msgId = ConfigUtils.isDevelopment() == null || !ConfigUtils.isDevelopment() ? "2n8N79pHw8tBHwTUdManihUnCrKl2FEpELtq-sDF0NU" : "crZiCkNMCec7svHsHcKSxTTzPT5NWOA1To5HmhyaDeE";
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setTemplate_id(msgId);
                    templateMessage.setData(data);
                    data.put("first", new TemplateMessage.Keyword(
                            "亲爱的Boy粉，我们很高兴你能加入我们，和数十万职场人一起提升自我。\n\n" +
                                    "我们为你准备了一个大礼包，内含价值99元的思考力课程一门和一张粉丝团通行证。\n"));
                    data.put("keyword1", new TemplateMessage.Keyword(profile.getNickname()));
                    data.put("keyword2", new TemplateMessage.Keyword("张良计Boy粉丝礼包"));
                    data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
                    data.put("remark", new TemplateMessage.Keyword("\n点击详情拆开礼包哦↓↓↓"));
                    templateMessage.setUrl("https://shimo.im/doc/Vc0qdZw0Qv8VIlqS?r=NPGKQE/");
                    templateMessageService.sendMessage(templateMessage);
                }
            }
            promotionActivityDao.insertPromotionActivity(activity);
        }
    }


    // TODO 8.31日0点删除
    @Override
    public Boolean isPayZhangPeng(Integer profileId){
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByRegex(PromotionConstants.Activities.CourseReduction, profileId);
        for (PromotionLevel promotionLevel : promotionLevels) {
            List<PromotionActivity> promotionActivities = promotionActivityDao.loadPromotionActivities(profileId, promotionLevel.getActivity());
            for (PromotionActivity activity : promotionActivities) {
                if (activity.getAction() == PromotionConstants.CourseReductionAction.PayZhangPeng) {
                    // 购买张鹏课程
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public Pair<CourseReductionActivity, PromotionLevel> loadRecentCourseReduction(Integer profileId, Integer problemId) {
        List<PromotionLevel> promotionLevels = promotionLevelDao.loadByRegex(PromotionConstants.Activities.CourseReduction, profileId);
        if (CollectionUtils.isEmpty(promotionLevels)) {
            return null;
        }
        List<String> activities = Lists.newArrayList();
        promotionLevels.forEach(level -> {
            String activity = level.getActivity();
            String[] split = activity.split("_");
            if (split.length < 1) {
                logger.error("异常，课程减免活动数据异常", level);
            } else {
                String realActivity = split[0];
                if (!activities.contains(realActivity)) {
                    activities.add(realActivity);
                }
            }
        });
        List<CourseReductionActivity> list = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(activities)) {
            list = courseReductionActivityDao
                    .loadReductions(activities)
                    .stream()
                    .filter(activity -> activity.getProblemId() == null ||
                            activity.getProblemId().equals(problemId))
                    .collect(Collectors.toList());
        }
        promotionLevels.sort((o1, o2) -> o2.getId() - o1.getId());
        for (PromotionLevel level : promotionLevels) {
            for (CourseReductionActivity courseReductionActivity : list) {
                if (level.getActivity().contains(courseReductionActivity.getActivity())) {
                    // 取出第一个匹配到的courseReduction,因为有可能扫码后活动取消了
                    // courseReduction_zlj_02 contains courseReduction_zlj
                    return new MutablePair<>(courseReductionActivity, level);
                }
            }
        }
        // 上面没有return的话，则return null
        return null;
    }
}
