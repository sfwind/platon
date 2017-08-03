package com.iquanwai.platon.biz.domain.fragmentation.plan;

import com.iquanwai.platon.biz.po.common.Profile;

import java.awt.image.BufferedImage;

/**
 * Created by justin on 17/8/2.
 */
public interface CardRepository {

    /**
     * 获取精华卡
     * */
    BufferedImage loadEssenceCardImg(Profile profile, Integer problemId, Integer chapterId, int totalSize);

    /**
     * 获取缩略图url
     * */
    String loadTargetThumbnailByChapterId(int chapterId, int totalSize);

    /**
     * 获取缩略图锁定url
     * */
    String loadTargetThumbnailLockByChapterId(int chapterId, int totalSize);

    /**
     * 获取限免小课熊猫卡
     * */
    BufferedImage loadDefaultCardImg(Profile profile);
}
