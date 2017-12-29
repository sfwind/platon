package com.iquanwai.platon.biz.domain.common.subscribe;


import com.iquanwai.platon.biz.dao.common.SubscribeRouterConfigDao;
import com.iquanwai.platon.biz.po.common.SubscribeRouterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class SubscribeRouterServiceImpl implements SubscribeRouterService {

    @Autowired
    private SubscribeRouterConfigDao subscribeRouterConfigDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public SubscribeRouterConfig loadUnSubscribeRouterConfig(String currentPatchName) {
        List<SubscribeRouterConfig> routerConfigs = subscribeRouterConfigDao.loadAll();

        SubscribeRouterConfig targetSubscribeRouterConfig = null;

        for (SubscribeRouterConfig routerConfig : routerConfigs) {
            try {
                String urlRegex = routerConfig.getUrl();
                logger.info("开始解析：" + urlRegex);
                boolean isMatch = Pattern.matches(urlRegex, currentPatchName);
                if (isMatch) {
                    targetSubscribeRouterConfig = routerConfig;
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        return targetSubscribeRouterConfig;
    }

}
