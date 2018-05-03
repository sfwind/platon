package com.iquanwai.platon.biz.po.survey.report;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Data
public class SurveyReport {
    private List<SurveyCategoryInfo> categoryInfos;
    private Integer otherSurveyCount;
    private Boolean showComplete;
    private String character;
    private List<Pair<String,String>> namePicPair;
}
