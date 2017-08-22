package com.iquanwai.platon.biz.service;

import com.iquanwai.platon.biz.TestBase;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationEvaluateService;
import com.iquanwai.platon.biz.domain.fragmentation.operation.OperationFreeLimitService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Test
    public void testSendCustomerMsg(){
        operationFreeLimitService.sendCustomerMsg("o-Es21RVF3WCFQMOtl07Di_O9NVo");
    }

    @Test
    public void testSendInvitationMsg(){
//        String[] list = {
//                "oWo9HwvvQGd0MmdTNGEceP0UrzyU",
//                "oWo9Hwqp_xtCW4-pRl41q2lgxL10",
//                "oWo9Hwvja6qrBhcye8FIzMBevCV4",
//                "oWo9HwlV4EFDHJV4A4dgMunH7hNE",
//                "oWo9Hwp_HXF8VvrI_oomJLBZbVQU",
//                "oWo9HwiKlwmzu2aUhY-tDGudSgrU",
//                "oWo9HwrkDdA9IaxiihMywj-Zv4_Q",
//                "oWo9Hwrb-elldX4Qd39H97HPMGko",
//                "oWo9HwkH8uGkLWyt3ovQ85m5CT3o",
//                "oWo9HwoS9v46Nouv8pctxgIl-1FI",
//                "oWo9HwnLbWXxJC79CpMRldSOQ20g",
//                "oWo9Hwv-oFkR4U7SofdeKw1I2vVY",
//                "oWo9Hwvx5NpT7QvYM_-bz7Lapj5k",
//                "oWo9HwulzQNFek9qTk8Mkf5Sy8dE",
//                "oWo9HwnNA8Eu_4vKC8jJjTJDCqwU",
//                "oWo9HwtfLm_e9wIbxz-a83UhfnpI",
//                "oWo9HwsMKATx1-HaK18_UgOYo0Rk",
//                "oWo9Hwl46dQS8SAvebS1N4pggRLQ",
//                "oWo9Hwv42hEyh73l77YbULmGh-vs",
//                "oWo9HwsaFgGXHSRXMqKHwQpThIfw",
//                "oWo9Hwta0yUQBKWdlxdwnG2ubeAc",
//                "oWo9Hwspr4IaYH6jNi42zT94bNE8",
//                "oWo9HwghUil5jF3r5oLSXJZ0lv0E",
//                "oWo9HwsvuHzEeV8TiNg1Rv2Im3Wg",
//                "oWo9Hwl4y_KLkBuOUJk1SB5qsqP4",
//                "oWo9HwmZpHU5Q59Qse0WJYdL463Y",
//                "oWo9HwqtB5DAR1YDWSckuoklNAVg",
//                "oWo9HwvYQlmD0WP8niHWgl46oM2I",
//                "oWo9HwqCri6Yip_xux2cORV1QoKA",
//                "oWo9HwpdDXvzzldn57Z6-wi1QZRU",
//                "oWo9HwqS_xvjr6Zxnx9iDAkX1AoI",
//                "oWo9Hwi0jV9l0e40CAgGiWiJP0es",
//                "oWo9HwlmHj7gArFbbC4IeO2dSxEY",
//                "oWo9HwgJHlZ7QfaFa_To6HS8kTYg",
//                "oWo9HwgT2d8WOMNDci10e-pL3AUI",
//                "oWo9HwhO5ZS-aPMNnmfNoJ6Joi5M",
//                "oWo9HwnT7EUJP17Jdfe6NAMENLrU",
//                "oWo9HwtkmjfEXiewOmXwq1eOPAVk",
//                "oWo9HwvRK2_I_KJIbaQLB-EZlsoQ",
//                "oWo9HwruUV-n46vWPBtzFo4P58Hc",
//                "oWo9HwseZWosZBTuVk6aiMckGsjc",
//                "oWo9HwvJ44SzWcm1upPNdtMmYjPk",
//                "oWo9Hwj73JTQToOOni6guxQuAQFo",
//                "oWo9HwkWlfQTwZGot3YiBXefLptQ",
//                "oWo9HwgZZCgoAgPVbtxSLYm13_CA",
//                "oWo9HwqT2wlcU4_QL-er-M77hFTg",
//                "oWo9HwuovIDQX7R_1c1t4g3BdCmg",
//                "oWo9Hwu3RgM9IPuzQ2We6HkQsFoo",
//                "oWo9HwsRfE0szCyZWPcK2zF54W4M",
//                "oWo9HwpEV9FdIiY0sHZlYE2xJdPU"
//
//        };
//        for(String openid:list){
//            try {
//                customerMessageService.sendCustomerMessage(openid, "CJuSSkLTy_al_iKlsSO3U3WnvWTJeQ7bOqj2uaGgEcrhA0Qd9LS8mcqDo6S1cKLw", 2);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }


    }

}
