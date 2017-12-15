package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.ArticleLabel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by nethunder on 2017/1/14.
 */
@Data
public class RiseWorkInfoDto {
    private String title; //文章标题
    private String userName; //作者名称
    private String submitUpdateTime; //提交更新时间
    private String headImage; //作者头像
    private String content;  //文章内容
    private Integer voteCount;  // 赞数
    private Integer commentCount; //评论数
    private Integer submitId; //提交id
    private Integer type; //类型（1-小目标,2-应用练习,3-小课分享）
    private Integer voteStatus; //赞状态
    private Date publishTime; //发布时间
    private String desc; //文章描述
    private Integer priority; //排序优先级
    private Boolean perfect; //是否是精华
    private Integer problemId; //课程id
    private Integer authorType; //作者类型
    private Boolean isMine; //是否是本人文章
    private List<ArticleLabel> labelList; //标签列表
    @Deprecated
    private List<String> picList;
    private Integer role; //作者角色
    private String signature; //作者签名
    private Integer requestCommentCount; //求点评字数
    private Boolean request; //是否已经求点评
    private Boolean feedback; //是否已经点评
}
