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
    private String name; // 会员名
    private Double fee; // 会员费用
    private String description; // 描述
    private Integer openMonth; // 会员时长
    private Double initPrice; //初始费用
    private Boolean purchaseSwitch; //开关
    private String startTime; // 开启时间 非DB字段
    private String endTime; // 结束时间 非DB字段

    public MemberType copy() {
        MemberType temp = new MemberType();
        temp.setId(this.id);
        temp.setFee(this.fee);
        temp.setName(this.name);
        temp.setDescription(this.description);
        temp.setInitPrice(this.initPrice);
        temp.setPurchaseSwitch(this.purchaseSwitch);
        temp.setOpenMonth(this.openMonth);
        temp.setStartTime(this.startTime);
        temp.setEndTime(this.endTime);
        return temp;
    }
}
