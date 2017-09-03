package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.fragmentation.CouponDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseCertificateDao;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessage;
import com.iquanwai.platon.biz.domain.weixin.message.TemplateMessageService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.NumberToHanZi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 17/8/29.
 */
@Service
public class CertificateServiceImpl implements CertificateService {
    @Autowired
    private RiseCertificateDao riseCertificateDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TemplateMessageService templateMessageService;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private CouponDao couponDao;

    //优秀学员,优秀团队奖励积分
    private static final int PRIZE_POINT = 200;

    //优秀班长优惠券
    private static final int PRIZE_COUPON_CLASS_LEADER = 200;

    //优秀组长优惠券
    private static final int PRIZE_COUPON_GROUP_LEADER = 100;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RiseCertificate getCertificate(String certificateNo) {
        RiseCertificate riseCertificate = riseCertificateDao.loadByCertificateNo(certificateNo);
        buildDetail(riseCertificate);
        //删除profileId
        riseCertificate.setProfileId(null);
        return riseCertificate;
    }

    @Override
    public void sendCertificate(Integer year, Integer month) {
        List<RiseCertificate> certificateList = riseCertificateDao.loadByMonthAndYear(year, month);
        certificateList.forEach(riseCertificate -> {
            Integer type = riseCertificate.getType();
            TemplateMessage templateMessage = new TemplateMessage();
            Profile profile = accountService.getProfile(riseCertificate.getProfileId());
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());
            templateMessage.setUrl(
                    ConfigUtils.domainName() + "/rise/static/customer/certificate/profile?certificateNo="
                            + riseCertificate.getCertificateNo());
            buildGraduateMessage(riseCertificate, type, templateMessage, profile);
            //发送毕业消息
            templateMessageService.sendMessage(templateMessage);

            templateMessage = new TemplateMessage();
            templateMessage.setTouser(profile.getOpenid());
            templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());
            templateMessage.setUrl("https://shimo.im/doc/3kL94FYajYgls0Zx?r=GQ373Y/");
            //发送优惠券信息
            sendCouponMessage(riseCertificate, type, templateMessage, profile);
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
                "您的" + riseCertificate.getMonth() + "月小课训练营" + amount + "元优惠券奖励已到账"));
        data.put("keyword2", new TemplateMessage.Keyword("点击详情，了解优惠券领取位置及使用方式"));
        data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));

        Coupon coupon = new Coupon();
        coupon.setOpenId(profile.getOpenid());
        coupon.setProfileId(profile.getId());
        coupon.setDescription(description);
        coupon.setUsed(0);
        coupon.setExpiredDate(DateUtils.parseStringToDate("2099-01-01"));
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

                    data.put("keyword1", new TemplateMessage.Keyword(
                            "恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月小课训练营优秀班长"));
                    data.put("keyword2", new TemplateMessage.Keyword("点击详情，领取优秀班长荣誉证书"));
                    data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀班长的同学，除了荣誉证书外，还将获得圈外200元优惠券（领取及使用方式，我们在以下给您发了另一条通知，专门介绍啦）" +
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

                    data.put("keyword1", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月小课训练营优秀组长"));
                    data.put("keyword2", new TemplateMessage.Keyword("点击详情，领取优秀组长荣誉证书"));
                    data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀组长的同学，除了荣誉证书外，还将获得圈外100元优惠券（领取及使用方式，我们在以下给您发了另一条通知，专门介绍啦）" +
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

                    data.put("keyword1", new TemplateMessage.Keyword("恭喜您荣膺［圈外同学］" + riseCertificate.getMonth() + "月小课训练营优秀学员"));
                    data.put("keyword2", new TemplateMessage.Keyword("点击详情，领取优秀学员荣誉证书"));
                    data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀学员的同学，除了荣誉证书外，您还将额外获得200个积分（积分已存入您的［圈外同学］－［我的］－［我的小课］－［总积分］）" +
                            "\n\n" +
                            "此外，如果您在本次会员期内，\n" +
                            "累计3次荣膺优秀学员：成为助教资格＋圈外同学奖学金\n" +
                            "累计6次荣膺优秀学员：圈圈1V1半小时咨询"));

                    pointRepo.riseCustomerPoint(profile.getId(), PRIZE_POINT);
                } catch (Exception e) {
                    logger.error(riseCertificate.getProfileId() + " 发送证书失败", e);
                }
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                try {
                    Map<String, TemplateMessage.Keyword> data = Maps.newHashMap();
                    templateMessage.setData(data);
                    templateMessage.setTemplate_id(ConfigUtils.incompleteTaskMsg());

                    data.put("keyword1", new TemplateMessage.Keyword("恭喜您所在的小组荣膺［圈外同学］" + riseCertificate.getMonth() + "月小课训练营优秀团队"));
                    data.put("keyword2", new TemplateMessage.Keyword("点击详情，领取优秀团队荣誉证书"));
                    data.put("keyword3", new TemplateMessage.Keyword(DateUtils.parseDateTimeToString(new Date())));
                    data.put("remark", new TemplateMessage.Keyword("\n被评为优秀团队的小组，除了荣誉证书外，小组内的每位小伙伴还将额外获得200个积分（积分已存入您的［圈外同学］－［我的］－［我的小课］－［总积分］）"));

                    pointRepo.riseCustomerPoint(profile.getId(), PRIZE_POINT);
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
                        riseCertificate.getMonth() + "月小课训练营中担任班长一职，表现突出，荣膺\"优秀班长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.CLASS_LEADER);
                break;
            case Constants.CERTIFICATE.TYPE.GROUP_LEADER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中担任组长一职，表现优异，荣膺\"优秀班长\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.GROUP_LEADER);
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_MEMBER:
                riseCertificate.setName(profile.getRealName());
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中成绩名列前茅，荣膺\"优秀学员\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_MEMBER);
                break;
            case Constants.CERTIFICATE.TYPE.SUPERB_GROUP:
                String monthStr = NumberToHanZi.formatInteger(riseCertificate.getMonth());
                String groupNoStr = NumberToHanZi.formatInteger(riseCertificate.getGroupNo());
                riseCertificate.setName(monthStr + "月小课" + groupNoStr + "组");
                riseCertificate.setCongratulation("在【圈外同学】" + riseCertificate.getYear() + "年" +
                        riseCertificate.getMonth() + "月小课训练营中小组表现优异，荣膺\"优秀小组\"称号" +
                        "\n\n" +
                        "特发此证，以资鼓励");
                riseCertificate.setTypeName(Constants.CERTIFICATE.NAME.SUPERB_GROUP);
                break;
        }
    }
}
