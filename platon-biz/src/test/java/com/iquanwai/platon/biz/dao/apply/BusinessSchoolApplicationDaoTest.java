package com.iquanwai.platon.biz.dao.apply;

import com.iquanwai.platon.biz.TestBase;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class BusinessSchoolApplicationDaoTest extends TestBase{
    @Autowired
    private BusinessSchoolApplicationDao businessSchoolApplicationDao;


    @Test
    public void getLastVerifiedByProfileId() throws Exception {
       // System.out.println(businessSchoolApplicationDao.getLastVerifiedByProfileId(54343).toString());
    }

}