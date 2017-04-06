package com.iquanwai.platon.web.fragmentation.controller;

import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.domain.fragmentation.plan.PlanService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.ImprovementPlan;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.WarmupPractice;
import com.iquanwai.platon.biz.po.common.MemberType;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.CommonUtils;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.RestfulHelper;
import com.iquanwai.platon.web.fragmentation.dto.CompletePlanDto;
import com.iquanwai.platon.web.fragmentation.dto.PlanLoadDto;
import com.iquanwai.platon.web.fragmentation.dto.PlayIntroduceDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 16/12/8.
 * 训练计划相关的请求处理类
 */
@RestController
@RequestMapping("/rise/plan")
public class PlanController {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    @Autowired
    private PlanService planService;
    @Autowired
    private GeneratePlanService generatePlanService;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private RestfulHelper restfulHelper;

    public static String CREATE_ORDER_URL = ConfigUtils.adapterDomainName()+"/signup/rise/signup";


    @RequestMapping(value = "/choose/problem/{problemId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlan(LoginUser loginUser,
                                                          @PathVariable Integer problemId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan!=null){
            //如果是同一个问题的训练,直接返回训练id
            if(improvementPlan.getProblemId().equals(problemId)){
                return WebUtils.result(improvementPlan.getId());
            }
            LOGGER.error("planId {} is existed", improvementPlan.getId());
            return WebUtils.error("您已经有正在运行的训练,耐心学习吧~");
        }
        Integer planId = generatePlanService.generatePlan(loginUser.getOpenId(), problemId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("问题")
                .function("问题优先级判断")
                .action("选择最需要解决的问题")
                .memo(problemId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(planId);
    }


    @RequestMapping("/play/{planId}")
    public ResponseEntity<Map<String, Object>> planPlayIntroduce(LoginUser loginUser,
                                                                 @PathVariable Integer planId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getPlan(planId);
        if(improvementPlan==null){
            LOGGER.error("planId {} is invalid", planId);
            return WebUtils.error("打开训练玩法介绍失败");
        }

        PlayIntroduceDto playIntroduceDto = new PlayIntroduceDto();

        int interval = DateUtils.interval(improvementPlan.getStartDate(),improvementPlan.getCloseDate());
        playIntroduceDto.setLength(interval);
        interval = DateUtils.interval(improvementPlan.getStartDate(),improvementPlan.getEndDate());
        playIntroduceDto.setTotalSeries(interval);
        DateTime dateTime = new DateTime(improvementPlan.getCloseDate());
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        playIntroduceDto.setEndDate(month + "月" + day + "日");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("玩法")
                .function("训练玩法介绍")
                .action("打开玩法页")
                .memo(planId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(playIntroduceDto);
    }

    @RequestMapping("/load")
    public ResponseEntity<Map<String, Object>> startPlan(LoginUser loginUser){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            // 没有正在进行的计划
            return WebUtils.result(null);
        }

        planService.buildPlanDetail(improvementPlan);
        // openid置为null
        improvementPlan.setOpenid(null);
        improvementPlan.setOpenRise(loginUser.getOpenRise());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("开始训练")
                .action("加载训练")
                .memo(improvementPlan.getId()+"");
        operationLogService.log(operationLog);
        return WebUtils.result(new PlanLoadDto(improvementPlan, loginUser.getRiseMember()));
    }

    @RequestMapping("/history/load/{series}")
    public ResponseEntity<Map<String, Object>> loadHistoryPlan(LoginUser loginUser, @PathVariable Integer series){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getLatestPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            return WebUtils.result(null);
        }

        Integer result = planService.buildSeriesPlanDetail(improvementPlan, series);
        // openid置为null
        improvementPlan.setOpenid(null);
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("开始训练")
                .action("加载历史训练")
                .memo(improvementPlan.getId()+"");
        operationLogService.log(operationLog);
        if(result==-1){
            return WebUtils.error(211,null);
        }else if(result==-2){
            return WebUtils.error(212,null);
        }
        return WebUtils.result(new PlanLoadDto(improvementPlan, loginUser.getRiseMember()));
    }


