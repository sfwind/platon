package com.iquanwai.platon.biz.po;

import com.iquanwai.platon.biz.util.ConfigUtils;
import lombok.Data;

/**
 * Created by justin on 16/12/4.
 */
@Data
public class Knowledge {
    private int id;
    private String knowledge; //知识点
    private String step; //知识点所属步骤
    private String analysis; //作用
    private String means; //方法
    private String keynote; //要点
    private String analysisPic;// 作用图片
    private String meansPic;// 方法图片
    private String keynotePic;// 要点图片
    private String pic; //图片链接
    private String analysisAudio;// 作用语音
    private Integer analysisAudioId;
    private String analysisAudioWords;
    private String meansAudio;// 方法语音
    private Integer meansAudioId;
    private String meansAudioWords;
    private String keynoteAudio;// 要点语音
    private Integer keynoteAudioId;
    private String keynoteAudioWords;
    private String audio; //语音链接
    private Integer videoId;//视频id
    private String videoUrl;//视频url
    private String videoPoster;//视频第一帧
    private String videoWords;//视频文字
    private Integer audioId; // 语音id
    private String audioWords; //语音文字
    private Integer appear; //非db字段,是否出现过
    private WarmupPractice example; //非db字段 例题

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
