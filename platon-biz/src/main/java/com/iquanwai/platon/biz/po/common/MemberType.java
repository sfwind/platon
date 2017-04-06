package com.iquanwai.platon.biz.po.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by nethunder on 2017/4/6.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberType {
    private Integer id; // MemberId
    private Double fee; // 会员费用
    private String name; // 描述
}
