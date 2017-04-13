package com.iquanwai.platon.biz.util.zk;

import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by justin on 17/3/25.
 */
public class ZKConfigUtils {
    private RobustZooKeeper zooKeeper;

    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static String zkAddress = "106.14.26.18:2181";

    /* 每个项目的path不同 */
    private static final String CONFIG_PATH = "/quanwai/config/rise/";
    /* zk本地配置文件路径 */
    private static final String ZK_CONFIG_PATH = "/data/config/zk";
    /* zk服务器地址配置key */
    private static final String ZK_ADDRESS_KEY = "zk.address";

    public ZKConfigUtils(){
        init();
    }

    public void init(){
        try {
            config();
            zooKeeper = new RobustZooKeeper(zkAddress);
            zk = zooKeeper.getClient();
        } catch (IOException e) {
            logger.error("zk"+zkAddress+" is not connectible", e);
        }
    }

    private void config() {
        File file = new File(ZK_CONFIG_PATH);
        if(file.exists()){
            Properties p = new Properties();
            try {
                p.load(new FileReader(file));
                zkAddress = p.getProperty(ZK_ADDRESS_KEY);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void destroy(){
        if(zooKeeper!=null){
            try {
                zooKeeper.shutdown();
            } catch (InterruptedException e) {
                logger.error("zk" + zkAddress + " is shutdown", e);
            }
        }
    }

    public String getValue(String key){
        try {
            return new String(zk.getData(CONFIG_PATH.concat(key), false, null), "utf-8");
        } catch (Exception e) {
            logger.error("zk" + zkAddress + " get value", e);
        }

        return null;
    }

    public boolean getBooleanValue(String key){
        String value = getValue(key);

        return Boolean.valueOf(value);
    }

    public int getIntValue(String key){
        String value = getValue(key);
        try{
            Assert.notNull(value);
            return Integer.valueOf(value);
        }catch (NumberFormatException e){
            logger.error("zk" + zkAddress + " get int {}", value);
        }

        return Integer.MIN_VALUE;
    }
}
