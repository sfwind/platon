package com.iquanwai.platon.web.fragmentation.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/5/12.
 */
@Data
public class ChapterDto {
    private Integer chapterId;
    private String chapter;
    List<SectionDto> sectionList;
}

