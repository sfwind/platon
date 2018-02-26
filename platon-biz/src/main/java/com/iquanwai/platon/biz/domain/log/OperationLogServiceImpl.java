package com.iquanwai.platon.biz.domain.log;

import com.iquanwai.platon.biz.dao.common.ActionLogDao;
import com.iquanwai.platon.biz.dao.common.OperationLogDao;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by justin on 16/9/3.
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private ActionLogDao actionLogDao;

    @Override
    public void log(OperationLog operationLog) {
        if(ConfigUtils.logSwitch()) {
            if (operationLog.getMemo() != null && operationLog.getMemo().length() > 1024) {
                operationLog.setMemo(operationLog.getMemo().substring(0, 1024));
            }
            operationLogDao.insert(operationLog);
        }
    }

    @Override
    public void log(ActionLog actionLog) {
        if(ConfigUtils.logSwitch()) {
            if (actionLog.getMemo() != null && actionLog.getMemo().length() > 1024) {
                actionLog.setMemo(actionLog.getMemo().substring(0, 1024));
            }
            actionLogDao.insert(actionLog);
        }
    }
}
