package com.iquanwai.platon.biz.domain.weixin.account;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.*;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.dao.wx.RegionDao;
import com.iquanwai.platon.biz.domain.common.message.SMSDto;
import com.iquanwai.platon.biz.domain.common.message.ShortMessageService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.*;
import com.iquanwai.platon.biz.util.*;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQFactory;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQPublisher;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/8/10.
 */
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    public RestfulHelper restfulHelper;
    @Autowired
    private FollowUserDao followUserDao;
    @Autowired
    private RegionDao regionDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private PointRepo pointRepo;
    @Autowired
    private ShortMessageService shortMessageService;
    @Autowired
    private SMSValidCodeDao smsValidCodeDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private CustomerStatusDao customerStatusDao;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private SubscribePushDao subscribePushDao;
    @Autowired
    private CourseScheduleDao courseScheduleDao;
    @Autowired
    private GroupPromotionDao groupPromotionDao;
    @Autowired
    private PrizeCardDao prizeCardDao;
    @Autowired
    private RabbitMQFactory rabbitMQFactory;

    private List<Region> provinceList;
    private List<Region> cityList;
    private Map<String, Integer> userRoleMap = Maps.newHashMap();
    private static final String SUBSCRIBE_PUSH_PREFIX = "subscribe_push_";
    // 用户头像失效校验 publisher
    private RabbitMQPublisher headImgUrlCheckPublisher;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);
        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole -> userRoleMap.put(userRole.getOpenid(), userRole.getRoleId()));
        logger.info("role init complete");
        headImgUrlCheckPublisher = rabbitMQFactory.initFanoutPublisher("profile_headImgUrl_check");
    }

    @Override
    public Account getAccount(String openid, boolean realTime) throws NotFollowingException {
        if (realTime) {
            return getAccountFromWeixin(openid);
        } else {
            //先从数据库查询account对象
            Account account = followUserDao.queryByOpenid(openid);
            if (account != null) {
                if (account.getSubscribe() == 0) {
                    // 曾经关注，现在取关的人
                    throw new NotFollowingException();
                }
                return account;
            }
            //从微信处获取
            return getAccountFromWeixin(openid);
        }
    }

    @Override
    public UserRole getUserRole(Integer profileId) {
        List<UserRole> userRoles = userRoleDao.getRoles(profileId);
        return userRoles.size() > 0 ? userRoles.get(0) : null;
    }

    @Override
    public Profile getProfileByRiseId(String riseId) {
        Profile profile = profileDao.queryByRiseId(riseId);

        if (profile != null) {
            profile.setRiseMember(riseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }

        return profile;
    }

    @Override
    public Profile getProfile(String openid) {
        Profile profile = getProfileFromDB(openid);
        // checkHeadImgUrlEffectiveness(profile);
        return profile;
    }

    @Override
    public Profile getProfile(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile != null) {
            profile.setRiseMember(riseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
            // checkHeadImgUrlEffectiveness(profile);
        }
        return profile;
    }

    @Override
    public List<Profile> getProfiles(List<Integer> profileIds) {
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        profiles.forEach(profile -> {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            profile.setRiseMember(riseMember(profile.getId()));
            Integer role = userRoleMap.get(profile.getOpenid());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
            // checkHeadImgUrlEffectiveness(profile);
        });
        return profiles;
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);

        if (profile != null) {
            profile.setRiseMember(riseMember(profile.getId()));
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }

        return profile;
    }

    @Override
    public Account getGuestFromWeixin(String openId, String accessToken) {
        String url = GUEST_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openId);
        map.put("access_token", accessToken);
        logger.info("请求游客信息:{}", openId);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求游客信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account account = new Account();
        try {
            BeanUtils.populate(account, result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return account;
    }

    private Account getAccountFromWeixin(String openid) throws NotFollowingException {
        //调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        logger.info("请求用户信息:{}", openid);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}", body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account accountNew = new Account();
        try {
            ConvertUtils.register((aClass, value) -> {
                if (value == null) {
                    return null;
                }

                if (!(value instanceof Double)) {
                    logger.error("不是日期类型");
                    throw new ConversionException("不是日期类型");
                }
                Double time = (Double) value * 1000;

                return new DateTime(time.longValue()).toDate();
            }, Date.class);

            BeanUtils.populate(accountNew, result);
            if (accountNew.getSubscribe() != null && accountNew.getSubscribe() == 0) {
                //未关注直接抛异常
                throw new NotFollowingException();
            }
            redisUtil.lock("lock:wx:user:insert", (lock) -> {
                Account finalQuery = followUserDao.queryByOpenid(openid);
                if (finalQuery == null) {
                    if (accountNew.getNickname() != null) {
                        logger.info("插入用户信息:{}", accountNew);
                        followUserDao.insert(accountNew);
                        // 插入profile表
                        Profile profile = getProfileFromDB(accountNew.getOpenid());
                        if (profile == null) {
                            try {
                                ModelMapper modelMapper = new ModelMapper();
                                profile = modelMapper.map(accountNew, Profile.class);
                                logger.info("插入Profile表信息:{}", profile);
                                profile.setRiseId(CommonUtils.randomString(7));
                                profileDao.insertProfile(profile);
                            } catch (SQLException err) {
                                profile.setRiseId(CommonUtils.randomString(7));
                                try {
                                    profileDao.insertProfile(profile);
                                } catch (SQLException subErr) {
                                    logger.error("插入Profile失败，openId:{},riseId:{}", profile.getOpenid(), profile.getRiseId());
                                }
                            }
                        }
                    }
                } else {
                    if (accountNew.getNickname() != null) {
                        followUserDao.updateMeta(accountNew);
                    }
                }
            });
        } catch (NotFollowingException e1) {
            throw new NotFollowingException();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return accountNew;
    }

    @Override
    public List<Region> loadAllProvinces() {
        if (provinceList == null) {
            provinceList = regionDao.loadAllProvinces();
        }
        return provinceList;
    }

    @Override
    public List<Region> loadCities() {
        if (cityList == null) {
            cityList = regionDao.loadAllCities();
        }
        return cityList;
    }

    @Override
    public int updateOpenNavigator(Integer id) {
        return profileDao.updateOpenNavigator(id);
    }

    @Override
    public int updateOpenRise(Integer id) {
        return profileDao.updateOpenRise(id);
    }

    @Override
    public int updateOpenApplication(Integer id) {
        return profileDao.updateOpenApplication(id);
    }

    @Override
    public int updateOpenConsolidation(Integer id) {
        return profileDao.updateOpenConsolidation(id);
    }

    @Override
    public int updateOpenWelcome(Integer id) {
        return profileDao.updateOpenWelcome(id);
    }

    @Override
    public void submitPersonalCenterProfile(Profile profile) {
        Assert.notNull(profile, "profile 不能为空");
        Assert.notNull(profile.getId(), "profileId 不能为空");
        Profile oldProfile = profileDao.load(Profile.class, profile.getId());
        Boolean result;
        if (profile.getAddress() != null) {
            result = profileDao.submitPersonalCenterProfileWithMoreDetail(profile);
        } else {
            result = profileDao.submitPersonalCenterProfile(profile);
        }
        if (result && oldProfile.getIsFull() == 0) {
            logger.info("用户:{} 完成个人信息填写,加{}积分", profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 第一次提交，加分
            pointRepo.riseCustomerPoint(profile.getId(), ConfigUtils.getProfileFullScore());
            // 更新信息状态
            profileDao.completeProfile(profile.getId());
        }
    }

    @Override
    public void submitCertificateProfile(Profile profile) {
        profileDao.submitCertificateProfile(profile);
    }

    @Override
    public void reloadRegion() {
        provinceList = regionDao.loadAllProvinces();
        cityList = regionDao.loadAllCities();
    }

    @Override
    public Region loadProvinceByName(String name) {
        Region result = null;
        if (provinceList != null) {
            for (Region province : provinceList) {
                if (StringUtils.equals(province.getName(), name)) {
                    result = province;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Region loadCityByName(String name) {
        Region result = null;
        if (cityList != null) {
            for (Region city : cityList) {
                if (StringUtils.equals(city.getName(), name)) {
                    result = city;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Role getRole(Integer profileId) {
        List<UserRole> userRoles = userRoleDao.getRoles(profileId);
        if (CollectionUtils.isEmpty(userRoles)) {
            return null;
        } else {
            Integer roleId = userRoles.get(0).getRoleId();
            return userRoleDao.load(Role.class, roleId);
        }
    }

    @Override
    public Pair<Boolean, String> sendValidCode(String phone, Integer profileId, String areaCode) {
        if (!shortMessageService.canSend(profileId)) {
            return new ImmutablePair<>(false, "操作太频繁，请稍候再试");
        }
        SMSDto smsDto = new SMSDto();
        //拼接区号不加中国区号
        if (areaCode != null && !"86".equals(areaCode)) {
            //首位去0,补+号
            phone = "+" + StringUtils.removeStart(areaCode, "0") + phone;
        }
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile == null) {
            return new ImmutablePair<>(false, "系统错误,请联系小Q");
        }
        smsDto.setPhone(phone);
        smsDto.setProfileId(profileId);
        smsDto.setType(SMSDto.NORMAL);
        String code = CommonUtils.randomNumber(4);
        smsDto.setContent("验证码:" + code + "，请在30分钟内完成验证。");
        //插入验证码
        SMSValidCode SMSValidCode = new SMSValidCode(smsDto, code, Constants.ValidCode.MOBILE_VALID);
        smsValidCodeDao.insert(SMSValidCode);
        return shortMessageService.sendShortMessage(smsDto);
    }

    @Override
    public boolean validCode(String code, Integer profileId) {
        SMSValidCode smsValidCode = smsValidCodeDao.loadValidCode(profileId);

        if (smsValidCode == null) {
            return false;
        }
        //过期校验
        if (smsValidCode.getExpiredTime().before(new Date())) {
            return false;
        }

        if (smsValidCode.getCode().equals(code)) {
            profileDao.updateMobile(smsValidCode.getPhone(), profileId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean isRiseMember(Integer profileId) {
        return riseMemberDao.loadValidRiseMember(profileId) != null;
    }

    @Override
    public List<Coupon> loadCoupons(Integer profileId) {
        List<Coupon> targetCoupons = Lists.newArrayList();
        // 过滤出未使用并且未过期的优惠券
        List<Coupon> sourceCoupons = couponDao.loadByProfileId(profileId);
        for (Coupon coupon : sourceCoupons) {
            Coupon tempCoupon = new Coupon();
            tempCoupon.setAmount(coupon.getAmount());
            tempCoupon.setExpiredDateString(DateUtils.parseDateToFormat5(DateUtils.beforeDays(coupon.getExpiredDate(), 1)));
            tempCoupon.setDescription(coupon.getDescription());
            targetCoupons.add(tempCoupon);
        }
        return targetCoupons;
    }

    @Override
    public RiseClassMember loadDisplayRiseClassMember(Integer profileId) {
        RiseClassMember activeRiseClassMember = riseClassMemberDao.loadActiveRiseClassMember(profileId);
        if (activeRiseClassMember == null) {
            activeRiseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
        }
        return activeRiseClassMember;
    }

    @Override
    public Boolean openLearningNotify(Integer profileId) {
        return profileDao.updateLearningNotifyStatus(profileId, 1);
    }

    @Override
    public Boolean closeLearningNotify(Integer profileId) {
        return profileDao.updateLearningNotifyStatus(profileId, 0);
    }

    @Override
    public Boolean hasStatusId(Integer profileId, Integer statusId) {
        return customerStatusDao.load(profileId, statusId) != null;
    }

    @Override
    public Boolean addStatusId(Integer profileId, Integer statusId) {
        CustomerStatus status = customerStatusDao.load(profileId, statusId);
        if (status == null) {
            return customerStatusDao.insert(profileId, statusId) > 0;
        } else {
            return true;
        }
    }

    @Override
    public List<RiseMember> loadAllRiseMembersByProfileId(Integer profileId) {
        return riseMemberDao.loadRiseMembersByProfileId(profileId);
    }

    private Integer riseMember(Integer profileId) {
        RiseMember riseMember = riseMemberDao.loadValidRiseMember(profileId);
        if (riseMember == null) {
            return 0;
        }
        Integer memberTypeId = riseMember.getMemberTypeId();
        if (memberTypeId == null) {
            return 0;
        }
        // 精英或者专业版用户
        if (memberTypeId == RiseMember.HALF || memberTypeId == RiseMember.ANNUAL || memberTypeId == RiseMember.ELITE || memberTypeId == RiseMember.HALF_ELITE) {
            return 1;
            // 训练营用户
        } else if (memberTypeId == RiseMember.CAMP) {
            return 3;
            // 课程单买用户
        } else if (memberTypeId == RiseMember.COURSE) {
            return 2;
        }

        return 0;
    }

    @Override
    public String createSubscribePush(String openid, String callback, String scene) {
        Integer result = subscribePushDao.insert(openid, callback, scene);
        String sceneCode = SUBSCRIBE_PUSH_PREFIX + result;
        return qrCodeService.loadQrBase64(sceneCode);
    }

    @Override
    public SubscribePush loadSubscribePush(Integer id) {
        return subscribePushDao.loadById(id);
    }

    @Override
    public Integer loadUserScheduleCategory(Integer profileId) {
        CourseSchedule courseSchedule = courseScheduleDao.loadOldestCourseSchedule(profileId);
        if (courseSchedule != null) {
            return courseSchedule.getCategory();
        }
        // 老用户
        CustomerStatus status = customerStatusDao.load(profileId, CustomerStatus.SCHEDULE_LESS);
        if (status != null) {
            return CourseScheduleDefault.CategoryType.OLD_STUDENT;
        } else {
            return CourseScheduleDefault.CategoryType.NEW_STUDENT;
        }

    }

    @Override
    public void updateWeixinId(Integer profileId, String weixinId) {
        profileDao.updateWeixinId(profileId, weixinId);
    }

    @Override
    public RiseMember getValidRiseMember(Integer profileId) {
        return riseMemberDao.loadValidRiseMember(profileId);
    }

    @Override
    public boolean isPreviewNewUser(Integer profileId) {
        //判断是否参加过商学院和训练营
        if (riseMemberDao.loadRiseMembersByProfileId(profileId).size() > 0) {
            return false;
        }
        //判断是否参加"一带二"活动
        if (groupPromotionDao.loadByProfileId(profileId) != null) {
            return false;
        }
        //判断是否领取过礼品卡
        if (prizeCardDao.loadAnnualCardByReceiver(profileId) != null) {
            return false;
        }
        return true;
    }

    // 生成用来发送更新 mq 的信息
    private void checkHeadImgUrlEffectiveness(Profile profile) {
        if (profile == null) {
            return;
        }
        Integer profileId = profile.getId();
        String headImgUrl = profile.getHeadimgurl();
        Date headImgUrlCheckTime = profile.getHeadImgUrlCheckTime();
        if (headImgUrl.indexOf("wx.qlogo.cn") > 0 && (headImgUrlCheckTime == null || DateUtils.interval(headImgUrlCheckTime) >= 7)) {
            JSONObject json = new JSONObject();
            json.put("profileId", profileId);
            json.put("openId", profile.getOpenid());
            json.put("headImgUrl", headImgUrl);
            try {
                headImgUrlCheckPublisher.publish(json.toString());
            } catch (ConnectException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }
}

