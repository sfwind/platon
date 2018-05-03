package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.po.survey.report.SurveyCategoryInfo;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Data
public class SurveyReportDto {
    public SurveyReportDto() {
        this.namePicPair = Lists.newArrayList();
    }

    private List<SurveyCategoryInfo> categoryInfos;
    private RadarDto mainRadar;
    private Integer otherSurveyCount;
    private Boolean showComplete;
    private Boolean generatedReport;
    private String character;
    private List<NamePic> namePicPair;

    public void addNamePic(Pair<String, String> namePicPair) {
        NamePic namePic = new NamePic();
        namePic.setName(namePicPair.getLeft());
        namePic.setPic(namePicPair.getRight());
        this.namePicPair.add(namePic);
    }

    @Data
    class NamePic {
        private String name;
        private String pic;
    }

}


