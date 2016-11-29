package com.iquanwai.platon.biz.domain.weixin.pay;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 16/9/6.
 */
@Data
public class OrderDetail {
    private List<GoodsDetail> goodsDetail = Lists.newArrayList();
}
