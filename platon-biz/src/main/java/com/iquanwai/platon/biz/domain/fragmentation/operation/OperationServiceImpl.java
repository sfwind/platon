package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionUserDao;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CardRepository;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xfduan on 2017/7/14.
 */
@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private PromotionUserDao promotionUserDao;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private MQService mqService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private UploadResourceService uploadResourceService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RabbitMQPublisher rabbitMQPublisher;

    private final static String cacheReloadTopic = "confucius_resource_reload";
    // 活动前缀
    private final static String prefix = "freeLimit_";
    private final static String naturePrefix = "natureRaise";
    // 推广的熊猫卡临时存放路径
    private final static String TEMP_IMAGE_PATH = "/data/static/images/";
    // 推广成功人数限额
    private final static Integer successNum = ConfigUtils.getFreeLimitSuccessCnt();

    @PostConstruct
    public void init() {
        rabbitMQPublisher = new RabbitMQPublisher();
        rabbitMQPublisher.init(cacheReloadTopic);
        rabbitMQPublisher.setSendCallback(mqService::saveMQSendOperation);
    }

    @Override
    public void recordPromotionLevel(String openId, String scene) {
        PromotionLevel tempPromotionLevel = promotionLevelDao.loadByOpenId(openId);
        if (!scene.contains(prefix) || tempPromotionLevel != null) return; // 不是本次活动，或者说已被其他用户推广则不算新人
        String[] sceneParams = scene.split("_");
        if (sceneParams.length != 3) return;
        Profile profile = accountService.getProfile(openId);
        if (profile != null && profile.getRiseMember() == 1) {
            // 会员扫码无效
            logger.error("recordPromotionLevel error,no profile ,openid:{},scene:{},profile:{}", openId, scene, profile);
            return;
        }
        if ("RISE".equals(sceneParams[1])) {
            promotionLevelDao.insertPromotionLevel(openId, 1);
        } else {
            Integer promotionProfileId = Integer.parseInt(sceneParams[1]);
            Profile promotionProfile = accountService.getProfile(promotionProfileId);
            String promotionOpenId = promotionProfile.getOpenid(); // 推广人的 OpenId
            PromotionLevel promotionLevelObject = promotionLevelDao.loadByOpenId(promotionOpenId); // 推广人层级表对象
            if (openId.equals(promotionOpenId)) {
                logger.error("自己扫自己，不能入表");
                return;
            }
            if (promotionLevelObject != null) {
                Integer promotionLevel = promotionLevelObject.getLevel(); // 推广人所在推广层级
                promotionLevelDao.insertPromotionLevel(openId, promotionLevel + 1);
            } else {
                // 没有推广人，推广人没有扫码，或者已经是会员
                promotionLevelDao.insertPromotionLevel(openId, 2);
                // 查看是否在user表里
                PromotionUser exist = promotionUserDao.loadUserByOpenId(openId);
                if (exist == null) {
                    PromotionUser promotionUser = new PromotionUser();
                    promotionUser.setSource(naturePrefix);
                    promotionUser.setOpenId(openId);
                    promotionUser.setAction(0);
                    promotionUser.setProfileId(null);
                    promotionUserDao.insert(promotionUser);
                }
            }
        }
    }

    @Override
    public void initFirstPromotionLevel(String openId, Integer riseMember) {
        // 不是会员
        if (riseMember != null && riseMember != 1) {
            // 查询是否在level表里
            PromotionLevel promotionLevel = promotionLevelDao.loadByOpenId(openId);
            if (promotionLevel == null) {
                // 没有在level表里
                PromotionUser promotionUser = new PromotionUser();
                promotionUser.setSource(naturePrefix);
                promotionUser.setOpenId(openId);
                promotionUser.setAction(0);
                promotionUser.setProfileId(null);
                promotionLevelDao.insertPromotionLevel(openId, 1);
                promotionUserDao.insert(promotionUser);
            }
        }
    }

    @Override
    public void recordOrderAndSendMsg(String openId, Integer newAction) {
        PromotionUser orderUser = promotionUserDao.loadUserByOpenId(openId); // 查询 PromotionUser 中是否存在该用户信息
        if (orderUser != null && orderUser.getSource() != null && orderUser.getSource().contains(prefix)) {
            // 该用户存在于新人表，并且是此次活动的用户
            Integer oldAction = orderUser.getAction();
            if (newAction > oldAction) { // 根据 action 类型，更新 action 信息
                promotionUserDao.updateActionByOpenId(openId, newAction);
            }
            // 必是成功推广，此时给推广人发送成功推送信息
            List<PromotionUser> newUsers;
            List<PromotionUser> successUsers;
            Integer profileId = orderUser.getProfileId();
            if (profileId != null) {
                newUsers = promotionUserDao.loadUsersByProfileId(profileId);
            } else {
                String source = orderUser.getSource();
                // 查看推广人当前所有推广的新人列表
                newUsers = promotionUserDao.loadUsersBySource(source);
            }
            successUsers = newUsers.stream().filter(user -> user.getAction() > 0).collect(Collectors.toList());
            logger.info("推广成功用户人数" + successUsers.size());
            // 发送推广成功消息
            // 获取的是来源的 profileId (推广人的 profileId)
            Integer sourceProfileId = orderUser.getProfileId();
            if (sourceProfileId == null) return;
            Profile sourceProfile = accountService.getProfile(sourceProfileId); // 推广人 Profile
            if (sourceProfile == null) return;
            // 区分是否为会员
            if (sourceProfile.getRiseMember() == 1) {
                logger.info("是会员");
                // 是会员
                if (successUsers.size() <= successNum) {
                    sendNormalSuccessOrderMsg(sourceProfile.getOpenid(), openId);
                }
            } else {
                logger.info("不是会员");
                // 非会员
                if (successUsers.size() < successNum) {
                    logger.info("正常推广");
                    sendSuccessOrderMsg(sourceProfile.getOpenid(), openId, successNum - successUsers.size());
                } else if (successUsers.size() == successNum) {
                    logger.info("准备拿优惠券");
                    // 发送优惠券，Coupon 表新增数据
                    Coupon coupon = new Coupon();
                    coupon.setOpenId(sourceProfile.getOpenid());
                    coupon.setProfileId(sourceProfile.getId());
                    coupon.setAmount(50);
                    coupon.setExpiredDate(DateUtils.afterDays(new Date(), 30));
                    coupon.setDescription("奖学金");
                    coupon.setUsed(0);
                    Integer insertResult = couponDao.insertCoupon(coupon);
                    if (insertResult > 0) {
                        logger.info("准备塞优惠券");
                        // 礼品券数据保存成功，发送获得优惠券的模板消息
                        sendSuccessPromotionMsg(sourceProfile.getOpenid());
                        // 刷新优惠券缓存
                        try {
                            rabbitMQPublisher.publish("class");
                        } catch (ConnectException e) {
                            logger.error(e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void sendCustomerMsg(String openId) {
        Profile profile = accountService.getProfile(openId);
        //先发文字,后发图片
        customerMessageService.sendCustomerMessage(openId, "Hi，" + profile.getNickname() + "，" +
                        "你已领取限免课程：找到本质问题，减少无效努力\n\n如需继续学习，请点击下方按钮“上课啦”\n\n" +
                        "\uD83D\uDC47送你一张专属知识卡+30个好友免费学习名额，立即分享出去吧！",
                Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        BufferedImage bufferedImage = cardRepository.loadDefaultCardImg(profile);
        if (bufferedImage != null) {
            // 发送图片消息
            String path = TEMP_IMAGE_PATH + CommonUtils.randomString(10) + profile.getId() + ".jpg";
            String mediaId = uploadResourceService.uploadResource(bufferedImage, path);
            customerMessageService.sendCustomerMessage(openId, mediaId,
                    Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        }
    }

    /**
     * 发送成功推广信息
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessOrderMsg(String targetOpenId, String orderOpenId, Integer remainCount) {
        Profile profile = accountService.getProfile(orderOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免小课《找到本质问题，减少无效努力》\n"));
        data.put("keyword1", new TemplateMessage.Keyword("知识传播大使召集令"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        data.put("remark", new TemplateMessage.Keyword("\n感谢你对优质内容传播做出的贡献，距离50元优惠券还有" + remainCount + "个好友啦！"));
        templateMessageService.sendMessage(templateMessage);
    }

    private void sendNormalSuccessOrderMsg(String targetOpenId, String orderOpenId) {
        Profile profile = accountService.getProfile(orderOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getShareCodeSuccessMsg());
        data.put("first", new TemplateMessage.Keyword("太棒了！" + profile.getNickname() + "通过你分享的卡片，学习了限免小课《找到本质问题，减少无效努力》\n"));
        data.put("keyword1", new TemplateMessage.Keyword("知识传播大使召集令"));
        data.put("keyword2", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("keyword3", new TemplateMessage.Keyword("【圈外同学】服务号"));
        data.put("remark", new TemplateMessage.Keyword("\n感谢你对优质内容传播做出的贡献！"));
        templateMessageService.sendMessage(templateMessage);
    }

    /**
     * 发送获得优惠券信息
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessPromotionMsg(String targetOpenId) {
        Profile profile = accountService.getProfile(targetOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getReceiveCouponMsg());
        data.put("first", new TemplateMessage.Keyword("恭喜！你已将优质内容传播给" + successNum + "位好友，成功get一张¥50代金券\n"));
        data.put("keyword1", new TemplateMessage.Keyword(profile.getNickname()));
        data.put("keyword2", new TemplateMessage.Keyword("¥50代金券"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateToString(new Date())));
        data.put("remark", new TemplateMessage.Keyword("\n点击下方“上课啦”并升级会员/报名小课，立即使用代金券，开学！"));
        templateMessageService.sendMessage(templateMessage);
    }

    @Override
    public Boolean hasGetTheCoupon(Integer profileId) {
        List<Coupon> coupons = couponDao.loadByProfileId(profileId);
        Long operationCouponCount = coupons.stream().filter(coupon -> coupon.getAmount().equals(50) && coupon.getDescription().equals("奖学金")).count();
        return operationCouponCount > 0;
    }

}
