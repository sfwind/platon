package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.AnnualSummary;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.AnnualUserDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.AuditoriumDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.LibraryDto;
import com.iquanwai.platon.web.fragmentation.controller.operation.dto.SchoolGateDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author nethunder
 */
@RestController
@RequestMapping("/rise/operation/annual")
public class AnnualController {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private CustomerService customerService;

    private final static String[] CLASSMATE_URL = {
            "https://static.iqycamp.com/images/classmate1_2.png?imageslim",
            "https://static.iqycamp.com/images/classmate2_2.png?imageslim",
            "https://static.iqycamp.com/images/classmate3_2.png?imageslim",
            "https://static.iqycamp.com/images/classmate4_2.png?imageslim",
            "https://static.iqycamp.com/images/classmate5_2.png?imageslim",
    };

    private final static String[] CLASSMATES = {
            "曾进、蔡垒磊、崔勇",
            "Eason、Cherry Xu、王斐",
            "Eason、曾进、蔡垒磊",
            "曾进、Cherry Xu、孙海光",
            "生菜、Eason、蔡垒磊",
    };

    @RequestMapping(value = "/summary/user", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSummaryUser(GuestUser guestUser, @RequestParam(required = false) String riseId) {
        OperationLog operationLog = OperationLog.create().openid(guestUser != null ? guestUser.getOpenId() : "")
                .module("推广")
                .function("年度报告")
                .action("获取用户");
        operationLogService.log(operationLog);
        Assert.notNull(guestUser, "登录用户不能为空");

        AnnualUserDto dto = new AnnualUserDto();
        if (riseId != null) {
            String currentOpenId = guestUser.getOpenId();
            Profile currentProfile = accountService.getProfile(currentOpenId);
            if (currentProfile != null) {
                dto.setCurrentRiseId(currentProfile.getRiseId());
            } else {
                dto.setCurrentRiseId(null);
            }
            dto.setMasterRiseId(riseId);
            Profile masterProfile = accountService.getProfileByRiseId(riseId);
            dto.setMasterNickName(masterProfile.getNickname());
            dto.setMasterHeadImageUrl(masterProfile.getHeadimgurl());
        } else {
            String currentOpenId = guestUser.getOpenId();
            Profile masterProfile = accountService.getProfile(currentOpenId);
            dto.setCurrentRiseId(masterProfile.getRiseId());
            dto.setMasterRiseId(masterProfile.getRiseId());
            dto.setMasterNickName(masterProfile.getNickname());
            dto.setMasterHeadImageUrl(masterProfile.getHeadimgurl());
        }
        return WebUtils.result(dto);
    }

    @RequestMapping(value = "/summary/schoolgate", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSummarySchoolGate(GuestUser guestUser, @RequestParam String riseId) {
        OperationLog operationLog = OperationLog.create().openid(guestUser != null ? guestUser.getOpenId() : "")
                .module("推广")
                .function("年度报告")
                .action("进入校门");
        operationLogService.log(operationLog);
        AnnualSummary annualSummary = customerService.loadUserAnnualSummary(riseId);
        SchoolGateDto dto = new SchoolGateDto();
        if (annualSummary == null) {
            return WebUtils.result(dto);
        } else {
            dto.setRegisterDate(DateUtils.parseDateToFormat5(annualSummary.getRegisterDate()));
            dto.setRegisterSequence(annualSummary.getRegisterSequence());
            dto.setClassmateUrl(getClassmate(riseId));
            dto.setClassmates(getClassmates(riseId));
            return WebUtils.result(dto);
        }
    }

    private String getClassmate(String riseId) {
        int hash = Math.abs(riseId.hashCode());
        return CLASSMATE_URL[hash % 5];
    }

    private String getClassmates(String riseId) {
        int hash = Math.abs(riseId.hashCode());
        return CLASSMATES[hash % 5];
    }

    @RequestMapping(value = "/summary/library", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSummarySchoolLibrary(GuestUser guestUser, @RequestParam String riseId) {
        OperationLog operationLog = OperationLog.create().openid(guestUser != null ? guestUser.getOpenId() : "")
                .module("推广")
                .function("年度报告")
                .action("进入图书馆");
        operationLogService.log(operationLog);
        AnnualSummary annualSummary = customerService.loadUserAnnualSummary(riseId);
        LibraryDto dto = new LibraryDto();
        if (annualSummary == null) {
            return WebUtils.result(dto);
        } else {
            dto.setCourseCount(annualSummary.getCourseCount());
            dto.setKnowledgeCount(annualSummary.getKnowledgeCount());
            dto.setAllRightCount(annualSummary.getAllRightCount());
            List<LibraryDto.HeadPicDto> assts = Lists.newArrayList();
            List<LibraryDto.HeadPicDto> classmates = Lists.newArrayList();
            this.createHeadPic(annualSummary.getFirstAsst()).append(assts);
            this.createHeadPic(annualSummary.getSecondAsst()).append(assts);
            this.createHeadPic(annualSummary.getThirdAsst()).append(assts);

            this.createHeadPic(annualSummary.getFirstClassmate()).append(classmates);
            this.createHeadPic(annualSummary.getSecondClassmate()).append(classmates);
            this.createHeadPic(annualSummary.getThirdClassmate()).append(classmates);
            this.createHeadPic(annualSummary.getForthClassmate()).append(classmates);

            dto.setAssts(assts);
            dto.setClassmates(classmates);
            return WebUtils.result(dto);
        }
    }

    @RequestMapping(value = "/summary/auditorium", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getSummaryAuditorium(GuestUser guestUser, @RequestParam String riseId) {
        OperationLog operationLog = OperationLog.create().openid(guestUser != null ? guestUser.getOpenId() : "")
                .module("推广")
                .function("年度报告")
                .action("进入礼堂");
        operationLogService.log(operationLog);
        AnnualSummary annualSummary = customerService.loadUserAnnualSummary(riseId);
        AuditoriumDto dto = new AuditoriumDto();
        if (annualSummary == null) {
            return WebUtils.result(dto);
        } else {
            dto.setCardCount(annualSummary.getCardCount());
            dto.setPoint(annualSummary.getPoint());
            dto.setDefeatPercentage(Double.valueOf(annualSummary.getDefeatPercentage() * 100).intValue());
            return WebUtils.result(dto);
        }
    }

    private LibraryDto.HeadPicDto createHeadPic(Integer profileId) {
        Profile profile = accountService.getProfile(profileId);
        LibraryDto.HeadPicDto headPicDto = new LibraryDto.HeadPicDto();
        headPicDto.setHeadImageUrl(profile.getHeadimgurl());
        headPicDto.setNickName(profile.getNickname());
        return headPicDto;
    }

}
