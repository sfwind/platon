package com.iquanwai.platon.biz.dao;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.dao.bible.SubscribeViewPointDao;
import com.iquanwai.platon.biz.po.bible.SubscribeViewPoint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/9/6.
 */
public class BibleDaoTest extends TestBase {
    @Autowired
    private SubscribeViewPointDao subscribeViewPointDao;

    @Test
    public void pointTest(){
        List<SubscribeViewPoint> load = subscribeViewPointDao.load(1, new Date(), "1,2");
        System.out.println(load);
    }
}
