package com.iquanwai.platon.biz.domain.weixin.oauth;

import com.iquanwai.platon.biz.dao.wx.CallbackDao;
import com.iquanwai.platon.biz.po.common.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/8/13.
 */
@Service
public class OAuthServiceImpl implements OAuthService {
    @Autowired
    private CallbackDao callbackDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String openId(String accessToken) {
        if(accessToken==null){
            return null;
        }
        Callback callback = callbackDao.queryByAccessToken(accessToken);
        if(callback==null){
            logger.error("accessToken {} is invalid", accessToken);
            return null;
        }
        return callback.getOpenid();
    }

    @Override
    public String pcOpenId(String act){
        if (act == null) {
            logger.info("error，pc _qt is null");
            return null;
        }
        Callback callback = callbackDao.queryByPcAccessToken(act);
        if (callback == null) {
            logger.error("pcAccessToken {} is invalid", act);
            return null;
        }
        return callback.getOpenid();
    }

}
