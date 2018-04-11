package com.iquanwai.platon.biz.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by 三十文
 */
@Data
@ApiModel("学员班级数据")
public class ClassMember {

    private Integer id;
    private Integer profileId;
    @ApiModelProperty("班级号")
    private String className;
    @ApiModelProperty("小组号")
    private String groupId;
    @ApiModelProperty("身份类型")
    private String memberTypeId;
    @ApiModelProperty("是否生效")
    private Boolean active;
    @ApiModelProperty("是否删除")
    private Boolean del;

}
