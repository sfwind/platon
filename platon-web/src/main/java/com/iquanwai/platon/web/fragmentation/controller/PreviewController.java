package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.po.ProblemPreview;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 2018/4/11.
 */
@RestController
@Api(description = "课前思考相关接口")
@RequestMapping("/rise/practice/preview")
public class PreviewController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;

    @RequestMapping("/start/{practicePlanId}")
    @ApiOperation("加载课前思考")
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> startPreview(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        ProblemPreview problemPreview = practiceService.loadProblemPreview(practicePlanId);
        return WebUtils.result(problemPreview);
    }

    @RequestMapping(value = "/learn/{practicePlanId}", method = RequestMethod.POST)
    @ApiOperation("学习课前思考")
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> learnKnowledge(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        practiceService.learnPracticePlan(unionUser.getId(), practicePlanId);

        return WebUtils.success();
    }
}
