package com.iquanwai.platon.biz.domain.fragmentation.manager;

import com.iquanwai.platon.biz.po.common.Profile;

import java.awt.image.BufferedImage;

/**
 * Created by justin on 17/8/2.
 */
public interface CardManager {

    /**
     * 获取精华卡
     * 返回base64
     * */
    String loadEssenceCardImg(Integer profileId, Integer problemId, Integer chapterId, Integer planId);

    /**
     * 获取缩略图url
     * */
    String loadTargetThumbnailByChapterId(int chapterId, int totalSize);

    /**
     * 获取缩略图锁定url
     * */
    String loadTargetThumbnailLockByChapterId(int chapterId, int totalSize);

    BufferedImage loadCaitongBgImage();

    BufferedImage loadCaitongHead();

}
