package com.iquanwai.platon.web.course;

import com.iquanwai.platon.biz.domain.fragmentation.plan.GeneratePlanService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.iquanwai.platon.web.resolver.UnionUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.support.Assert;

import java.util.Map;

@RestController
@RequestMapping("/rise")
public class OpenCourseController {

    @Autowired
    private GeneratePlanService generatePlanService;


    @RequestMapping(value = "/open/course",method = RequestMethod.GET)
    public ResponseEntity<Map<String,Object>> openCourse(UnionUser unionUser, @RequestParam("problemId")Integer problemId){
        Assert.notNull(unionUser);

        if(unionUser.getId()==null){
            return WebUtils.error("请先关注圈外测试");
        }

        generatePlanService.magicOpenProblem(unionUser.getId(),problemId,null,null,false);

        return WebUtils.success();
    }

}
