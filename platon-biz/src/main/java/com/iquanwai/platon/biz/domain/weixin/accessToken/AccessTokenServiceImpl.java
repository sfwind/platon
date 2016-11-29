package com.iquanwai.platon.biz.domain.weixin.accessToken;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AccessTokenServiceImpl implements AccessTokenService {
    private static String accessToken;
    protected static Logger logger = LoggerFactory.getLogger(AccessTokenService.class);
    @Autowired
    private WeiXinAccessTokenRepo weiXinAccessTokenRepo;

    public synchronized String getAccessToken() {
        if(accessToken!=null){
            return accessToken;
        }

        return _getAccessToken();
    }

    private String _getAccessToken() {
        logger.info("refreshing access token");
        String strAccessToken = weiXinAccessTokenRepo.getAccessToken();
        if(strAccessToken!=null){
            accessToken = strAccessToken;
        }
        return accessToken;
    }

    public synchronized String refreshAccessToken() {
        return _getAccessToken();
    }
}
