package com.iquanwai.platon.biz.domain.weixin.accessToken;


import com.iquanwai.platon.biz.dao.wx.AccessTokenDao;
import com.iquanwai.platon.biz.po.common.AccessToken;
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
    @Autowired
    private AccessTokenDao accessTokenDao;

    public String getAccessToken() {
        if(accessToken!=null){
            return accessToken;
        }

        AccessToken token = accessTokenDao.load(AccessToken.class, 1);
        if(token==null){
            logger.info("insert access token");
            accessTokenDao.insertOrUpdate(_getAccessToken());
        }else {
            accessToken = token.getAccessToken();
        }

        return accessToken;
    }

    private String _getAccessToken() {
        logger.info("refreshing access token");
        String strAccessToken = weiXinAccessTokenRepo.getAccessToken();
        if(strAccessToken!=null){
            accessToken = strAccessToken;
        }
        return accessToken;
    }

    public String refreshAccessToken(boolean force) {
        if(force) {
            forceUpdateAccessToken();
        }else{
            AccessToken token = accessTokenDao.load(AccessToken.class, 1);
            if(token==null){
                logger.info("insert access token");
                accessTokenDao.insertOrUpdate(_getAccessToken());
            }else{
                //如果数据库的accessToken未刷新,则强制刷新
                if(token.getAccessToken().equals(accessToken)){
                    forceUpdateAccessToken();
                }else{
                    //如果数据库的accessToken已刷新,返回数据库的token
                    logger.info("reload access token");
                    accessToken = token.getAccessToken();
                }
            }
        }

        return accessToken;
    }

    private void forceUpdateAccessToken(){
        String accessToken = _getAccessToken();
        accessTokenDao.insertOrUpdate(accessToken);
    }
}
