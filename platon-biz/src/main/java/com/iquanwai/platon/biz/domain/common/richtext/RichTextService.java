package com.iquanwai.platon.biz.domain.common.richtext;

import com.iquanwai.platon.biz.po.RichText;

/**
 * Created by 三十文
 */
public interface RichTextService {
    /**
     * 根据富文本 id 获取富文本内容
     */
    RichText loadRichText(Integer textId);
}
