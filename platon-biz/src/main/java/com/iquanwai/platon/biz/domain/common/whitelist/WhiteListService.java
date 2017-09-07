package com.iquanwai.platon.biz.domain.common.whitelist;

/**
 * Created by justin on 16/12/26.
 */
public interface WhiteListService {
    boolean isInWhiteList(String function, Integer profileId);

    /**
     * TODO 待删除，临时逻辑
     */
    boolean isInBibleWhiteList(Integer profileId);
}
