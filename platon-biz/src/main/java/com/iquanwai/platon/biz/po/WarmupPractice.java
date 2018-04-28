package com.iquanwai.platon.biz.po;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupComment;
import com.iquanwai.platon.biz.domain.fragmentation.practice.WarmupDiscussDistrict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;

import java.util.List;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("选择题")
public class WarmupPractice implements Cloneable {
    private int id;
    @ApiModelProperty("题干")
    private String question; 
    @ApiModelProperty("题型")
    private Integer type; 
    @ApiModelProperty("解析")
    private String analysis; 
    @ApiModelProperty("图片")
    private String pic; 
    @ApiModelProperty("难易度（1-容易，2-普通，3-困难）")
    private Integer difficulty; 
    @ApiModelProperty("知识点id")
    private Integer knowledgeId; 
    @ApiModelProperty("是否删除(0-否,1-是)")
    private Boolean del; 
    @ApiModelProperty("课程id")
    private Integer problemId; 
    @ApiModelProperty("出现顺序")
    private Integer sequence; 
    @ApiModelProperty("是否例题(0-否,1-是)")
    private Boolean example; 
    @ApiModelProperty("任务唯一编号")
    private String practiceUid; 
    @ApiModelProperty("分值")
    private Integer score; 
    @ApiModelProperty("所有选项")
    private List<Choice> choiceList; 
    @ApiModelProperty("巩固练习讨论")
    private List<WarmupComment> discussList; 
    @ApiModelProperty("用户选择选项")
    private List<Integer> choice; 
    @ApiModelProperty("知识点")
    private Knowledge knowledge;
    @ApiModelProperty("选择题讨论区")
    private WarmupDiscussDistrict warmupDiscussDistrict;

    @Override
    public WarmupPractice clone() throws CloneNotSupportedException {
        WarmupPractice dest = new WarmupPractice();
        try {
            BeanUtils.copyProperties(dest, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Choice> choices = Lists.newArrayList();
        this.choiceList.forEach(choice -> {
            Choice newOne = new Choice();
            try {
                BeanUtils.copyProperties(newOne, choice);
            } catch (Exception e) {
                e.printStackTrace();
            }
            choices.add(newOne);
        });
        dest.setChoiceList(choices);
        dest.setChoice(null);
        dest.setDiscussList(null);
        return dest;
    }
}
