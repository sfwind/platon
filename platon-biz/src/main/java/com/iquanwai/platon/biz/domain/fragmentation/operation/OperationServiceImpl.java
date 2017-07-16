package com.iquanwai.platon.biz.domain.fragmentation.operation;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionLevelDao;
import com.iquanwai.platon.biz.dao.fragmentation.PromotionUserDao;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.PromotionLevel;
import com.iquanwai.platon.biz.po.PromotionUser;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.krb5.Config;

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
    private ProfileDao profileDao;
    @Autowired
    private CouponDao couponDao;

    // 活动前缀
    private static String prefix = "freeLimit";
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
            String source = orderUser.getSource();
            // 查看推广人当前所有推广的新人列表
            List<PromotionUser> newUsers = promotionUserDao.loadUsersBySource(source);
            List<PromotionUser> successUsers = newUsers.stream().filter(user -> user.getAction() > 0).collect(Collectors.toList());

            // 发送推广成功消息
            Integer sourceProfileId = Integer.parseInt(orderUser.getSource().substring(prefix.length())); // 推广人 ProfileId
            Profile sourceProfile = profileDao.load(Profile.class, sourceProfileId); // 推广人 Profile
            // TODO 拿到优惠券之后是否还继续发送选课消息
            if(successUsers.size() <= successNum) {
                sendSuccessOrderMsg(sourceProfile.getOpenid());
            }
            if (successUsers.size() == successNum) {
                // 发送优惠券，Coupon 表新增数据
                Coupon coupon = new Coupon();
                coupon.setOpenId(sourceProfile.getOpenid());
                coupon.setProfileId(sourceProfile.getId());
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
     * 发送成功推广信息
     *
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessOrderMsg(String targetOpenId) {
        targetOpenId = "o-Es21SqXvnCmmaSQeiMZULoBRt8"; // TODO
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        // TODO 修改 msg 模板 id
        templateMessage.setTemplate_id(ConfigUtils.courseStartMsg());
        data.put("first", new TemplateMessage.Keyword("你好，这是测试订单成功"));
        data.put("keyword1", new TemplateMessage.Keyword("你好，这是测试订单成功"));
        data.put("keyword2", new TemplateMessage.Keyword("你好，这是测试订单成功"));
        data.put("remark", new TemplateMessage.Keyword("你好，这是测试订单成功"));
        templateMessageService.sendMessage(templateMessage);
    }

    /**
     * 发送获得优惠券信息
     *
     * @param targetOpenId 目标用户 openId
     */
    private void sendSuccessPromotionMsg(String targetOpenId) {
        targetOpenId = "o-Es21SqXvnCmmaSQeiMZULoBRt8"; // TODO
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setTouser(targetOpenId);
        Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
        templateMessage.setData(data);
        // TODO 修改 msg 模板 id
        templateMessage.setTemplate_id(ConfigUtils.courseStartMsg());
        data.put("first", new TemplateMessage.Keyword("你好，这是测试获得优惠券"));
        data.put("keyword1", new TemplateMessage.Keyword("你好，这是测试获得优惠券"));
        data.put("keyword2", new TemplateMessage.Keyword("你好，这是测试获得优惠券"));
        data.put("remark", new TemplateMessage.Keyword("你好，这是测试获得优惠券"));
        templateMessageService.sendMessage(templateMessage);
    }

}
