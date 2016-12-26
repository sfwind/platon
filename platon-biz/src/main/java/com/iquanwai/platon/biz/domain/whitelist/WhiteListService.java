package com.iquanwai.platon.biz.domain.whitelist;

/**
 * Created by justin on 16/12/26.
 */
public interface WhiteListService {
    boolean isInWhiteList(String function, String openid);
}
