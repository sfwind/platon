package com.iquanwai.platon.biz.domain.common.richtext;

import com.iquanwai.platon.biz.dao.common.RichTextDao;
import com.iquanwai.platon.biz.po.RichText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 三十文
 */
@Service
public class RichTextServiceImpl implements RichTextService {

    @Autowired
    private RichTextDao richTextDao;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public RichText loadRichText(Integer textId) {
        return richTextDao.load(RichText.class, textId);
    }

}
