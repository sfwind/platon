package com.iquanwai.platon.web.fragmentation.controller.bible.dto;

import com.iquanwai.platon.biz.po.bible.SubscribeArticleTag;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 2017/9/13.
 */
@Data
public class TagDto {
    private List<SubscribeArticleTag> subscribeArticleTags;
}
