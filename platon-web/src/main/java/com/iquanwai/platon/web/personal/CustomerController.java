package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.common.customer.RiseMemberService;
import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.event.EventWallService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.EventWall;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.RiseDto;
import com.iquanwai.platon.web.personal.dto.*;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserService;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.*;
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
@Api(description = "用户相关Controller")
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
    private CertificateService certificateService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UnionUserService unionUserService;

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation("查询小程序用户基本信息")
    public ResponseEntity<Map<String, Object>> getUserInfo(UnionUser unionUser) {
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("小程序")
                .function("获取用户基本信息")
                .action("查询");
        operationLogService.log(operationLog);

        Profile profile = new Profile();
        profile.setNickname(unionUser.getNickName());
        profile.setHeadimgurl(unionUser.getHeadImgUrl());
        return WebUtils.result(profile);
    }


    @RequestMapping(value = "/profile/info",method = RequestMethod.GET)
    @ApiOperation("查询个人中心首页信息")
    public ResponseEntity<Map<String,Object>> getProfileInfo(UnionUser unionUser){
        UserStudyDto userStudyDto = new UserStudyDto();
        Integer profileId = unionUser.getId();
        Profile profile = accountService.getProfile(profileId);
        userStudyDto.setNickName(profile.getNickname());
        userStudyDto.setHeadImgUrl(profile.getHeadimgurl());
        RiseClassMember riseClassMember = accountService.loadDisplayRiseClassMember(profileId);
        userStudyDto.setMemberId(riseClassMember.getMemberId());
        userStudyDto.setClassName(riseClassMember.getClassName());
        //TODO:知识卡张数和荣誉证书张数
        userStudyDto.setCardSum(problemService.getFinishedCards(profileId));

        return WebUtils.result(userStudyDto);
    }


    @RequestMapping(value = "/event/list", method = RequestMethod.GET)
    @ApiOperation("查询活动列表")
    public ResponseEntity<Map<String, Object>> getEventList(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("活动墙")
                .function("活动墙")
                .action("查询");
        operationLogService.log(operationLog);
        List<EventWall> eventWall = eventWallService.getEventWall(unionUser.getId());

        return WebUtils.result(eventWall);
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @ApiOperation("查询账号信息")
    public ResponseEntity<Map<String, Object>> loadRiseInfo(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("账号")
                .action("查询账号信息");
        operationLogService.log(operationLog);

        Profile profile = accountService.getProfile(unionUser.getId());

        RiseDto riseDto = new RiseDto();
        riseDto.setRiseId(profile.getRiseId());
        riseDto.setMobile(profile.getMobileNo());
        riseDto.setIsRiseMember(profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP);
        riseDto.setNickName(profile.getNickname());
        riseDto.setHeadImgUrl(profile.getHeadimgurl());

        RiseClassMember riseClassMember = accountService.loadDisplayRiseClassMember(unionUser.getId());
        if (riseClassMember != null) {
            riseDto.setMemberId(riseClassMember.getMemberId());
        }

        RiseMember riseMember = riseMemberService.getRiseMember(unionUser.getId());
        if (riseMember != null) {
            riseDto.setMemberType(riseMember.getName());
        }
        List<Coupon> coupons = accountService.loadCoupons(profile.getId());
        riseDto.setCoupons(coupons);

        return WebUtils.result(riseDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ApiOperation("查询圈外用户个人信息")
    public ResponseEntity<Map<String, Object>> loadProfile(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("加载个人信息");
        operationLogService.log(operationLog);
        ProfileDto profileDto = new ProfileDto();
        Profile account = accountService.getProfile(unionUser.getId());

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
    @ApiOperation("提交用户个人信息")
    public ResponseEntity<Map<String, Object>> submitProfile(UnionUser unionUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("个人信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile profile = new Profile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(unionUser.getId());
        accountService.submitPersonalCenterProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/profile/headImg/upload", method = RequestMethod.POST)
    @ApiOperation("上传个人头像")
    public ResponseEntity<Map<String, Object>> updateHeadImg(UnionUser unionUser, @RequestParam("file") MultipartFile file) {
        Long fileSize = file.getSize();
        if (fileSize > 5 * 1000 * 1000) { // 文件图片大于 5M
            return WebUtils.error("文件内容过大");
        }
        String fileName = file.getOriginalFilename();
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("头像修改")
                .action("上传头像");
        operationLogService.log(operationLog);
        String imageUrl = null;
        try {
            imageUrl = customerService.uploadHeadImage(unionUser.getId(), fileName, file.getInputStream());
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        if (imageUrl != null) {
            return WebUtils.result(imageUrl);
        } else {
            return WebUtils.error("头像上传失败");
        }
    }

    @RequestMapping(value = "/profile/headImg/update", method = RequestMethod.POST)
    @ApiOperation("更新个人头像")
    @ApiParam(name = "headImgUrl", value = "头像图片链接")
    public ResponseEntity<Map<String, Object>> updateHeadImg(UnionUser unionUser, @RequestParam("headImgUrl") String headImgUrl) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(headImgUrl, "上传头像不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("头像修改")
                .action("更新头像");
        operationLogService.log(operationLog);
        int updateResult = customerService.updateHeadImageUrl(unionUser.getId(), headImgUrl);
        if (updateResult > 0) {
            unionUserService.updateUserByUnionId(unionUser.getUnionId());
            return WebUtils.success();
        } else {
            return WebUtils.error("头像更新失败");
        }
    }

    @RequestMapping(value = "/profile/nickname/update", method = RequestMethod.POST)
    @ApiOperation("提交昵称")
    public ResponseEntity<Map<String, Object>> updateNickName(UnionUser unionUser, @RequestBody NicknameDto nickname) {
        Assert.notNull(unionUser, "登录用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("昵称修改")
                .action("提交昵称");
        operationLogService.log(operationLog);

        int result = customerService.updateNickName(unionUser.getId(), nickname.getNickname());
        if (result > 0) {
            unionUserService.updateUserByUnionId(unionUser.getUnionId());
            return WebUtils.result("昵称更新成功");
        } else {
            return WebUtils.result("昵称更新失败");
        }
    }

    @RequestMapping(value = "/profile/certificate", method = RequestMethod.POST)
    @ApiOperation("提交个人信息")
    public ResponseEntity<Map<String, Object>> submitCertificateProfile(UnionUser unionUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("提交个人信息");
        operationLogService.log(operationLog);
        Profile profile = new Profile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(unionUser.getId());
        accountService.submitCertificateProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/certificate/{certificateNo}", method = RequestMethod.GET)
    @ApiOperation("获取证书")
    @ApiParam(name = "certificateNo", value = "证书编号")
    public ResponseEntity<Map<String, Object>> getCertificate(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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
    @ApiOperation("下载证书")
    @ApiParam(name = "certificateNo", value = "证书编号")
    public ResponseEntity<Map<String, Object>> getCertificateAndNext(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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
    @ApiOperation("证书转码")
    public ResponseEntity<Map<String, Object>> convertBase64(UnionUser unionUser, @RequestBody Base64ConvertDto base64ConvertDto) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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
    @ApiOperation("下载课程证书完毕")
    @ApiParam(name = "certificateNo", value = "证书编号")
    public ResponseEntity<Map<String, Object>> updateCertificateDownloadTime(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("证书信息")
                .action("证书下载完成");
        operationLogService.log(operationLog);
        certificateService.updateDownloadTime(certificateNo);
        return WebUtils.success();
    }

    @RequestMapping(value = "/region", method = RequestMethod.GET)
    @ApiOperation("加载地区信息")
    public ResponseEntity<Map<String, Object>> loadRegion(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        List<Region> provinces = accountService.loadAllProvinces();
        List<Region> cities = accountService.loadCities();
        RegionDto regionDto = new RegionDto();
        regionDto.setProvinceList(provinces.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        regionDto.setCityList(cities.stream().map(item -> new AreaDto(item.getId() + "", item.getName(), item.getParentId() + "")).collect(Collectors.toList()));
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("地区信息")
                .action("加载地区信息");
        operationLogService.log(operationLog);
        return WebUtils.result(regionDto);
    }

    @RequestMapping(value = {"/plans", "/pc/plans"}, method = RequestMethod.GET)
    @ApiOperation("查询课程信息")
    public ResponseEntity<Map<String, Object>> loadUserPlans(UnionUser unionUser) {
        if (unionUser == null) {
            return WebUtils.error(401, "未登录");
        }

        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("个人中心")
                .function("课程")
                .action("查询课程信息");
        operationLogService.log(operationLog);
        List<RiseCertificate> riseCertificates = certificateService.getCertificates(unionUser.getId());
        //清空profileId
        riseCertificates.forEach(riseCertificate -> riseCertificate.setProfileId(null));
        List<ImprovementPlan> plans = planService.getPlans(unionUser.getId());
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
        Profile profile = accountService.getProfile(unionUser.getId());
        list.setRiseId(profile.getRiseId());
        list.setPoint(profile.getPoint());
        // 当前已收藏课程
        List<Problem> problemCollections = problemService.loadProblemCollections(unionUser.getId());
        list.setProblemCollections(problemCollections);
        return WebUtils.result(list);
    }

    @RequestMapping(value = "/member", method = RequestMethod.GET)
    @ApiOperation("查询用户会员信息")
    public ResponseEntity<Map<String, Object>> riseMember(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        RiseMember riseMember = riseMemberService.getRiseMember(unionUser.getId());
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
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

    @RequestMapping(value = "/global/notify", method = RequestMethod.GET)
    @ApiOperation("用户会员期过期提醒查询")
    public ResponseEntity<Map<String, Object>> notifyExpire(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        RiseMember riseMember = new RiseMember();
        boolean expiredRiseMemberInSevenDays = riseMemberService.expiredRiseMemberInSomeDays(unionUser.getId(), 15);
        boolean expiredRiseMember = riseMemberService.expiredRiseMember(unionUser.getId());
        riseMember.setExpiredInSevenDays(expiredRiseMemberInSevenDays);
        riseMember.setExpired(expiredRiseMember);
        riseMember.setShowGlobalNotify(expiredRiseMember || expiredRiseMemberInSevenDays);
        // riseMember.setShowGlobalNotify(customerService.hasAnnualSummaryAuthority(unionUser.getId()));
        return WebUtils.result(riseMember);
    }

    @RequestMapping(value = "/valid/sms", method = RequestMethod.POST)
    @ApiOperation("短信验证")
    public ResponseEntity<Map<String, Object>> validCode(UnionUser unionUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(unionUser, "用户不能为空");
        boolean result = accountService.validCode(validCodeDto.getCode(), unionUser.getId());
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("验证")
                .memo(validCodeDto.getCode() + ":" + result);
        operationLogService.log(operationLog);
        return result ? WebUtils.success() : WebUtils.error("验证失败");
    }

    @RequestMapping(value = "/send/valid/code", method = RequestMethod.POST)
    @ApiOperation("发送验证码")
    public ResponseEntity<Map<String, Object>> sendCode(UnionUser unionUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(unionUser, "用户不能为空");
        Pair<Boolean, String> result = accountService.sendValidCode(validCodeDto.getPhone(),
                unionUser.getId(), validCodeDto.getAreaCode());
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("发送验证码")
                .memo(validCodeDto.getPhone() + ":" + result.getLeft());
        operationLogService.log(operationLog);
        return result.getLeft() ? WebUtils.success() : WebUtils.error(result.getRight());
    }

    @RequestMapping(value = "/update/weixinId", method = RequestMethod.POST)
    @ApiOperation("更新微信id")
    public ResponseEntity<Map<String, Object>> updateWeixinId(UnionUser unionUser, @RequestBody WeixinDto weixinDto) {
        Assert.notNull(unionUser, "用户不能为空");
        accountService.updateWeixinId(unionUser.getId(), weixinDto.getWeixinId());
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("用户信息")
                .function("个人信息")
                .action("更新微信id")
                .memo(weixinDto.getWeixinId());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/check/subscribe/{key}", method = RequestMethod.GET)
    @ApiOperation("检查用户是否关注")
    public ResponseEntity<Map<String, Object>> goQuestionSubmitPage(GuestUser unionUser, @PathVariable(value = "key") String key, @RequestParam String callback) {
        OperationLog operationLog = OperationLog.create()
                .openid(unionUser != null ? unionUser.getOpenId() : null)
                .module("用户信息")
                .function("服务号")
                .action("检查是否关注")
                .memo(key);
        operationLogService.log(operationLog);
        if (unionUser == null || unionUser.getSubscribe() == null || !unionUser.getSubscribe()) {
            // 没有unionUser，即没有关注,创建一个img
            String qrCode = accountService.createSubscribePush(unionUser != null ? unionUser.getOpenId() : null, callback, key);
            return WebUtils.result(qrCode);
        } else {
            return WebUtils.success();
        }
    }

    @RequestMapping(value = "/coupon", method = RequestMethod.GET)
    @ApiOperation("查询用户优惠券")
    public ResponseEntity<Map<String, Object>> getCouponInfo(UnionUser unionUser) {
        OperationLog operationLog = OperationLog.create().openid(unionUser.getOpenId())
                .module("优惠券")
                .function("查询用户优惠券")
                .action("查询");
        operationLogService.log(operationLog);

        CouponDto couponDto = new CouponDto();
        List<Coupon> coupons = accountService.loadCoupons(unionUser.getId());
        couponDto.setCoupons(coupons);
        couponDto.setTotal(coupons.stream().collect(Collectors.summingInt(Coupon::getAmount)));

        return WebUtils.result(couponDto);
    }
}
