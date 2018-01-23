package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.forum.AnswerService;
import com.iquanwai.platon.biz.domain.forum.QuestionService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.biz.domain.fragmentation.event.EventWallService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.EventWall;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.po.forum.ForumAnswer;
import com.iquanwai.platon.biz.po.forum.ForumQuestion;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.RiseDto;
import com.iquanwai.platon.web.personal.dto.*;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
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
    private CacheService cacheService;
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
    @Autowired
    private CustomerService customerService;
    @Autowired
    private LoginUserService loginUserService;

    @RequestMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("小程序")
                .function("获取用户基本信息")
                .action("查询");
        operationLogService.log(operationLog);

        Profile profile = new Profile();
        profile.setNickname(loginUser.getWeixinName());
        profile.setHeadimgurl(loginUser.getHeadimgUrl());
        return WebUtils.result(profile);
    }

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
                .function("账号")
                .action("查询账号信息");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfile(loginUser.getId());

        RiseDto riseDto = new RiseDto();
        riseDto.setRiseId(profile.getRiseId());
        riseDto.setMobile(profile.getMobileNo());
        riseDto.setIsRiseMember(profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP);
        riseDto.setNickName(profile.getNickname());
        riseDto.setHeadImgUrl(profile.getHeadimgurl());

        RiseClassMember riseClassMember = accountService.loadDisplayRiseClassMember(loginUser.getId());
        if (riseClassMember != null) {
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

        BeanUtils.copyProperties(account, profileDto);
        // 查询id
        Region city = accountService.loadCityByName(account.getCity());
        Region province = accountService.loadProvinceByName(account.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
        boolean bindMobile = true;
        if (StringUtils.isEmpty(account.getMobileNo()) && StringUtils.isEmpty(account.getWeixinId())) {
            bindMobile = false;
        }
        profileDto.setBindMobile(bindMobile);
        profileDto.setPhone(account.getMobileNo());
        profileDto.setWeixinId(account.getWeixinId());
        profileDto.setReceiver(account.getReceiver());
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
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(loginUser.getId());
        accountService.submitPersonalCenterProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/headImg/upload")
    public ResponseEntity<Map<String, Object>> updateHeadImg(LoginUser loginUser, @RequestParam("file") MultipartFile file) {
        Long fileSize = file.getSize();
        if (fileSize > 5 * 1000 * 1000) { // 文件图片大于 5M
            return WebUtils.error("文件内容过大");
        }
        String fileName = file.getOriginalFilename();
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("头像修改")
                .action("上传头像");
        operationLogService.log(operationLog);
        String imageUrl = null;
        try {
            imageUrl = customerService.uploadHeadImage(loginUser.getId(), fileName, file.getInputStream());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        if (imageUrl != null) {
            return WebUtils.result(imageUrl);
        } else {
            return WebUtils.error("头像上传失败");
        }
    }

    @RequestMapping(value = "/profile/headImg/update")
    public ResponseEntity<Map<String, Object>> updateHeadImg(LoginUser loginUser, @RequestParam("headImgUrl") String headImgUrl) {
        Assert.notNull(loginUser, "登录用户不能为空");
        Assert.notNull(headImgUrl, "上传头像不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("头像修改")
                .action("更新头像");
        operationLogService.log(operationLog);
        int updateResult = customerService.updateHeadImageUrl(loginUser.getId(), headImgUrl);
        if (updateResult > 0) {
            loginUserService.updateLoginUserByOpenId(loginUser.getOpenId());
            return WebUtils.success();
        } else {
            return WebUtils.error("头像更新失败");
        }
    }

    @RequestMapping(value = "/profile/nickname/update", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateNickName(LoginUser loginUser, @RequestBody NicknameDto nickname) {
        Assert.notNull(loginUser, "登录用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("昵称修改")
                .action("提交昵称");
        operationLogService.log(operationLog);

        int result = customerService.updateNickName(loginUser.getId(), nickname.getNickname());
        if (result > 0) {
            loginUserService.updateLoginUserByOpenId(loginUser.getOpenId());
            return WebUtils.result("昵称更新成功");
        } else {
            return WebUtils.result("昵称更新失败");
        }
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
        BeanUtils.copyProperties(profileDto, profile);
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
        if (riseCertificate.getDel()) {
            return WebUtils.error("证书已失效");
        } else {
            return WebUtils.result(riseCertificate);
        }
    }

    @RequestMapping(value = "/certificate/download/{certificateNo}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCertificateAndNext(LoginUser loginUser, @PathVariable String certificateNo) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("下载证书");
        operationLogService.log(operationLog);
        RiseCertificate riseCertificate = certificateService.getCertificate(certificateNo);
        RiseCertificate nextRiseCertificate = certificateService.getNextCertificate(riseCertificate.getId());
        if (nextRiseCertificate != null) {
            riseCertificate.setNextCertificateNo(nextRiseCertificate.getCertificateNo());
        }

        if (riseCertificate.getDel()) {
            return WebUtils.error("证书已失效");
        } else {
            return WebUtils.result(riseCertificate);
        }
    }

    @RequestMapping(value = "/certificate/convert", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> convertBase64(LoginUser loginUser, @RequestBody Base64ConvertDto base64ConvertDto) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("证书转码");
        operationLogService.log(operationLog);
        if (base64ConvertDto != null && base64ConvertDto.getBase64Str() != null && base64ConvertDto.getImageName() != null) {
            // 去除 base64 的头属性信息
            String base64Str = base64ConvertDto.getBase64Str().substring(base64ConvertDto.getBase64Str().indexOf(",") + 1);
            // 图片保存路径，在测试环境使用，此处根据情况配置
            if (base64ConvertDto.getType() == null) {
                base64ConvertDto.setType(0);
            }
            // 本地调用时,需要在localconfig中配置certificate.local.save.folder
            String imagePath = ConfigUtils.getCertificateSaveFolder() + base64ConvertDto.getType() + "/" + base64ConvertDto.getImageName() + ".png";
            boolean saveSuccess = certificateService.convertCertificateBase64(base64Str, imagePath);
            if (saveSuccess) {
                return WebUtils.success();
            }
        }
        return WebUtils.error("图片保存失败");
    }

    @RequestMapping(value = "/certificate/download/success/{certificateNo}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> updateCertificateDownloadTime(LoginUser loginUser, @PathVariable String certificateNo) {
        Assert.notNull(loginUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("证书下载完成");
        operationLogService.log(operationLog);
        certificateService.updateDownloadTime(certificateNo);
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

    @RequestMapping(value = {"/plans", "/pc/plans"}, method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadUserPlans(LoginUser loginUser) {
        if (loginUser == null) {
            return WebUtils.error(401, "未登录");
        }

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("个人中心")
                .function("课程")
                .action("查询课程信息");
        operationLogService.log(operationLog);
        List<RiseCertificate> riseCertificates = certificateService.getCertificates(loginUser.getId());
        //清空profileId
        riseCertificates.forEach(riseCertificate -> riseCertificate.setProfileId(null));
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
            planDto.setProblem(cacheService.getProblem(item.getProblemId()).simple());
            planDto.setLearnable(item.getStartDate().compareTo(new Date()) <= 0);
        });
        list.setRunningPlans(runningPlans);
        list.setDonePlans(donePlans);
        list.setRiseCertificates(riseCertificates);
        // 查询riseId
        Profile profile = accountService.getProfile(loginUser.getId());
        list.setRiseId(profile.getRiseId());
        list.setPoint(profile.getPoint());
        // 当前已收藏课程
        List<Problem> problemCollections = problemService.loadProblemCollections(loginUser.getId());
        list.setProblemCollections(problemCollections);
        return WebUtils.result(list);
    }

    @RequestMapping("/member")
    public ResponseEntity<Map<String, Object>> riseMember(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        RiseMember riseMember = riseMemberService.getRiseMember(loginUser.getId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("会员")
                .action("查询rise会员信息")
                .memo(riseMember != null ? new Gson().toJson(riseMember) : "none");
        operationLogService.log(operationLog);

        if (riseMember != null) {
            return WebUtils.result(riseMember.simple());
        } else {
            return WebUtils.result(null);
        }
    }

    @RequestMapping("/global/notify")
    public ResponseEntity<Map<String, Object>> notifyExpire(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        RiseMember riseMember = new RiseMember();
        boolean expiredRiseMemberInSevenDays = riseMemberService.expiredRiseMemberInSevenDays(loginUser.getId());
        boolean expiredRiseMember = riseMemberService.expiredRiseMember(loginUser.getId());
        riseMember.setExpiredInSevenDays(expiredRiseMemberInSevenDays);
        riseMember.setExpired(expiredRiseMember);
        riseMember.setShowGlobalNotify(expiredRiseMember || expiredRiseMemberInSevenDays);
        // riseMember.setShowGlobalNotify(customerService.hasAnnualSummaryAuthority(loginUser.getId()));
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

    @RequestMapping(value = "/update/weixinId", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updateWeixinId(LoginUser loginUser, @RequestBody WeixinDto weixinDto) {
        Assert.notNull(loginUser, "用户不能为空");
        accountService.updateWeixinId(loginUser.getId(), weixinDto.getWeixinId());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("更新微信id")
                .memo(weixinDto.getWeixinId());
        operationLogService.log(operationLog);
        return WebUtils.success();
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
//        BibleRefreshListDto<ForumQuestion> result = new BibleRefreshListDto<>();
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

    @RequestMapping(value = "/check/subscribe/{key}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> goQuestionSubmitPage(GuestUser loginUser, @PathVariable(value = "key") String key, @RequestParam String callback) {
        OperationLog operationLog = OperationLog.create()
                .openid(loginUser != null ? loginUser.getOpenId() : null)
                .module("用户信息")
                .function("服务号")
                .action("检查是否关注")
                .memo(key);
        operationLogService.log(operationLog);
        if (loginUser == null || loginUser.getSubscribe() == null || loginUser.getSubscribe() == 0) {
            // 没有loginUser，即没有关注,创建一个img
            String qrCode = accountService.createSubscribePush(loginUser != null ? loginUser.getOpenId() : null, callback, key);
            return WebUtils.result(qrCode);
        } else {
            return WebUtils.success();
        }
    }

    @RequestMapping(value = "/annual/summary")
    public ResponseEntity<Map<String, Object>> getAnnualSummary(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create()
                .openid(loginUser.getOpenId())
                .module("用户信息")
                .function("学习")
                .action("年度报告");
        operationLogService.log(operationLog);
        AnnualSummary annualSummary = customerService.loadUserAnnualSummary(loginUser.getId());
        if (annualSummary == null) {
            return WebUtils.error("您还没有年度报告");
        } else {
            return WebUtils.result(annualSummary);
        }
    }
}
