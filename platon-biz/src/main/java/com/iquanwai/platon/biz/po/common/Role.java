package com.iquanwai.platon.biz.po.common;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by nethunder on 2016/12/28.
 */
@Data
public class Role {
    private Integer id;
    private String name;
    private Integer level;

    //常量值需和数据库持久化数据保持一致
    public static final Integer STRANGE = 0; //陌生人
    public static final Integer STUDENT = 1; //普通用户
    public static final Integer EMPLOYEE = 2; //工作人员
    public static final Integer COACH_LEVEL_1 = 3; //见习教练
    public static final Integer COACH_LEVEL_2 = 4; //教练
    public static final Integer COACH_LEVEL_3 = 5; //高级教练
    public static final Integer COACH_LEVEL_4 = 6; //首席教练
    public static final Integer BIG_VIP = 7; //大V
    public static final Integer CONTENT_ADMIN = 8; //内容管理员
    public static final Integer ADMIN = 9; //管理员
    public static final Integer CONTENT_OPERATION = 10; //内容运营
    public static final Integer COACH_LEVEL_11 = 11; // 候选教练
    public static final Integer COACH_LEVEL_12 = 12; // 候选教练
    public static final Integer COACH_LEVEL_13 = 13; // 候选教练
    public static final Integer COACH_LEVEL_14 = 14; // 候选教练
    public static final Integer COACH_LEVEL_15 = 15; // 候选教练

    private static List<Integer> asstRoleLevel = Lists.newArrayList(COACH_LEVEL_1, COACH_LEVEL_2, COACH_LEVEL_3, COACH_LEVEL_4);

    public static Role stranger() {
        Role stranger = new Role();
        stranger.setId(STRANGE);
        stranger.setName("陌生人");
        stranger.setLevel(STRANGE);
        return stranger;
    }

    public static Role student() {
        Role stranger = new Role();
        stranger.setId(STUDENT);
        stranger.setName("学生");
        stranger.setLevel(STUDENT);
        return stranger;
    }

    // 判断是否是教练
    public static boolean isAsst(int roleLevel) {
        return asstRoleLevel.contains(roleLevel);
    }

    /**
     * 获取助教的 roleId 集合
     */
    public static List<Integer> loadAsstRoleIds() {
        List<Integer> asstIds = Lists.newArrayList();
        asstIds.add(COACH_LEVEL_1);
        asstIds.add(COACH_LEVEL_2);
        asstIds.add(COACH_LEVEL_3);
        asstIds.add(COACH_LEVEL_4);
        asstIds.add(COACH_LEVEL_12);
        asstIds.add(COACH_LEVEL_13);
        asstIds.add(COACH_LEVEL_14);
        asstIds.add(COACH_LEVEL_15);
        return asstIds;
    }

}
