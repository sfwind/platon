package com.iquanwai.platon.biz;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/config/spring/local/appcontext-*.xml",
        "classpath*:/config/spring/local/dbcon/appcontext-*.xml"
})

public class TestBase {
    protected void log(Object obj){
        System.out.println(obj);
    }
}

