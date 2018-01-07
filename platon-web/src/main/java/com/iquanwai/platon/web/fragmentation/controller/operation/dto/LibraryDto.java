package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class LibraryDto {
    private Integer courseCount;
    private Integer knowledgeCount;
    private Integer allRightCount;
    private List<HeadPicDto> assts;
    private List<HeadPicDto> classmates;

    public LibraryDto(){
        this.assts = Lists.newArrayList();
        this.classmates = Lists.newArrayList();
    }

    @Data
    public static class HeadPicDto {
        private String nickName;
        private String headImageUrl;

        public void append(List<HeadPicDto> list) {
            list.add(this);
        }
    }
}


