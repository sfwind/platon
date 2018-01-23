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
    public String openId(String mobileState) {
        if (mobileState == null) {
            return null;
        }
        Callback callback = callbackDao.queryByState(mobileState);
        if (callback == null) {
            logger.error("accessToken {} is invalid", mobileState);
            return null;
        }
        return callback.getOpenid();
    }

    @Override
    public String pcOpenId(String pcState) {
        if (pcState == null) {
            logger.info("error，pc _qt is null");
            return null;
        }
        Callback callback = callbackDao.queryByState(pcState);
        if (callback == null) {
            logger.error("pcAccessToken {} is invalid", pcState);
            return null;
        }
        return callback.getOpenid();
    }

}
