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
    public static final Integer STUDENT = 1; //学生
    public static final Integer COACH_LEVEL_1 = 15; //见习教练
    public static final Integer COACH_LEVEL_2 = 20; //教练
    public static final Integer COACH_LEVEL_3 = 30; //高级教练
    public static final Integer COACH_LEVEL_4 = 40; //首席教练

    public static final Integer BIG_VIP = 9; //大V
    public static final Integer EMPLOYEE = 10; //员工
    public static final Integer CONTENT_OPERATION = 60; //内容运营
    public static final Integer CONTENT_ADMIN = 100; //内容管理员
    public static final Integer ADMIN = 1000; //管理员

    private static List<Integer> asstRoleLevel = Lists.newArrayList(COACH_LEVEL_1, COACH_LEVEL_2,
            COACH_LEVEL_3, COACH_LEVEL_4, CONTENT_OPERATION, CONTENT_ADMIN);

    public static Role stranger(){
        Role stranger = new Role();
        stranger.setName("陌生人");
        stranger.setLevel(STRANGE);
        return stranger;
    }

    public static Role student(){
        Role stranger = new Role();
        stranger.setName("学生");
        stranger.setLevel(STUDENT);
        return stranger;
    }

    public static boolean isAsst(int roleLevel){
        return asstRoleLevel.contains(roleLevel);
    }
}
