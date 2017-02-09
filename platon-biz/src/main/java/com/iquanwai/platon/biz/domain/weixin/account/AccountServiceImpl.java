package com.iquanwai.platon.biz.domain.weixin.account;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.dao.customer.ProfileDao;
import com.iquanwai.platon.biz.dao.wx.FollowUserDao;
import com.iquanwai.platon.biz.dao.wx.RegionDao;
import com.iquanwai.platon.biz.po.Account;
import com.iquanwai.platon.biz.po.Region;
import com.iquanwai.platon.biz.po.customer.Profile;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
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

    private List<Region> provinceList;

    private List<Region> cityList;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Account getAccount(String openid, boolean realTime) {
        //从数据库查询account对象
        Account account = followUserDao.queryByOpenid(openid);
        if(!realTime && account != null) {
            return account;
        }
        synchronized (this){
            Account accountTemp = followUserDao.queryByOpenid(openid);
            if(!realTime && accountTemp != null) {
                return accountTemp;
            }

            return getAccountFromWeixin(openid, accountTemp);
        }

    }

    private Account getAccountFromWeixin(String openid, Account account) {
        //调用api查询account对象
        String url = USER_INFO_URL;
        Map<String, String> map = Maps.newHashMap();
        map.put("openid", openid);
        url = CommonUtils.placeholderReplace(url, map);
        logger.info("请求用户信息:{}",openid);
        String body = restfulHelper.get(url);
        logger.info("请求用户信息结果:{}",body);
        Map<String, Object> result = CommonUtils.jsonToMap(body);
        Account accountNew = new Account();
        try {
            ConvertUtils.register(new Converter() {
                public Object convert(Class aClass, Object value) {
                    if (value == null)
                        return null;

                    if (!(value instanceof Double)) {
                        logger.error("不是日期类型");
                        throw new ConversionException("不是日期类型");
                    }
                    Double time = (Double) value * 1000;

                    return new DateTime(time.longValue()).toDate();
                }
            }, Date.class);

            BeanUtils.populate(accountNew, result);
            //去除昵称里的表情
            accountNew.setNickname(accountNew.getNickname());
            if(account==null) {
                logger.info("插入用户信息:{}",accountNew);
                if(accountNew.getOpenid()==null){
                    logger.error("===============NULL===============");
                }
                if(accountNew.getNickname()!=null){
                    followUserDao.insert(accountNew);
                    Profile profile = profileDao.queryByOpenId(accountNew.getOpenid());
                    if(profile==null){
                        profile = new Profile();
                        try{
                            BeanUtils.copyProperties(profile,accountNew);
                            logger.info("插入Profile表信息:{}",profile);
                            profileDao.insertProfile(profile);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            logger.error("beanUtils copy props error ######",e);
                        }
                    }
                }
            }else{
                logger.info("更新用户信息:{}",accountNew);
                if(accountNew.getNickname()!=null){
                    followUserDao.updateMeta(accountNew);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return accountNew;
    }

    public void submitPersonalInfo(Account account) {
        followUserDao.updateInfo(account);
    }

    public void collectUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

        String body = restfulHelper.get(url);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);

        for(String openid:usersDto.getData().getOpenid()) {
            getAccount(openid, true);
        }
        logger.info("处理完成");
    }

    public void collectNewUsers() {
        //调用api查询account对象
        String url = GET_USERS_URL;

        String body = restfulHelper.get(url);

        UsersDto usersDto = new Gson().fromJson(body, UsersDto.class);

        List<String> openids = followUserDao.queryAll();
        for(String openid:usersDto.getData().getOpenid()) {
            if(!openids.contains(openid)) {
                getAccountFromWeixin(openid, null);
            }
        }
        logger.info("处理完成");
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
}
