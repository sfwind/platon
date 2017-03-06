package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.DBUtil;
import com.iquanwai.platon.biz.util.Constants;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Created by nethunder on 2017/2/27.
 */
@Component
public class FragmentAnalysisDataDao extends DBUtil {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public void insertArticleViewInfo(Integer module,Integer articleId){
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "INSERT INTO ArticleViewInfo(ArticleModule,ArticleId,ViewEventType) VALUES(?,?,?) ";
        try {
            Object[][] param = new Object[4][];
            for (int i = 0; i < param.length; i++) {
                param[i] = new Object[3];
                param[i][0] = module;
                param[i][1] = articleId;
            }
            param[0][2] = Constants.ViewInfo.EventType.PC_SUBMIT;
            param[1][2] = Constants.ViewInfo.EventType.MOBILE_SUBMIT;
            param[2][2] = Constants.ViewInfo.EventType.PC_SHOW;
            param[3][2] = Constants.ViewInfo.EventType.MOBILE_SHOW;
            runner.batch(sql, param);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    public int riseArticleViewCount(Integer articleModule, Integer articleId, Integer viewEventType) {
        QueryRunner runner = new QueryRunner((getDataSource()));
        String sql = "UPDATE ArticleViewInfo SET Count = Count + 1 where ArticleId = ? and ArticleModule = ? and ViewEventType = ?";
        try{
            return runner.update(sql, articleId, articleModule,viewEventType);
        } catch (SQLException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return -1;
    }
}
