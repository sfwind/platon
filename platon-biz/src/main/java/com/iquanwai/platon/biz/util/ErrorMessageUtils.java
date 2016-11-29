package com.iquanwai.platon.biz.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

/**
 * Created by justin on 16/9/30.
 */
public class ErrorMessageUtils {
    private static Config config;
    private static Config fileconfig;
    static{
        config = ConfigFactory.load("errmsg");
        fileconfig = ConfigFactory.parseFile(new File("/data/config/errmsg"));
        config = fileconfig.withFallback(config);
    }

    public static String getErrmsg(String key){
        return config.getString(key);
    }
}
