package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

/**
 * Created by nethunder on 2017/5/18.
 */
@Data
public class ProblemExploreDto {
    private Integer id;
    private String catalog;
    private Integer catalogId;
    private String catalogDescribe;
    private String subCatalog;
    private Integer subCatalogId;
    private String author;
    private String difficulty;
    private String pic;
    private String name;
    private Integer chosenPersonCount; // 该门小课学习的人数
}
