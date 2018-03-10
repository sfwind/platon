package com.iquanwai.platon.web.personal.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by justin on 2018/1/9.
 */
@Data
@ApiModel("昵称")
public class NicknameDto {
    @ApiModelProperty("昵称")
    private String nickname;
}
