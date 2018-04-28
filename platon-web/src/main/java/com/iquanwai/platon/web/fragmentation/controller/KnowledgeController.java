package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.practice.DiscussElementsPair;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PersonalDiscuss;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeDiscussService;
import com.iquanwai.platon.biz.domain.fragmentation.practice.PracticeService;
import com.iquanwai.platon.biz.po.Knowledge;
import com.iquanwai.platon.biz.po.KnowledgeDiscuss;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.biz.util.page.Page;
import com.iquanwai.platon.web.fragmentation.dto.DiscussDistrictDto;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by justin on 2018/2/16.
 */
@RestController
@Api(description = "知识点相关接口")
@RequestMapping("/rise/practice/knowledge")
public class KnowledgeController {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private PracticeDiscussService practiceDiscussService;

    @RequestMapping(value = "/start/{practicePlanId}", method = RequestMethod.GET)
    @ApiOperation(value = "打开知识点", response = Knowledge.class, responseContainer = "List")
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> startKnowledge(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        List<Knowledge> knowledges = practiceService.loadKnowledges(practicePlanId);

        return WebUtils.result(knowledges);
    }

    @RequestMapping(value = "/{knowledgeId}", method = RequestMethod.GET)
    @ApiOperation(value = "加载知识点", response = Knowledge.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "knowledgeId", value = "知识点id")})
    public ResponseEntity<Map<String, Object>> loadKnowledge(UnionUser unionUser, @PathVariable Integer knowledgeId) {
        Assert.notNull(unionUser, "用户不能为空");
        Assert.notNull(knowledgeId, "知识点id不能为空");

        Knowledge knowledge = practiceService.loadKnowledge(knowledgeId);
        return WebUtils.result(knowledge);
    }

    @RequestMapping(value = "/learn/{practicePlanId}", method = RequestMethod.POST)
    @ApiOperation("学习知识点")
    @ApiImplicitParams({@ApiImplicitParam(name = "practicePlanId", value = "练习id")})
    public ResponseEntity<Map<String, Object>> learnKnowledge(UnionUser unionUser, @PathVariable Integer practicePlanId) {
        Assert.notNull(unionUser, "用户不能为空");
        practiceService.learnPracticePlan(unionUser.getId(), practicePlanId);

        return WebUtils.success();
    }

    @RequestMapping(value = "/discuss/{knowledgeId}/{offset}", method = RequestMethod.GET)
    @ApiOperation(value = "加载更多讨论", response = KnowledgeDiscuss.class, responseContainer = "List")
    @ApiImplicitParams({@ApiImplicitParam(name = "knowledgeId", value = "知识点id"),
            @ApiImplicitParam(name = "offset", value = "页数")})
    public ResponseEntity<Map<String, Object>> loadMoreDiscuss(UnionUser unionUser, @PathVariable Integer knowledgeId, @PathVariable Integer offset) {
        Assert.notNull(unionUser, "用户不能为空");
        Page page = new Page();
        page.setPageSize(Constants.DISCUSS_PAGE_SIZE);
        page.setPage(offset);
        List<KnowledgeDiscuss> discusses = practiceDiscussService.loadKnowledgeDiscusses(knowledgeId, page);

        discusses.forEach(knowledgeDiscuss -> {
            knowledgeDiscuss.setIsMine(unionUser.getId().equals(knowledgeDiscuss.getProfileId()));
            knowledgeDiscuss.setReferenceId(knowledgeDiscuss.getKnowledgeId());
        });

        return WebUtils.result(discusses);
    }

    @ApiOperation(value = "获取当前知识点加精过后的讨论", response = DiscussDistrictDto.class)
    @RequestMapping(value = "/priority/discuss/{knowledgeId}", method = RequestMethod.GET)
    @ApiImplicitParams({@ApiImplicitParam(name = "knowledgeId", value = "知识点id")})
    public ResponseEntity<Map<String, Object>> loadKnowledgeDiscuss(UnionUser unionUser, @PathVariable("knowledgeId") Integer knowledgeId) {
        List<PersonalDiscuss> personalDiscusses = practiceDiscussService.loadPersonalKnowledgeDiscussList(unionUser.getId(), knowledgeId);
        List<DiscussElementsPair> elements = practiceDiscussService.loadPriorityKnowledgeDiscuss(knowledgeId);

        DiscussDistrictDto districtDto = new DiscussDistrictDto();
        districtDto.setPersonal(personalDiscusses);
        districtDto.setPriorities(elements);

        return WebUtils.result(districtDto);
    }

    @RequestMapping(value = "/discuss", method = RequestMethod.POST)
    @ApiOperation("提交知识点评论")
    public ResponseEntity<Map<String, Object>> discuss(UnionUser unionUser, @RequestBody KnowledgeDiscuss discussDto) {
        Assert.notNull(unionUser, "用户不能为空");
        if (discussDto.getComment() == null || discussDto.getComment().length() > 1000) {
            logger.error("{} 理解练习讨论字数过长", unionUser.getOpenId());
            return WebUtils.result("您提交的讨论字数过长");
        }

        practiceDiscussService.discussKnowledge(unionUser.getId(), discussDto.getReferenceId(),
                discussDto.getComment(), discussDto.getRepliedId());

        return WebUtils.success();
    }

    @RequestMapping(value = "/discuss/del/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "删除知识点评论", response = String.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "评论id")})
    public ResponseEntity<Map<String, Object>> deleteKnowledgeDiscuss(UnionUser unionUser, @PathVariable Integer id) {
        Assert.notNull(unionUser, "用户不能为空");
        int result = practiceDiscussService.deleteKnowledgeDiscussById(id);
        String respMsg;
        if (result > 0) {
            respMsg = "删除成功";
        } else {
            respMsg = "操作失败";
        }

        return WebUtils.result(respMsg);
    }
}
