package com.iquanwai.platon.mq;

import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.common.message.MQService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.rabbitmq.RabbitMQReceiver;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * Created by justin on 17/4/25.
 */
@Service
public class CacheReloadReceiver {
    public final static String TOPIC = "rise_resource_reload";

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AccountService accountService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private MQService mqService;
    @Autowired
    private RiseMemberDao riseMemberDao;

    @PostConstruct
    public void init(){
        RabbitMQReceiver receiver = new RabbitMQReceiver();
        receiver.init(null, TOPIC, ConfigUtils.getRabbitMQIp(), ConfigUtils.getRabbitMQPort());
        logger.info("通道建立");
        receiver.setAfterDealQueue(mqService::updateAfterDealOperation);
        // 监听器
        receiver.listen(msg -> {
            String message = msg.toString();
            logger.info("receive message {}", message);
            switch (message) {
                case "region":
                    accountService.reloadRegion();
                    break;
                case "reload":
                    cacheService.reload();
                    pictureService.reloadModule();
                    break;
                case "member":
                    Integer memberSize = refreshStatus();
                    logger.info("当前登录人数:{}", memberSize);
                    break;
            }
        });
        logger.info("开启队列监听");
    }

    // 刷新缓存，返回当前登录人数
    public Integer refreshStatus(){
        Collection<LoginUser> allUsers = LoginUserResolver.getAllUsers();
        for (LoginUser user : allUsers) {
            try {
                if (user.getRiseMember() == 1) {
                    // 是会员，查询现在还是不是
                    RiseMember riseMember = riseMemberDao.validRiseMember(user.getId());
                    if (riseMember == null) {
                        // 不是会员了
                        user.setRiseMember(0);
                        logger.info("openId:{},expired member", user.getOpenId());
                    }
                }
            } catch (Exception e){
                logger.error("会员过期检查失败", e);
            }
        }
        return allUsers.size();
    }

}
