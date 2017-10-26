package com.iquanwai.platon.biz.dao.common;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.po.common.Picture;
import com.iquanwai.platon.biz.util.ThreadPool;
import org.apache.commons.dbutils.AsyncQueryRunner;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by nethunder on 2016/12/15.
 */
@Repository
public class PictureDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<Picture> picture(Integer moduleId, Integer referId){
        QueryRunner run = new QueryRunner(getDataSource());
        ResultSetHandler<List<Picture>> h = new BeanListHandler(Picture.class);
        try{
            List<Picture> pictureList = run.query("select * from Picture where ModuleId=? and ReferencedId=?", h, moduleId, referId);
            return pictureList;
        } catch (SQLException e){
            logger.error(e.getLocalizedMessage(), e);
        }
        return Lists.newArrayList();
    }

    public int upload(Picture picture){
        QueryRunner run = new QueryRunner(getDataSource());
        AsyncQueryRunner asyncRun = new AsyncQueryRunner(ThreadPool.createSingleThreadExecutor(), run);
        String insertSql = "INSERT INTO Picture(ModuleId, ReferencedId , RemoteIp,  RealName, Length, Type, Thumbnail) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try{
            Future<Integer> result = asyncRun.update(insertSql,
                    picture.getModuleId(),picture.getReferencedId(),picture.getRemoteIp(),picture.getRealName(),picture.getLength(),picture.getType(),picture.getThumbnail());
            return result.get();
        } catch (SQLException e){
            logger.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e){
            // ignore
        } catch (ExecutionException e){
            logger.error(e.getMessage(), e);
        }
        return -1;
    }
}
