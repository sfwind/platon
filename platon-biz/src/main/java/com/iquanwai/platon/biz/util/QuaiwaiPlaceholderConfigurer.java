package com.iquanwai.platon.biz.util;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * Created by justin on 16/9/11.
 */
public class QuaiwaiPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    @Override
    protected String resolvePlaceholder(String placeholder, Properties props) {
        return ConfigUtils.getValue(placeholder);
    }
}