    @RequestMapping("/knowledge/load/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer knowledgeId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Knowledge knowledge = planService.getKnowledge(knowledgeId);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点回顾")
                .action("打开回顾页")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(knowledge);
    }

    @RequestMapping("/knowledge/learn/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> learnKnowledge(LoginUser loginUser,
                                                             @PathVariable Integer knowledgeId){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        planService.learnKnowledge(knowledgeId, improvementPlan.getId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点回顾")
                .action("学习知识点")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> complete(LoginUser loginUser){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());

        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        Pair<Boolean,Integer> result = planService.completeCheck(improvementPlan);
        CompletePlanDto completePlanDto = new CompletePlanDto();
        completePlanDto.setIscomplete(result.getLeft());
        completePlanDto.setPercent(result.getRight());
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("完成训练")
                .action("完成训练")
                .memo(improvementPlan.getId() + "");
        operationLogService.log(operationLog);
        return WebUtils.result(completePlanDto);
    }

    @RequestMapping(value = "/close", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> close(LoginUser loginUser){

        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }
        planService.completePlan(improvementPlan.getId(), ImprovementPlan.CLOSE);

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("训练计划")
                .function("完成训练")
                .action("完成训练")
                .memo(improvementPlan.getId()+"");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/openrise", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> openRise(LoginUser loginUser){
        Assert.notNull(loginUser,"用户不能为空");
        int count = accountService.updateOpenRise(loginUser.getOpenId());
        if (count > 0) {
            loginUser.setOpenRise(true);
        }
        return WebUtils.success();
    }

    @RequestMapping("/knowledge/example/{knowledgeId}")
    public ResponseEntity<Map<String, Object>> loadKnowledgeExample(LoginUser loginUser,
                                                              @PathVariable Integer knowledgeId){
        Assert.notNull(loginUser, "用户不能为空");
        ImprovementPlan improvementPlan = planService.getRunningPlan(loginUser.getOpenId());
        if(improvementPlan==null){
            LOGGER.error("{} has no improvement plan", loginUser.getOpenId());
            return WebUtils.result("您还没有制定训练计划哦");
        }

        Assert.notNull(loginUser, "用户不能为空");
        WarmupPractice warmupPractice = planService.getExample(knowledgeId, improvementPlan.getProblemId());

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("知识点")
                .function("知识点例题")
                .action("学习例题")
                .memo(knowledgeId.toString());
        operationLogService.log(operationLog);
        return WebUtils.result(warmupPractice);
    }

    @RequestMapping("/welcome")
    public ResponseEntity<Map<String, Object>> welcome(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");

        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("专题")
                .function("打开专题")
                .action("打开欢迎页");
        operationLogService.log(operationLog);
        return WebUtils.result(loginUser.getRiseMember());
    }
    @RequestMapping("/mark/trial")
    public ResponseEntity<Map<String,Object>> markTrialClick(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("欢迎界面")
                .function("打点")
                .action("点击试用版")
                .memo("");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }
    @RequestMapping("/mark/becomerise")
    public ResponseEntity<Map<String,Object>> markBecomeRiseClick(LoginUser loginUser){
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("欢迎界面")
                .function("打点")
                .action("点击成为RISER")
                .memo("");
        operationLogService.log(operationLog);
        return WebUtils.success();
    }



    @RequestMapping( value = "/member/types",method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loadRiseMemberTypes(LoginUser loginUser) {
        Assert.notNull(loginUser, "用户不能为空");
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("会员")
                .action("获取会员说明");
        operationLogService.log(operationLog);
        List<MemberType> memberTypes = accountService.loadMemberTypes();
        if (loginUser.getRiseMember()) {
            // 已经是会员了
            return WebUtils.error(201, memberTypes);
        } else {
            return WebUtils.result(memberTypes);
        }
    }

    @RequestMapping(value = "/member/{memberTypeId}",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> signupRiseMember(LoginUser loginUser,@PathVariable("memberTypeId") Integer memberTypeId){
        Assert.notNull(loginUser, "用户不能为空");
        MemberType memberType = accountService.loadMemberType(memberTypeId);
        if (memberType == null) {
            LOGGER.error("会员类型异常,memberTypeId:{}", memberTypeId);
            return WebUtils.error("该会员类型异常，请联系管理员");
        }
        OperationLog operationLog = OperationLog.create().openid(loginUser.getOpenId())
                .module("RISE")
                .function("会员")
                .action("创建订单")
                .memo(memberTypeId + "");
        operationLogService.log(operationLog);
        // 下单
        Map<String,Object> params = Maps.newHashMap();
        params.put("memberType",memberTypeId);
        params.put("openId", loginUser.getOpenId());
        String responseBody = restfulHelper.httpPost(CREATE_ORDER_URL, CommonUtils.mapToJson(params));
        if (StringUtils.isEmpty(responseBody)) {
            LOGGER.error("创建订单失败");
            return WebUtils.error("创建订单失败，请联系管理员");
        } else {
            LOGGER.info("创建订单:response:{}", responseBody);
            Map<String, Object> responseMap = CommonUtils.jsonToMap(responseBody);
            Double code = Double.parseDouble(responseMap.get("code").toString());
            if (new Double(200).equals(code)) {
                LOGGER.info("创建订单成功,msg:{}", responseMap.get("msg"));
                return WebUtils.result(responseMap.get("msg"));
            } else {
                return WebUtils.error(responseMap.get("msg"));
            }
        }
    }
}
