package com.iquanwai.platon.biz.domain.weixin.account;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.*;
import com.iquanwai.platon.biz.dao.fragmentation.CourseScheduleDao;
import com.iquanwai.platon.biz.dao.fragmentation.ImprovementPlanDao;
import com.iquanwai.platon.biz.dao.user.UserInfoDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.dao.wx.RegionDao;
import com.iquanwai.platon.biz.domain.common.message.SMSDto;
import com.iquanwai.platon.biz.domain.common.message.ShortMessageService;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointManager;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.CourseSchedule;
import com.iquanwai.platon.biz.po.CourseScheduleDefault;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.common.*;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private UserRoleDao userRoleDao;
    @Autowired
    private PointManager pointManager;
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
    private ImprovementPlanDao improvementPlanDao;
    @Autowired
    private UserInfoDao userInfoDao;


    private List<Region> provinceList;
    private List<Region> cityList;
    private Map<Integer, Integer> userRoleMap = Maps.newHashMap();
    private static final String SUBSCRIBE_PUSH_PREFIX = "subscribe_push_";

    private Logger logger = LoggerFactory.getLogger(getClass());


    @Override
    public boolean initUserByUnionId(String unionId, Boolean realTime) {
        String requestUrl = "http://" + ConfigUtils.getInternalIp() + ":" + ConfigUtils.getInternalPort() + "/internal/init/user?unionId=" + unionId + "&realTime=" + realTime;
        String body = restfulHelper.getPure(requestUrl);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        String code = result.get("code").toString();
        return "200".equals(code);
    }

    @Override
    public boolean checkIsSubscribe(String openId, String unionId) {
        Account account = followUserDao.queryByUnionId(unionId);
        if (account != null && account.getSubscribe() == 1) {
            return true;
        } else {
            String requestUrl = "http://" + ConfigUtils.getInternalIp() + ":" + ConfigUtils.getInternalPort() + "/internal/user/subscribe?openId=" + openId;
            String body = restfulHelper.getPure(requestUrl);
            JSONObject jsonObject = JSONObject.parseObject(body);
            Integer code = jsonObject.getInteger("code");
            Integer subscribe = jsonObject.getInteger("msg");
            logger.info(code + "");
            logger.info(subscribe + "");
            if (200 == code) {
                return subscribe != null && subscribe == 1;
            }
        }
        return false;
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
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }
        return profile;
    }

    @Override
    public Profile getProfileByUnionId(String unionId) {
        Profile profile = profileDao.queryByUnionId(unionId);
        if (profile != null) {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
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
        return getProfileFromDB(openid);
    }

    @Override
    public Profile getProfile(Integer profileId) {
        Profile profile = profileDao.load(Profile.class, profileId);
        if (profile != null) {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
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
            // NOTE:批量接口不注入risemember
            // profile.setRiseMember(getProfileRiseMember(profile.getId()));
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        });
        return profiles;
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);

        if (profile != null) {
            if (profile.getHeadimgurl() != null) {
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:", "https:"));
            }
            Integer role = userRoleMap.get(profile.getId());
            if (role == null) {
                profile.setRole(0);
            } else {
                profile.setRole(role);
            }
        }

        return profile;
    }

    @Override
    public Account getAccountByUnionId(String unionId) {
        return followUserDao.queryByUnionId(unionId);
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
    public void submitPersonalCenterProfile(Profile profile, UserInfo userInfo) {
        Assert.notNull(profile, "profile 不能为空");
        Profile oldProfile = profileDao.load(Profile.class, profile.getId());

        if (profile.getMarried() != null) {
            oldProfile.setMarried(profile.getMarried());
        }
        if (profile.getProvince() != null) {
            oldProfile.setProvince(profile.getProvince());
        }
        if (profile.getCity() != null) {
            oldProfile.setCity(profile.getCity());
        }
        if (profile.getEmail() != null) {
            oldProfile.setEmail(profile.getEmail());
        }
        if (profile.getWeixinId() != null) {
            oldProfile.setWeixinId(profile.getWeixinId());
        }
        profileDao.submitPersonalCenterProfileWithMoreDetail(oldProfile);

        UserInfo existUserInfo = userInfoDao.loadByProfileId(profile.getId());
        if (existUserInfo == null) {
            //表示手机号没有通过检验，不进行加积分操作
            userInfoDao.insert(userInfo);
        } else {
            userInfoDao.update(userInfo);
            //判断之前信息是否已经填写完整
            //如果未完整，则增加积分和更新isFull
            if (existUserInfo.getIsFull() == 0 && userInfo.getRate() == 100 && existUserInfo.getMobile()!=null) {
                logger.info(profile.getId() + "首次信息填写完整，增加积分");
                pointManager.riseCustomerPoint(profile.getId(), ConfigUtils.getProfileFullScore());
                userInfoDao.updateIsFull(profile.getId());
            }
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
            List<ImprovementPlan> improvementPlans = improvementPlanDao.loadUserPlans(profileId);
            return improvementPlans.size() == 0 ? Role.stranger() : Role.student();
        } else {
            Integer roleId = userRoles.get(0).getRoleId();
            return userRoleDao.load(Role.class, roleId);
        }
    }

    @Override
    public UserRole getAssist(Integer profileId) {
        return userRoleDao.getAssist(profileId);
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
            UserInfo userInfo = userInfoDao.loadByProfileId(profileId);
            if (userInfo == null) {
                UserInfo insertUser = new UserInfo();
                insertUser.setProfileId(profileId);
                insertUser.setMobile(smsValidCode.getPhone());
                userInfoDao.insert(insertUser);
            } else {
                userInfoDao.updateMobile(smsValidCode.getPhone(), profileId);
            }
            return true;
        } else {
            return false;
        }
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
    public Boolean openLearningNotify(Integer profileId) {
        return profileDao.updateLearningNotifyStatus(profileId, 1);
    }

    @Override
    public Boolean closeLearningNotify(Integer profileId) {
        return profileDao.updateLearningNotifyStatus(profileId, 0);
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
        // 修复因为其他课表原因导致不能正常显示的问题
        CourseSchedule courseSchedule = courseScheduleDao.loadOldestCoreCourseSchedule(profileId);
        if (courseSchedule != null) {
            return courseSchedule.getCategory();
        }
        // 老用户
        CustomerStatus status = customerStatusDao.load(profileId, CustomerStatus.OLD_SCHEDULE);
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
    public List<Integer> getProfileIdsByMemberId(List<String> memberIds) {
        List<Profile> profiles = profileDao.queryAccountsByMemberIds(memberIds);
        List<Integer> profileIds = profiles.stream().map(Profile::getId)
                .distinct().collect(Collectors.toList());
        return profileIds;
    }


    @Override
    public String getOpenidByMemberId(String memberId) {
        Profile profile = profileDao.queryAccountByMemberId(memberId);
        if (profile == null) {
            return null;
        } else {
            return profile.getOpenid();
        }
    }

}

