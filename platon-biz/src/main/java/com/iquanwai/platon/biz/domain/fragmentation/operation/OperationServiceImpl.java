package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.EssenceCardDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionUserDao;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRResponse;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Created by xfduan on 2017/7/14.
 */
@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private PromotionUserDao promotionUserDao;
    @Autowired
    private PromotionLevelDao promotionLevelDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private CouponDao couponDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    // 活动前缀
    private static String prefix = "freeLimit_";
    // 推广成功人数限额
    private static Integer successNum = 9;

    @Override
    public void recordPromotionLevel(String openId, String scene) {
        PromotionLevel tempPromotionLevel = promotionLevelDao.loadByOpenId(openId);
        if (!scene.contains(prefix) || tempPromotionLevel != null) return; // 不是本次活动，或者说已被其他用户推广则不算新人

        String source = scene.substring(prefix.length());
        if ("RISE".equals(source)) {
            promotionLevelDao.insertPromotionLevel(openId, 1);
        } else {
            Integer promotionProfileId = Integer.parseInt(source);
            Profile promotionProfile = profileDao.load(Profile.class, promotionProfileId);
            String promotionOpenId = promotionProfile.getOpenid(); // 推广人的 OpenId
            PromotionLevel promotionLevelObject = promotionLevelDao.loadByOpenId(promotionOpenId); // 推广人层级表对象
            if (promotionLevelObject != null) {
                Integer promotionLevel = promotionLevelObject.getLevel(); // 推广人所在推广层级
                promotionLevelDao.insertPromotionLevel(openId, promotionLevel + 1);
            } else {
                promotionLevelDao.insertPromotionLevel(openId, 1);
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
            // 发送推广成功消息
            // 获取的是来源的 profileId (推广人的 profileId)
            Integer sourceProfileId = orderUser.getProfileId();
            if (sourceProfileId == null) return;
            Profile sourceProfile = profileDao.load(Profile.class, sourceProfileId); // 推广人 Profile
            // TODO 拿到优惠券之后是否还继续发送选课消息
            if (successUsers.size() <= successNum) {
                sendSuccessOrderMsg(sourceProfile.getOpenid(), openId, successNum - successUsers.size());
            }
            if (successUsers.size() == successNum) {
                // 发送优惠券，Coupon 表新增数据
                Coupon coupon = new Coupon();
                coupon.setOpenId(sourceProfile.getOpenid());
                coupon.setProfileId(sourceProfile.getId());
                // TODO 优惠券金额、过期时间
                coupon.setAmount(50);
                coupon.setExpiredDate(DateUtils.parseStringToDateTime("2018-01-01 00:00:00"));
                coupon.setDescription("限免推广券");
                Integer insertResult = couponDao.insertCoupon(coupon);
                if (insertResult > 0) {
                    // 礼品券数据保存成功，发送获得优惠券的模板消息
                    sendSuccessPromotionMsg(sourceProfile.getOpenid());
                }
            }
        }
    }

    /**
     * 获取小课精华卡
     */
    @Override
    public String loadEssenceCard(Integer profileId, Integer problemId, Integer knowledgeId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        BufferedImage targetImage = ImageUtils.getBufferedImageByUrl("https://static.iqycamp.com/images/fragment/free_limit_test.jpg?imageslim");
        String headImgUrl = profile.getHeadimgurl();
        if (headImgUrl == null) {
            // 如果用户头像为空，则拉取实时新头像
            Profile realProfile = accountService.getProfile(profile.getOpenid(), true);
            headImgUrl = realProfile.getHeadimgurl();
        }
        targetImage = ImageUtils.scaleByPercentage(targetImage, 338, 600);
        QRResponse response = qrCodeService.generateTemporaryQRCode("RISE30", null);
        BufferedImage qrImage = null;
        try {
            qrImage = ImageIO.read(qrCodeService.showQRCode(response.getTicket()));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        qrImage = ImageUtils.scaleByPercentage(qrImage, 90, 90);
        targetImage = ImageUtils.overlapImage(targetImage, qrImage, 150, 150);

        BufferedImage headImg = ImageUtils.convertCircular(ImageUtils.getBufferedImageByUrl(headImgUrl));
        headImg = ImageUtils.scaleByPercentage(headImg, 60, 60);
        targetImage = ImageUtils.overlapImage(targetImage, headImg, 10, 10);
        targetImage = ImageUtils.writeText(targetImage, 60, 10, profile.getNickname(),
                new Font("微软雅黑", Font.BOLD, 24), new Color(102, 102, 102));
        targetImage = ImageUtils.writeText(targetImage, 40, 50, "理清问题需求，澄清偏差",
                new Font("微软雅黑", Font.BOLD, 24), new Color(51, 51, 51));

        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(targetImage, "png", outputStream);
            String imagePath = "/Users/xfduan/Pictures/a.png";
            ImageIO.write(targetImage, imagePath.substring(imagePath.lastIndexOf(".") + 1), new File(imagePath));
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return "data:image/png;base64," + encoder.encode(outputStream.toByteArray());
    }

    /**
     * 发送成功推广信息
     *
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessOrderMsg(String targetOpenId, String orderOpenId, Integer remainCount) {
        Profile profile = profileDao.queryByOpenId(orderOpenId);
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

    /**
     * 发送获得优惠券信息
     *
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessPromotionMsg(String targetOpenId) {
        Profile profile = profileDao.queryByOpenId(targetOpenId);
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        templateMessage.setTemplate_id(ConfigUtils.getReceiveCouponMsg());
        data.put("first", new TemplateMessage.Keyword("恭喜！你已将优质内容传播给9位好友，成功get一张¥50代金券\n"));
        data.put("keyword1", new TemplateMessage.Keyword(profile.getNickname()));
        data.put("keyword2", new TemplateMessage.Keyword("¥50代金券"));
        data.put("remark", new TemplateMessage.Keyword("\n点击下方“圈外同学”并升级会员/报名小课，立即使用代金券，开学！"));
        templateMessageService.sendMessage(templateMessage);
    }

}
