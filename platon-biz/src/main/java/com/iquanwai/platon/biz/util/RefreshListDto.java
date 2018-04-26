package com.iquanwai.platon.biz.util;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class RefreshListDto<T> {
    @ApiModelProperty("文章列表")
    private List<T> list;
    @ApiModelProperty("是否结尾")
    private boolean end;
}
