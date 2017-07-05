package com.iquanwai.platon.biz.dao.fragmentation;

import com.iquanwai.platon.biz.dao.PracticeDBUtil;
import com.iquanwai.platon.biz.po.ProblemExtension;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xfduan on 2017/7/5.
 */
public class ProblemExtensionDao extends PracticeDBUtil {

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * insert 字段：catalog subCatalog problem problemId extension
     * @param problemExtension
     * @return
     */
    public Integer insert(ProblemExtension problemExtension) {
        QueryRunner runner = new QueryRunner(getDataSource());
        String sql = "insert into ProblemExtention () values ()";
        try {
            Long result = runner.insert(sql, new ScalarHandler<>(), problemExtension.get)
        }
    }

}
