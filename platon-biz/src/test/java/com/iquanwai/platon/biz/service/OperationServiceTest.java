package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by justin on 17/8/2.
 */
public class OperationServiceTest extends TestBase {
    @Autowired
    private OperationService operationService;

    @Test
    public void testSendCustomerMsg(){
        operationService.sendCustomerMsg("o-Es21RVF3WCFQMOtl07Di_O9NVo");
    }
}
