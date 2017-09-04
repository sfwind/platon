package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.forum.AnswerService;
import com.iquanwai.platon.biz.domain.forum.QuestionService;
import com.iquanwai.platon.biz.domain.fragmentation.event.EventWallService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.EventWall;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.RiseDto;
import com.iquanwai.platon.web.personal.dto.AreaDto;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import com.iquanwai.platon.web.personal.dto.PlanListDto;
import com.iquanwai.platon.web.personal.dto.ProfileDto;
import com.iquanwai.platon.web.personal.dto.RegionDto;
import com.iquanwai.platon.web.personal.dto.ValidCodeDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nethunder on 2017/2/4.
 */
@RestController
@RequestMapping("/rise/customer")
public class CustomerController {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private PlanService planService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private RiseMemberService riseMemberService;
    @Autowired
    private EventWallService eventWallService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private CertificateService certificateService;

    @RequestMapping("/event/list")
    public ResponseEntity<Map<String, Object>> getEventList(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("活动墙")
                .function("活动墙")
                .action("查询");
        operationLogService.log(operationLog);
        List<EventWall> eventWall = eventWallService.getEventWall(loginUser.getId());

        return WebUtils.result(eventWall);
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRiseInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询帐号信息");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfile(loginUser.getId());

        RiseDto riseDto = new RiseDto();
        riseDto.setRiseId(profile.getRiseId());
        riseDto.setMobile(profile.getMobileNo());
        riseDto.setIsRiseMember(profile.getRiseMember() == 1);
        riseDto.setNickName(profile.getNickname());

        RiseClassMember riseClassMember = accountService.loadLatestRiseClassMember(loginUser.getId());
        if(riseClassMember != null) {
            riseDto.setMemberId(riseClassMember.getMemberId());
        }

        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        if (riseMember != null) {
            riseDto.setMemberType(riseMember.getName());
        }
        List<Coupon> coupons = accountService.loadCoupons(profile.getId());
        riseDto.setCoupons(coupons);

        return WebUtils.result(riseDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadProfile(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        ProfileDto profileDto = new ProfileDto();
        Profile account = accountService.getProfile(loginUser.getId());

        try {
            BeanUtils.copyProperties(profileDto, account);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error", e);
            return WebUtils.error("加载个人信息失败");
        }
        // 查询id
        Region city = accountService.loadCityByName(account.getCity());
        Region province = accountService.loadProvinceByName(account.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
        profileDto.setBindMobile(!StringUtils.isEmpty(account.getMobileNo()));
        return WebUtils.result(profileDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitProfile(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile profile = new Profile();
        try {
            BeanUtils.copyProperties(profile, profileDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error", e);
            return WebUtils.error("提交个人信息失败");
        }
        profile.setId(loginUser.getId());
        accountService.submitPersonalCenterProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/certificate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitCertificateProfile(LoginUser loginUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile profile = new Profile();
        try {
            BeanUtils.copyProperties(profile, profileDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error", e);
            return WebUtils.error("提交个人信息失败");
        }
        profile.setId(loginUser.getId());
        accountService.submitCertificateProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/certificate/{certificateNo}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCertificate(LoginUser loginUser, @PathVariable String certificateNo) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("获取证书");
        operationLogService.log(operationLog);
        RiseCertificate riseCertificate = certificateService.getCertificate(certificateNo);
        return WebUtils.result(riseCertificate);
    }

    @RequestMapping("/region")
    public ResponseEntity<Map<String, Object>> loadRegion(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        List<Region> provinces = accountService.loadAllProvinces();
        List<Region> cities = accountService.loadCities();
        RegionDto regionDto = new RegionDto();
        regionDto.setProvinceList(provinces.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        regionDto.setCityList(cities.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("地区信息")
                .action("加载地区信息");
        operationLogService.log(operationLog);
        return WebUtils.result(regionDto);
    }

    @RequestMapping(value = {"/plans", "/pc/plans"}, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUserPlans(LoginUser loginUser) {
        if (loginUser == null) {
            return WebUtils.error(401, "未登录");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询小课信息");
        operationLogService.log(operationLog);
        List<RiseCertificate> riseCertificates = certificateService.getCertificates(loginUser.getId());
        //清空profileId
        riseCertificates.forEach(riseCertificate -> {
            riseCertificate.setProfileId(null);
        });
        List<ImprovementPlan> plans = planService.getPlans(loginUser.getId());
        PlanListDto list = new PlanListDto();
        List<PlanDto> runningPlans = Lists.newArrayList();
        List<PlanDto> donePlans = Lists.newArrayList();
        plans.forEach(item -> {
            PlanDto planDto = new PlanDto();
            planDto.setName(problemService.getProblem(item.getProblemId()).getProblem());
            planDto.setPic(problemService.getProblem(item.getProblemId()).getPic());
            planDto.setPoint(item.getPoint());
            planDto.setProblemId(item.getProblemId());
            planDto.setPlanId(item.getId());
            if (item.getStatus() == ImprovementPlan.RUNNING || item.getStatus() == ImprovementPlan.COMPLETE) {
                runningPlans.add(planDto);
            } else if (item.getStatus() == ImprovementPlan.CLOSE) {
                donePlans.add(planDto);
            }
        });
        list.setRunningPlans(runningPlans);
        list.setDonePlans(donePlans);
        list.setRiseCertificates(riseCertificates);
        // 查询riseId
        Profile profile = accountService.getProfile(loginUser.getId());
        list.setRiseId(profile.getRiseId());
//        list.setRiseMember(profile.getRiseMember());
        list.setPoint(profile.getPoint());
        return WebUtils.result(list);
    }

    @RequestMapping("/member")
    public ResponseEntity<Map<String, Object>> riseMember(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("RISE")
                .action("查询rise会员信息")
                .memo(riseMember != null ? new Gson().toJson(riseMember) : "none");
        operationLogService.log(operationLog);
        return WebUtils.result(riseMember);
    }

    @RequestMapping(value = "/valid/sms", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> validCode(LoginUser loginUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(loginUser, "用户不能为空");
        boolean result = accountService.validCode(validCodeDto.getCode(), loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("验证")
                .memo(validCodeDto.getCode() + ":" + result);
        operationLogService.log(operationLog);
        return result ? WebUtils.success() : WebUtils.error("验证失败");
    }

    @RequestMapping(value = "/send/valid/code", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> sendCode(LoginUser loginUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(loginUser, "用户不能为空");
        Pair<Boolean, String> result = accountService.sendValidCode(validCodeDto.getPhone(),
                loginUser.getId(), validCodeDto.getAreaCode());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("发送验证码")
                .memo(validCodeDto.getPhone() + ":" + result.getLeft());
        operationLogService.log(operationLog);
        return result.getLeft() ? WebUtils.success() : WebUtils.error(result.getRight());
    }

    @RequestMapping("/forum/mine/questions")
    public ResponseEntity<Map<String, Object>> loadMineQuestions(LoginUser loginUser, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("论坛")
                .action("查询我的提问");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPage(1);
        page.setPageSize(100);
        List<ForumQuestion> forumQuestions = questionService.loadSelfQuestions(loginUser.getId(), page);
        // 设置刷新列表
//        RefreshListDto<ForumQuestion> result = new RefreshListDto<>();
//        result.setList(forumQuestions);
//        result.setEnd(page.isLastPage());
        return WebUtils.result(forumQuestions);
    }

    @RequestMapping("/forum/mine/answers")
    public ResponseEntity<Map<String, Object>> loadMineAnswers(LoginUser loginUser, @ModelAttribute Page page) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("论坛")
                .action("查询我的回答");
        operationLogService.log(operationLog);
        if (page == null) {
            page = new Page();
        }
        page.setPage(1);
        page.setPageSize(100);
        List<ForumAnswer> forumAnswers = answerService.loadSelfAnswers(loginUser.getId(), page);
        return WebUtils.result(forumAnswers);
    }

}
