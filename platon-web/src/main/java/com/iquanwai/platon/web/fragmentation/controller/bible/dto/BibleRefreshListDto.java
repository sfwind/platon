package com.iquanwai.platon.web.fragmentation.controller.bible.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class BibleRefreshListDto<T>{
    private List<T> list;
    private Boolean firstOpen;
    private Boolean isDateEnd;
}
