package com.iquanwai.platon.web.forum.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RefreshListDto<T> {
    private List<T> list;
    private boolean end;
}
