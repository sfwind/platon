package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.domain.weixin.material.UploadResourceService;
import com.iquanwai.platon.biz.domain.weixin.qrcode.QRCodeService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;

/**
 * Created by justin on 17/8/2.
 */
public class OperationServiceTest extends TestBase {
    @Autowired
    private OperationFreeLimitService operationFreeLimitService;
    @Autowired
    private OperationEvaluateService operationEvaluateService;
    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private UploadResourceService uploadResourceService;

    @Test
    public void testSendInvitationMsg(){
        String scene = "test";
        BufferedImage qrBuffer = qrCodeService.loadQrImage(scene);
        String s = uploadResourceService.uploadResource(qrBuffer);
        System.out.println(s);

    }

}
