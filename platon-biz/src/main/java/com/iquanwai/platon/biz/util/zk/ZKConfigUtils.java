package com.iquanwai.platon.biz.util.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * Created by justin on 17/3/25.
 */
public class ZKConfigUtils {
    private RobustZooKeeper zooKeeper;

    private ZooKeeper zk;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final String zkAddress = "106.14.26.18:2181";

    /* 每个项目的path不同 */
    private static final String CONFIG_PATH = "/quanwai/config/rise/";

    public ZKConfigUtils(){
        init();
    }

    public void init(){
        try {
            zooKeeper = new RobustZooKeeper(zkAddress);
            zk = zooKeeper.getClient();
        } catch (IOException e) {
            logger.error("zk"+zkAddress+" is not connectible", e);
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
//            key = key.replaceAll("\\.","/");
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


    public void updateValue(String key, String value){
        try {
//            key = key.replaceAll("\\.","/");
            zk.setData(CONFIG_PATH.concat(key), value.getBytes("utf-8"), -1);
        } catch (Exception e) {
            logger.error("zk" + zkAddress + " set key {} value {} failed", key, value);
        }
    }

    public void deleteValue(String key){
        try {
//            key = key.replaceAll("\\.","/");
            zk.delete(CONFIG_PATH.concat(key),  -1);
        } catch (Exception e) {
            logger.error("zk" + zkAddress + " delete key {} failed", key);
        }
    }

    public void createValue(String key, String value){
        try {
//            key = key.replaceAll("\\.","/");
            zk.create(CONFIG_PATH.concat(key), value.getBytes("utf-8"),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (Exception e) {
            logger.error("zk" + zkAddress + " create key {} value {} failed", key, value);
        }
    }
}
