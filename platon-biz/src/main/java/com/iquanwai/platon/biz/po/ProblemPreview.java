package com.iquanwai.platon.biz.po;

import lombok.Data;

/**
 * Created by justin on 2018/4/11.
 */
@Data
public class ProblemPreview {
    private int id;
    private String description; //内容
    private Integer problemScheduleId; //章节id
    private Integer audioId; // 语音id
    private Integer videoId; //视频id
    private String videoUrl; //视频url
    private String videoPoster; //非db字段 视频第一帧
    private String audioWords; //非db字段 语音文字
    private String audio; //非db字段 语音链接
}
