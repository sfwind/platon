package com.iquanwai.platon.biz.domain.log;

import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.po.common.OperationLog;

/**
 * Created by justin on 16/9/3.
 */
public interface OperationLogService {

    void log(OperationLog operationLog);

    void log(ActionLog actionLog);
}
