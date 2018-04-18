package com.iquanwai.platon.web.personal;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.cache.CacheService;
import com.iquanwai.platon.biz.domain.common.customer.CustomerService;
import com.iquanwai.platon.biz.domain.fragmentation.certificate.CertificateService;
import com.iquanwai.platon.biz.domain.fragmentation.event.EventWallService;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemCard;
import com.iquanwai.platon.biz.domain.fragmentation.plan.ProblemService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.StudyService;
import com.iquanwai.platon.biz.domain.user.UserInfoService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.Coupon;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Problem;
import com.iquanwai.platon.biz.po.RiseCertificate;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.EventWall;
import com.iquanwai.platon.biz.po.common.Feedback;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Region;
import com.iquanwai.platon.biz.po.user.UserInfo;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.fragmentation.dto.RiseDto;
import com.iquanwai.platon.web.personal.dto.AreaDto;
import com.iquanwai.platon.web.personal.dto.Base64ConvertDto;
import com.iquanwai.platon.web.personal.dto.CertificateDto;
import com.iquanwai.platon.web.personal.dto.CertificateListDto;
import com.iquanwai.platon.web.personal.dto.CouponDto;
import com.iquanwai.platon.web.personal.dto.CustomerInfoDto;
import com.iquanwai.platon.web.personal.dto.NicknameDto;
import com.iquanwai.platon.web.personal.dto.PlanDto;
import com.iquanwai.platon.web.personal.dto.PlanListDto;
import com.iquanwai.platon.web.personal.dto.ProfileDto;
import com.iquanwai.platon.web.personal.dto.RegionDto;
import com.iquanwai.platon.web.personal.dto.RiseMemberStatusDto;
import com.iquanwai.platon.web.personal.dto.UserStudyDto;
import com.iquanwai.platon.web.personal.dto.ValidCodeDto;
import com.iquanwai.platon.web.personal.dto.WeixinDto;
import com.iquanwai.platon.web.resolver.GuestUser;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.resolver.UnionUserService;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    private PlanService planService;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private EventWallService eventWallService;
    @Autowired
    private CertificateService certificateService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private UnionUserService unionUserService;
    @Autowired
    private StudyService studyService;
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ApiOperation("查询小程序用户基本信息")
    public ResponseEntity<Map<String, Object>> getUserInfo(UnionUser unionUser) {


        CustomerInfoDto profile = new CustomerInfoDto();
        Profile profilePojo = accountService.getProfile(unionUser.getId());
        profile.setRiseId(profilePojo.getRiseId());
        profile.setNickname(unionUser.getNickName());
        profile.setHeadimgurl(unionUser.getHeadImgUrl());

        profile.setIsAsst(accountService.getAssist(unionUser.getId()) != null);
        List<RiseMember> members = riseMemberManager.member(unionUser.getId());
        if (members.isEmpty()) {
            profile.setRoleNames(Lists.newArrayList("0"));
        } else {
            profile.setRoleNames(members.stream().map(RiseMember::getMemberTypeId).map(Object::toString).collect(Collectors.toList()));
        }

        Map<String, String> propsValues = customerService.loadClassGroup(unionUser.getId());
        profile.setClassGroupMaps(propsValues);

        return WebUtils.result(profile);
    }

    @RequestMapping(value = "/profile/info", method = RequestMethod.GET)
    @ApiOperation("查询个人中心首页信息")
    public ResponseEntity<Map<String, Object>> getProfileInfo(UnionUser unionUser) {
        UserStudyDto userStudyDto = new UserStudyDto();
        Integer profileId = unionUser.getId();
        Profile profile = accountService.getProfile(profileId);
        userStudyDto.setNickName(profile.getNickname());
        userStudyDto.setHeadImgUrl(profile.getHeadimgurl());
        userStudyDto.setMemberId(profile.getMemberId());

        userStudyDto.setIsProMember(riseMemberManager.proMember(profileId)!=null);

        userStudyDto.setShowShare(CollectionUtils.isNotEmpty(riseMemberManager.businessSchoolMember(profileId)));

        userStudyDto.setCardSum(problemService.loadProblemCardsList(unionUser.getId()).stream().map(ProblemCard::getCompleteCount).reduce(0, Integer::sum));
        userStudyDto.setPoint(profile.getPoint());
        Integer certificateSum = certificateService.getCertificates(profileId).size();
        userStudyDto.setCertificateSum(certificateSum);
        List<Coupon> coupons = accountService.loadCoupons(profileId);
        userStudyDto.setCouponSum(coupons.stream().map(Coupon::getAmount).reduce(0, Integer::sum));
        return WebUtils.result(userStudyDto);
    }

    @RequestMapping(value = "/event/list", method = RequestMethod.GET)
    @ApiOperation("查询活动列表")
    public ResponseEntity<Map<String, Object>> getEventList(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        List<EventWall> eventWall = eventWallService.getEventWall(unionUser.getId());

        return WebUtils.result(eventWall);
    }

    @RequestMapping(value = "/account", method = RequestMethod.GET)
    @ApiOperation("查询账号信息")
    public ResponseEntity<Map<String, Object>> loadRiseInfo(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");

        Profile profile = accountService.getProfile(unionUser.getId());

        RiseDto riseDto = new RiseDto();
        riseDto.setRiseId(profile.getRiseId());
        riseDto.setMobile(profile.getMobileNo());
        riseDto.setIsRiseMember(profile.getRiseMember() == Constants.RISE_MEMBER.MEMBERSHIP);
        riseDto.setNickName(profile.getNickname());
        riseDto.setHeadImgUrl(profile.getHeadimgurl());
        riseDto.setMemberId(profile.getMemberId());

        RiseMember riseMember = accountService.getValidRiseMember(unionUser.getId());
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

        Integer profileId = unionUser.getId();
        ProfileDto profileDto = new ProfileDto();

        Profile profile = accountService.getProfile(profileId);

        BeanUtils.copyProperties(profile, profileDto);
        UserInfo userInfo = userInfoService.loadByProfileId(profileId);
        if (userInfo != null) {
            BeanUtils.copyProperties(userInfo, profileDto);
            profileDto.setMobileNo(userInfo.getReceiverMobile());

        }

        RiseMember riseMember = accountService.getValidRiseMember(unionUser.getId());
        if (riseMember != null) {
            profileDto.setMemberTypeId(riseMember.getMemberTypeId());
        } else {
            profileDto.setMemberTypeId(0);
        }
        Boolean isElite = CollectionUtils.isNotEmpty(riseMemberManager.businessSchoolMember(profileId));
        profileDto.setIsShowInfo(isElite);
        Boolean cansSkip = true;

        if (isElite && (userInfo == null || userInfo.getAddress() == null || userInfo.getRealName() == null || userInfo.getReceiver() == null)) {
            cansSkip = false;
        }
        profileDto.setCanSkip(cansSkip);

        // 查询id
        Region city = accountService.loadCityByName(profile.getCity());
        Region province = accountService.loadProvinceByName(profile.getProvince());
        profileDto.setCityId(city == null ? null : city.getId());
        profileDto.setProvinceId(province == null ? null : province.getId());
        boolean bindMobile = true;
        //判断是否绑定手机号或者填写微信号
        if (userInfo == null || (StringUtils.isEmpty(userInfo.getMobile())) && StringUtils.isEmpty(profile.getWeixinId())) {
            bindMobile = false;
        }
        if (userInfo == null) {
            profileDto.setIsFull(false);
        } else {
            profileDto.setIsFull(userInfo.getIsFull() == 1);
        }
        profileDto.setNickName(profile.getNickname());
        profileDto.setBindMobile(bindMobile);
        profileDto.setScore(ConfigUtils.getProfileFullScore());


        if (profile.getNickname() != null && userInfo != null && userInfo.getWorkingYear() != null && profile.getProvince() != null && profile.getCity() != null && userInfo.getIndustry() != null && userInfo.getFunction() != null) {
            profileDto.setCanSubmit(true);
        } else {
            profileDto.setCanSubmit(false);
        }


        return WebUtils.result(profileDto);
    }

    @RequestMapping(value = "/profile", method = RequestMethod.POST)
    @ApiOperation("提交用户个人信息")
    public ResponseEntity<Map<String, Object>> submitProfile(UnionUser unionUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(unionUser, "用户信息不能为空");
        Profile profile = new Profile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(unionUser.getId());
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(profileDto, userInfo);
        userInfo.setReceiverMobile(profileDto.getMobileNo());
        userInfo.setProfileId(unionUser.getId());
        accountService.submitPersonalCenterProfile(profile, userInfo);
        return WebUtils.success();
    }

    @RequestMapping(value = "/new/profile", method = RequestMethod.POST)
    @ApiOperation("提交个人中心信息")
    public ResponseEntity<Map<String, Object>> submitNewProfile(UnionUser unionUser, @RequestBody ProfileDto profileDto) {
        Assert.notNull(unionUser, "用户信息不能为空");
        UserInfo userInfo = new UserInfo();
        Profile profile = new Profile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(unionUser.getId());
        profile.setNickname(profileDto.getNickName());

        BeanUtils.copyProperties(profileDto, userInfo);
        userInfo.setReceiverMobile(profileDto.getMobileNo());
        userInfo.setProfileId(unionUser.getId());
        accountService.submitPersonalCenterProfile(profile, userInfo);
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
    @ApiImplicitParams({@ApiImplicitParam(name = "headImgUrl", value = "头像图片链接")})
    public ResponseEntity<Map<String, Object>> updateHeadImg(UnionUser unionUser, @RequestParam("headImgUrl") String headImgUrl) {
        Assert.notNull(unionUser, "登录用户不能为空");
        Assert.notNull(headImgUrl, "上传头像不能为空");

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

        Profile profile = new Profile();
        BeanUtils.copyProperties(profileDto, profile);
        profile.setId(unionUser.getId());
        accountService.submitCertificateProfile(profile);
        return WebUtils.success();
    }

    @RequestMapping(value = "/certificate/{certificateNo}", method = RequestMethod.GET)
    @ApiOperation("获取证书")
    @ApiImplicitParams({@ApiImplicitParam(name = "certificateNo", value = "证书编号")})
    public ResponseEntity<Map<String, Object>> getCertificate(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");

        RiseCertificate riseCertificate = certificateService.getCertificate(certificateNo);
        if (riseCertificate.getDel()) {
            return WebUtils.error("证书已失效");
        } else {
            return WebUtils.result(riseCertificate);
        }
    }

    @RequestMapping(value = "/certificate/download/{certificateNo}", method = RequestMethod.GET)
    @ApiOperation("下载证书")
    @ApiImplicitParams({@ApiImplicitParam(name = "certificateNo", value = "证书编号")})
    public ResponseEntity<Map<String, Object>> getCertificateAndNext(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");

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
    @ApiImplicitParams({@ApiImplicitParam(name = "certificateNo", value = "证书编号")})
    public ResponseEntity<Map<String, Object>> updateCertificateDownloadTime(UnionUser unionUser, @PathVariable String certificateNo) {
        Assert.notNull(unionUser, "用户信息不能为空");

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

        return WebUtils.result(regionDto);
    }

    @RequestMapping(value = {"/plans", "/pc/plans"}, method = RequestMethod.GET)
    @ApiOperation("查询课程信息")
    public ResponseEntity<Map<String, Object>> loadUserPlans(UnionUser unionUser) {
        if (unionUser == null) {
            return WebUtils.error(401, "未登录");
        }

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

    @RequestMapping(value = "/finished/plans", method = RequestMethod.GET)
    @ApiOperation("查询用户已经完成的课程")
    public ResponseEntity<Map<String, Object>> loadFinishedPlans(UnionUser unionUser) {
        List<ImprovementPlan> plans = planService.getPlans(unionUser.getId());
        PlanListDto list = new PlanListDto();
        List<PlanDto> donePlans = Lists.newArrayList();
        plans.forEach(item -> {
            PlanDto planDto = new PlanDto();
            planDto.setPlanId(item.getId());
            planDto.setName(problemService.getProblem(item.getProblemId()).getProblem());
            planDto.setPoint(item.getPoint());
            planDto.setProblemId(item.getProblemId());
            if (item.getStatus() == ImprovementPlan.CLOSE) {
                donePlans.add(planDto);
            }
            planDto.setProblem(cacheService.getProblem(item.getProblemId()).simple());
        });
        list.setDonePlans(donePlans);
        // 查询riseId
        Profile profile = accountService.getProfile(unionUser.getId());
        list.setRiseId(profile.getRiseId());
        list.setPoint(profile.getPoint());
        return WebUtils.result(list);
    }

    @RequestMapping(value = "/member", method = RequestMethod.GET)
    @ApiOperation("查询用户会员信息")
    public ResponseEntity<Map<String, Object>> riseMember(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        List<RiseMember> riseMember = riseMemberManager.member(unionUser.getId());
        //TODO: 待验证
        if (CollectionUtils.isNotEmpty(riseMember)) {
            return WebUtils.result(riseMember.get(0).simple());
        } else {
            return WebUtils.result(null);
        }
    }

    @RequestMapping(value = "/global/notify", method = RequestMethod.GET)
    @ApiOperation("用户会员期过期提醒查询")
    public ResponseEntity<Map<String, Object>> notifyExpire(UnionUser unionUser) {
        Assert.notNull(unionUser, "用户不能为空");
        RiseMember riseMember = new RiseMember();
        boolean expiredRiseMemberInSevenDays = riseMemberManager.expiredRiseMemberInSomeDays(unionUser.getId(), 15);
        boolean expiredRiseMember = riseMemberManager.expiredRiseMember(unionUser.getId());
        riseMember.setExpiredInSevenDays(expiredRiseMemberInSevenDays);
        riseMember.setExpired(expiredRiseMember);
        riseMember.setShowGlobalNotify(expiredRiseMember || expiredRiseMemberInSevenDays);
        return WebUtils.result(riseMember);
    }

    @RequestMapping(value = "/valid/sms", method = RequestMethod.POST)
    @ApiOperation("短信验证")
    public ResponseEntity<Map<String, Object>> validCode(UnionUser unionUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(unionUser, "用户不能为空");
        boolean result = accountService.validCode(validCodeDto.getCode(), unionUser.getId());

        return result ? WebUtils.success() : WebUtils.error("验证失败");
    }

    @RequestMapping(value = "/send/valid/code", method = RequestMethod.POST)
    @ApiOperation("发送验证码")
    public ResponseEntity<Map<String, Object>> sendCode(UnionUser unionUser, @RequestBody ValidCodeDto validCodeDto) {
        Assert.notNull(unionUser, "用户不能为空");
        Pair<Boolean, String> result = accountService.sendValidCode(validCodeDto.getPhone(),
                unionUser.getId(), validCodeDto.getAreaCode());

        return result.getLeft() ? WebUtils.success() : WebUtils.error(result.getRight());
    }

    @RequestMapping(value = "/update/weixinId", method = RequestMethod.POST)
    @ApiOperation("更新微信id")
    public ResponseEntity<Map<String, Object>> updateWeixinId(UnionUser unionUser, @RequestBody WeixinDto weixinDto) {
        Assert.notNull(unionUser, "用户不能为空");
        accountService.updateWeixinId(unionUser.getId(), weixinDto.getWeixinId());

        return WebUtils.success();
    }

    @RequestMapping(value = "/check/subscribe/{key}", method = RequestMethod.GET)
    @ApiOperation("检查用户是否关注")
    public ResponseEntity<Map<String, Object>> goQuestionSubmitPage(GuestUser unionUser, @PathVariable(value = "key") String key, @RequestParam String callback) {
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
        CouponDto couponDto = new CouponDto();
        List<Coupon> coupons = accountService.loadCoupons(unionUser.getId());
        couponDto.setCoupons(coupons);
        couponDto.setTotal(coupons.stream().collect(Collectors.summingInt(Coupon::getAmount)));

        return WebUtils.result(couponDto);
    }

    @RequestMapping(value = "/get/certificate", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> getCertificates(UnionUser unionUser) {
        List<RiseCertificate> certificateList = certificateService.getCertificates(unionUser.getId());

        List<RiseCertificate> finishList = certificateList.stream().filter(riseCertificate -> riseCertificate.getType() == 5).collect(Collectors.toList());
        List<RiseCertificate> gradeList = certificateList.stream().filter(riseCertificate -> riseCertificate.getType() == 1 || riseCertificate.getType() == 2 || riseCertificate.getType() == 3 || riseCertificate.getType() == 4 || riseCertificate.getType() == 6 || riseCertificate.getType() == 7).collect(Collectors.toList());

        List<CertificateDto> finishDtos = Lists.newArrayList();
        List<CertificateDto> gradeDtos = Lists.newArrayList();

        List<Problem> problems = problemService.loadProblems();

        finishList.forEach(riseCertificate -> {
            CertificateDto certificateDto = new CertificateDto();
            BeanUtils.copyProperties(riseCertificate, certificateDto);
            certificateDto.setTypeName(getCertificateName(riseCertificate.getType()));

            List<Problem> problems1 = problems.stream().filter(problem -> riseCertificate.getProblemName().equals(problem.getProblem())).collect(Collectors.toList());
            if (problems1.size() > 0) {
                certificateDto.setAbbreviation(problems1.get(0).getAbbreviation());
            } else {
                certificateDto.setAbbreviation(riseCertificate.getProblemName());
            }
            finishDtos.add(certificateDto);
        });

        gradeList.forEach(riseCertificate -> {
            CertificateDto certificateDto = new CertificateDto();
            BeanUtils.copyProperties(riseCertificate, certificateDto);
            certificateDto.setTypeName(getCertificateName(riseCertificate.getType()));

            List<Problem> problems1 = problems.stream().filter(problem -> riseCertificate.getProblemName().equals(problem.getProblem())).collect(Collectors.toList());
            if (problems1.size() > 0) {
                certificateDto.setAbbreviation(problems1.get(0).getAbbreviation());
            } else {
                certificateDto.setAbbreviation(riseCertificate.getProblemName());
            }
            gradeDtos.add(certificateDto);
        });

        CertificateListDto certificateListDto = new CertificateListDto();
        certificateListDto.setFinishDto(finishDtos);
        certificateListDto.setGradeDto(gradeDtos);

        return WebUtils.result(certificateListDto);
    }

    @RequestMapping(value = "/get/countdown/status", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadCurrentRiseMember(UnionUser unionUser) {
        RiseMemberStatusDto riseMemberStatusDto = new RiseMemberStatusDto();
        Pair<Boolean, Integer> result = studyService.shouldGoCountDownPage(unionUser.getId());
        riseMemberStatusDto.setGoCountDownPage(result.getLeft());
        riseMemberStatusDto.setMemberTypeId(result.getRight());
        return WebUtils.result(riseMemberStatusDto);
    }

    private String getCertificateName(Integer type) {
        if (type == 1) {
            return "优秀班长";
        }
        if (type == 2) {
            return "优秀组长";
        }
        if (type == 3) {
            return "优秀学员";
        }
        if (type == 4) {
            return "优秀团队";
        }
        if (type == 5) {
            return "结课证书";
        }
        if (type == 6) {
            return "优秀助教";
        }
        if (type == 7) {
            return "优秀班委";
        }
        return "未知类型";
    }

    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
    @ApiOperation("用户提交意见反馈")
    public ResponseEntity<Map<String, Object>> submitFeedback(UnionUser unionUser, @RequestBody Feedback feedback) {
        feedback.setProfileId(unionUser.getId());
        customerService.sendFeedback(feedback);

        return WebUtils.success();
    }

}
