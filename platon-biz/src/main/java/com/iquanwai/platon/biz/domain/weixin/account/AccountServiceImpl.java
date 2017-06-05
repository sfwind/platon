package com.iquanwai.platon.biz.domain.weixin.account;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.RedisUtil;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.dao.wx.RegionDao;
import com.iquanwai.platon.biz.domain.fragmentation.point.PointRepo;
import com.iquanwai.platon.biz.exception.NotFollowingException;
import com.iquanwai.platon.biz.po.common.Account;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.po.common.UserRole;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
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

    private List<Region> provinceList;

    private List<Region> cityList;
    @Autowired
    private UserRoleDao userRoleDao;

    private Map<String, Integer> userRoleMap = Maps.newHashMap();

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PointRepo pointRepo;

    @PostConstruct
    public void init(){
        List<UserRole> userRoleList = userRoleDao.loadAll(UserRole.class);

        userRoleList.stream().filter(userRole1 -> !userRole1.getDel()).forEach(userRole -> {
            userRoleMap.put(userRole.getOpenid(), userRole.getRoleId());
        });

        logger.info("role init complete");
    }

    public Account getAccount(String openid, boolean realTime) throws NotFollowingException {
        if(realTime){
            return getAccountFromWeixin(openid);
        }else{
            //先从数据库查询account对象
            Account account = followUserDao.queryByOpenid(openid);
            if(account != null) {
                return account;
            }
            //从微信处获取
            return getAccountFromWeixin(openid);
        }
    }

    @Override
    public Profile getProfile(String openid, boolean realTime){
        return getProfileFromDB(openid);
    }

    private Profile getProfileFromDB(String openid) {
        Profile profile = profileDao.queryByOpenId(openid);

        if(profile!=null) {
            if(profile.getHeadimgurl()!=null){
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:","https:"));
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
    public List<Profile> getProfiles(List<String> openid) {
        List<Profile> profiles = profileDao.queryAccounts(openid);
        profiles.stream().forEach(profile -> {
            if(profile.getHeadimgurl()!=null){
                profile.setHeadimgurl(profile.getHeadimgurl().replace("http:","https:"));
            }
            Integer role = userRoleMap.get(profile.getOpenid());
            if(role==null){
                profile.setRole(0);
            }else{
                profile.setRole(role);
            }
        });

        return profiles;
    }

    private Account getAccountFromWeixin(String openid) throws NotFollowingException {
        //调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        logger.info("请求用户信息:{}",openid);
        url = CommonUtils.placeholderReplace(url, map);

        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}",body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account accountNew = new Account();
        try {
            ConvertUtils.register((aClass, value) -> {
                if (value == null)
                    return null;

                if (!(value instanceof Double)) {
                    logger.error("不是日期类型");
                    throw new ConversionException("不是日期类型");
                }
                Double time = (Double) value * 1000;

                return new DateTime(time.longValue()).toDate();
            }, Date.class);

            BeanUtils.populate(accountNew, result);
            if(accountNew.getSubscribe() == 0){
                //未关注直接抛异常
                throw new NotFollowingException();
            }
            Account finalQuery = followUserDao.queryByOpenid(openid);
            if(finalQuery==null) {
                redisUtil.lock("lock:wx:user:insert", (lock) -> {
                    if (accountNew.getNickname() != null) {
                        logger.info("插入用户信息:{}", accountNew);
                        followUserDao.insert(accountNew);
                        try {
                            updateProfile(accountNew);
                        } catch (Exception e) {
                            logger.error(e.getLocalizedMessage(), e);
                        }
                    }
                });
            }else{
                logger.info("更新用户信息:{}",accountNew);
                if(accountNew.getNickname()!=null) {
                    followUserDao.updateMeta(accountNew);
                    updateProfile(accountNew);
                }
            }
        } catch (NotFollowingException e1){
            throw new NotFollowingException();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return accountNew;
    }

    @Override
    public List<Region> loadAllProvinces() {
        if(provinceList ==null){
            provinceList = regionDao.loadAllProvinces();
        }
        return provinceList;
    }

    @Override
    public List<Region> loadCities() {
        if(cityList==null) {
            cityList = regionDao.loadAllCities();
        }
        return cityList;
    }

    @Override
    public int updateOpenRise(String openId) {
       return profileDao.updateOpenRise(openId);
    }

    @Override
    public int updateOpenApplication(String openId){
        return profileDao.updateOpenApplication(openId);
    }

    @Override
    public int updateOpenConsolidation(String openId){
        return profileDao.updateOpenConsolidation(openId);
    }

    @Override
    public void submitPersonalCenterProfile(Profile profile) {
        Assert.notNull(profile.getOpenid(), "openID不能为空");
        Profile oldProfile = profileDao.queryByOpenId(profile.getOpenid());
        Boolean result = profileDao.submitPersonalCenterProfile(profile);
        if(result && oldProfile.getIsFull()==0){
            logger.info("用户:{} 完成个人信息填写,加{}积分",profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 第一次提交，加分
            pointRepo.riseCustomerPoint(profile.getOpenid(), ConfigUtils.getProfileFullScore());
            // 更新信息状态
            profileDao.completeProfile(profile.getOpenid());
        }
    }

    @Override
    public void reloadRegion(){
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

    private void updateProfile(Account accountNew) throws IllegalAccessException, InvocationTargetException {
        Profile profile = getProfileFromDB(accountNew.getOpenid());
        if(profile==null){
            profile = new Profile();
            try{
                BeanUtils.copyProperties(profile, accountNew);
                logger.info("插入Profile表信息:{}",profile);
                profile.setRiseId(CommonUtils.randomString(7));
                profileDao.insertProfile(profile);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("beanUtils copy props error",e);
            } catch (SQLException err){
                profile.setRiseId(CommonUtils.randomString(7));
                try{
                    profileDao.insertProfile(profile);
                } catch (SQLException subErr){
                    logger.error("插入Profile失败，openId:{},riseId:{}",profile.getOpenid(),profile.getRiseId());
                }
            }
        }else{
            //更新原数据
            BeanUtils.copyProperties(profile,accountNew);
            profileDao.updateMeta(profile);
        }
    }
}
