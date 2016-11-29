package com.iquanwai.platon.biz.domain.weixin.account;

import lombok.Data;

/**
 * Created by justin on 16/9/27.
 */
@Data
public class UsersDto {
    private Integer total;
    private Integer count;
    private DataDto data;
    private String next_openid;
}
