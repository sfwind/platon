package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.fragmentation.event.EventWallService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.EventWall;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.web.personal.dto.*;
import com.iquanwai.platon.web.fragmentation.dto.RiseDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/event/list")
    public ResponseEntity<Map<String,Object>> getEventList(LoginUser loginUser){
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
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        if(riseMember!=null){
            riseDto.setMemberType(riseMember.getName());
        }
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
            logger.error("beanUtils copy props error",e);
            return WebUtils.error("加载个人信息失败");
        }
        // 查询id
        Region city = accountService.loadCityByName(account.getCity());
        Region province = accountService.loadProvinceByName(account.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
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
        Profile profile =  new Profile();
        try {
            BeanUtils.copyProperties(profile,profileDto);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("beanUtils copy props error",e);
            return WebUtils.error("提交个人信息失败");
        }
        profile.setOpenid(loginUser.getOpenId());
        accountService.submitPersonalCenterProfile(profile);
        return WebUtils.success();
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

    @RequestMapping(value = {"/plans","/pc/plans"}, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUserPlans(LoginUser loginUser) {
        if (loginUser == null) {
            return WebUtils.error(401, "未登录");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("RISE")
                .action("查询小课信息");
        operationLogService.log(operationLog);
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
        // 查询riseId
        Profile profile = accountService.getProfile(loginUser.getId());
        list.setRiseId(profile.getRiseId());
        list.setRiseMember(profile.getRiseMember());
        list.setPoint(profile.getPoint());
        return WebUtils.result(list);
    }

    @RequestMapping("/member")
    public ResponseEntity<Map<String,Object>> riseMember(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("RISE")
                .action("查询rise会员信息")
                .memo(riseMember!=null?new Gson().toJson(riseMember):"none");
        operationLogService.log(operationLog);
        return WebUtils.result(riseMember);
    }
}
