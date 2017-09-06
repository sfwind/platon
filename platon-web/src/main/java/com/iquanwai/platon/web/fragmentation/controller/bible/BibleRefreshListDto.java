package com.iquanwai.platon.web.fragmentation.controller.bible;

import com.iquanwai.platon.biz.util.RefreshListDto;
import lombok.Data;

/**
 * Created by nethunder on 2017/3/7.
 */
@Data
public class BibleRefreshListDto<T> extends RefreshListDto<T> {
    private Boolean firstOpen;
}
