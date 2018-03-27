package com.iquanwai.platon.biz.util.zk;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 17/3/25.
 */
public class ZKConfigUtils {
    private RobustZooKeeper zooKeeper;

    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static String zkAddress = "172.19.10.215:12181";

    private static Cache<String, String> CONFIG_CACHE;

    /* 每个项目的path不同 */
    private static final String CONFIG_PATH = "/quanwai/config/rise/";
    /* 架构类型的path */
    private static final String ARCH_PATH = "/quanwai/config/arch/";
    /* zk本地配置文件路径 */
    private static final String ZK_CONFIG_PATH = "/data/config/zk";
    /* zk服务器地址配置key */
    private static final String ZK_ADDRESS_KEY = "zk.address";

    public ZKConfigUtils() {
        init();
    }

    public void init() {
        try {
//            config();
            zooKeeper = new RobustZooKeeper(zkAddress);
            zk = zooKeeper.getClient();
        } catch (IOException e) {
            logger.error("zk" + zkAddress + " is not connectible", e);
        }
    }

    public void reconnect() throws IOException {
        zk = zooKeeper.getClient();
    }

    private void config() {
        CONFIG_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(1L, TimeUnit.MINUTES)
                .build();
        File file = new File(ZK_CONFIG_PATH);
        if (file.exists()) {
            Properties p = new Properties();
            try {
                p.load(new FileReader(file));
                zkAddress = p.getProperty(ZK_ADDRESS_KEY);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void destroy() {
        if (zooKeeper != null) {
            try {
                zooKeeper.shutdown();
            } catch (InterruptedException e) {
                logger.error("zk" + zkAddress + " is shutdown", e);
            }
        }
    }

    public String getArchValue(String key) {
        return getValue(key, ARCH_PATH);
    }

    public String getValue(String key) {
        String fullPath = CONFIG_PATH.concat(key);
        try {
            if (zk.exists(fullPath, false) != null) {
                return getValue(key, CONFIG_PATH);
            } else {
                return getArchValue(key);
            }
        } catch (KeeperException e) {
            try {
                logger.error("zk " + zkAddress + " get value", e);
                reconnect();
                return getValueOnce(key, CONFIG_PATH);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public String getValue(String key, String prePath) {
        try {
            String value = CONFIG_CACHE.getIfPresent(key);
            if (value != null) {
                return value;
            }
            logger.info("get {} from zk", key);
            String fullPath = prePath.concat(key);
            if (zk.exists(fullPath, false) == null) {
                logger.error("the full path node is none : {}", fullPath);
                return null;
            }
            String json = new String(zk.getData(fullPath, false, null), "utf-8");
            ConfigNode configNode = new Gson().fromJson(json, ConfigNode.class);
            value = configNode.getValue();
            CONFIG_CACHE.put(key, value);
            return value;
        } catch (KeeperException e) {
            try {
                logger.error("zk " + zkAddress + " get value", e);
                reconnect();
                return getValueOnce(key, prePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get value", e);
        }

        return null;
    }

    public Boolean getBooleanValue(String key) {
        String value = getValue(key);

        return Boolean.valueOf(value);
    }

    public Integer getIntValue(String key) {
        String value = getValue(key);
        try {
            Assert.notNull(value);
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.error("zk" + zkAddress + " get int {}", value);
        }

        return null;
    }

    private String getValueOnce(String key, String prePath) {
        try {
            String value = CONFIG_CACHE.getIfPresent(key);
            if (value != null) {
                return value;
            }
            logger.info("get {} from zk", key);
            String fullPath = prePath.concat(key);
            if (zk.exists(fullPath, false) == null) {
                logger.error("the full path node is none : {}", fullPath);
                return null;
            }
            String json = new String(zk.getData(fullPath, false, null), "utf-8");
            ConfigNode configNode = new Gson().fromJson(json, ConfigNode.class);
            value = configNode.getValue();
            CONFIG_CACHE.put(key, value);
            return value;
        } catch (Exception e) {
            logger.error("zk " + zkAddress + " get value", e);
        }

        return null;
    }

    public Double getDoubleValue(String key) {
        String value = getValue(key);
        try {
            Assert.notNull(value);
            return Double.valueOf(value);
        } catch (Exception e) {
            logger.error("zk" + zkAddress + " get int {}", value);
        }

        return null;
    }
}
