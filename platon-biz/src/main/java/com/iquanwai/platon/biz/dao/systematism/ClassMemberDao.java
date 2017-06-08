package com.iquanwai.platon.biz.dao.systematism;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.systematism.ClassMember;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by nethunder on 2017/6/7.
 */
@Repository
public class ClassMemberDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public ClassMember classMember(String openid, Integer courseId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<ClassMember> h = new BeanHandler<>(ClassMember.class);
        try {
            ClassMember classMember = run.query("SELECT * FROM ClassMember where Openid=? and CourseId=? and Graduate=0",
                    h, openid, courseId);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return null;
    }

    public List<ClassMember> classMember(String openid){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<ClassMember>> h = new BeanListHandler<>(ClassMember.class);

        try {
            List<ClassMember> classMember = run.query("SELECT * FROM ClassMember where Openid=? and Graduate = 0", h, openid);
            return classMember;
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }

        return Lists.newArrayList();
    }

    public void graduate(Integer id){
        QueryRunner run = new QueryRunner(getDataSource());

        try {
            run.update("UPDATE ClassMember SET Graduate =1 " +
                    "where id=?", id);

        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}
