package com.iquanwai.platon.biz.po;

import com.alibaba.fastjson.JSON;
import com.iquanwai.platon.biz.domain.fragmentation.manager.Chapter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("课程")
public class Problem {
    private int id;
    @ApiModelProperty("课程名称")
    private String problem;
    @ApiModelProperty("头图")
    private String pic; 
    @ApiModelProperty("训练节数")
    private Integer length; 
    @ApiModelProperty("分类")
    private Integer catalogId;  
    @ApiModelProperty("子目录分类")
    private Integer subCatalogId;  
    @ApiModelProperty("讲师")
    private String author;  
    @ApiModelProperty("讲师图片")
    private String authorPic;  
    @ApiModelProperty("难度分")
    private Double difficultyScore; 
    @ApiModelProperty("实用度")
    private Double usefulScore;  
    @ApiModelProperty("描述图片")
    private String descPic;  
    @ApiModelProperty("语音链接")
    private String audio; 
    @ApiModelProperty("语音id")
    private Integer audioId; 
    @ApiModelProperty("语音文字稿")
    private String audioWords; 
    @ApiModelProperty("视频id")
    private Integer videoId;  
    @ApiModelProperty("视频源地址")
    private String videoUrl; 
    @ApiModelProperty("视频第一帧")
    private String videoPoster;
    @ApiModelProperty("视频文字")
    private String videoWords;
    @ApiModelProperty("腾讯云上传得到的视频id")
    private String fileId;
    @ApiModelProperty("适合人群")
    private String who; 
    @ApiModelProperty("如何学习")
    private String how; 
    @ApiModelProperty("为什么学习")
    private String why; 
    @ApiModelProperty("是否删除(0-否,1-是)")
    private Boolean del; 
    @ApiModelProperty("是否是新课程")
    private Boolean newProblem;  
    @ApiModelProperty("试用版（0-否,1-是）")
    private Boolean trial; 
    @ApiModelProperty("分类图示")
    private String categoryPic; 
    @ApiModelProperty("缩略名")
    private String abbreviation;  
    @ApiModelProperty("是否已经上线")
    private Boolean publish;  
    @ApiModelProperty("项目 1-核心能力 2-商业思维")
    private Integer project;  
    @ApiModelProperty("用户是否做过")
    private Boolean done;   
    @ApiModelProperty("用户选过课程（0-未选过,1-正在进行,2-已完成）")
    private Integer status;   
    @ApiModelProperty("课程表")
    private List<Chapter> chapterList;   
    @ApiModelProperty("子类名")
    private String subCatalog;   
    @ApiModelProperty("类名")
    private String catalog;   
    @ApiModelProperty("课程类型 1-主修 2-辅修")
    private Integer problemType;  
    @ApiModelProperty("该门课程学习的人数")
    private Integer chosenPersonCount;  
    @ApiModelProperty("课程对应专项课月份")
    private Integer monthlyCampMonth;  


    public Problem simple() {
        Problem problem = new Problem();
        problem.setId(this.id);
        problem.setProblem(this.problem);
        problem.setPic(this.pic);
        problem.setDel(this.del);
        problem.setNewProblem(this.newProblem);
        problem.setTrial(this.trial);
        problem.setCatalogId(this.catalogId);
        problem.setSubCatalogId(this.subCatalogId);
        problem.setDifficultyScore(this.difficultyScore);
        problem.setStatus(this.status);
        problem.setDone(this.done);
        problem.setSubCatalog(this.subCatalog);
        problem.setCatalog(this.catalog);
        problem.setAbbreviation(this.abbreviation);
        problem.setPublish(this.publish);
        problem.setLength(this.length);
        problem.setProject(this.project);
        return problem;
    }

    public Problem copy() {
        return JSON.parseObject(JSON.toJSONString(this), this.getClass());
    }


    public interface ProjectId{
        int ELITE = 1;
        int THOUGHT = 2;
    }

}
