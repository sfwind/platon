package com.iquanwai.platon.biz.po;

import com.iquanwai.platon.biz.util.ConfigUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
@ApiModel("知识点")
public class Knowledge {
    private int id;
    @ApiModelProperty("知识点")
    private String knowledge; 
    @ApiModelProperty("知识点所属步骤")
    private String step; 
    @ApiModelProperty("描述")
    private String description;
    @ApiModelProperty("作用")
    private String analysis; 
    @ApiModelProperty("方法")
    private String means; 
    @ApiModelProperty("要点")
    private String keynote; 
    @ApiModelProperty("作用图片")
    private String analysisPic; 
    @ApiModelProperty("方法图片")
    private String meansPic; 
    @ApiModelProperty("要点图片")
    private String keynotePic; 
    @ApiModelProperty("图片链接")
    private String pic; 
    @ApiModelProperty("作用语音")
    private String analysisAudio; 
    @ApiModelProperty("作用语音id")
    private Integer analysisAudioId;
    @ApiModelProperty("作用语音文字稿")
    private String analysisAudioWords;
    @ApiModelProperty("方法语音")
    private String meansAudio; 
    @ApiModelProperty("方法语音id")
    private Integer meansAudioId;
    @ApiModelProperty("方法语音文字稿")
    private String meansAudioWords;
    @ApiModelProperty("要点语音")
    private String keynoteAudio; 
    @ApiModelProperty("要点语音id")
    private Integer keynoteAudioId;
    @ApiModelProperty("要点语音文字稿")
    private String keynoteAudioWords;
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
    @ApiModelProperty("语音id")
    private Integer audioId;  
    @ApiModelProperty("语音文字")
    private String audioWords; 
    @ApiModelProperty("语音链接")
    private String audio; 
    @ApiModelProperty("是否出现过")
    private Integer appear;
    @ApiModelProperty("例题")
    private WarmupPractice example;

    private static String REVIEW_KNOWLEDGE = ConfigUtils.getIntegratedPracticeIndex();

    public static boolean isReview(Integer knowledgeId) {
        if (knowledgeId == null) {
            return false;
        }
        String[] ids = REVIEW_KNOWLEDGE.split(",");
        for (String id : ids) {
            if (id.equals(knowledgeId.toString())) {
                return true;
            }
        }

        return false;
    }

}
