package com.iquanwai.platon.biz.domain.log;

import com.iquanwai.platon.biz.dao.common.ActionLogDao;
import com.iquanwai.platon.biz.dao.common.OperationLogDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseClassMemberDao;
import com.iquanwai.platon.biz.dao.fragmentation.RiseMemberDao;
import com.iquanwai.platon.biz.po.RiseClassMember;
import com.iquanwai.platon.biz.po.RiseMember;
import com.iquanwai.platon.biz.po.common.ActionLog;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.UserRole;
import com.iquanwai.platon.biz.util.ConfigUtils;
import com.iquanwai.platon.biz.util.ThreadPool;
import com.sensorsdata.analytics.javasdk.SensorsAnalytics;
import com.sensorsdata.analytics.javasdk.exceptions.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.support.Assert;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by justin on 16/9/3.
 */
@Service
public class OperationLogServiceImpl implements OperationLogService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private ActionLogDao actionLogDao;
    @Autowired
    private SensorsAnalytics sa;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public void log(OperationLog operationLog) {
        if (ConfigUtils.logSwitch()) {
            if (operationLog.getMemo() != null && operationLog.getMemo().length() > 1024) {
                operationLog.setMemo(operationLog.getMemo().substring(0, 1024));
            }
            operationLogDao.insert(operationLog);
        }
    }

    @Override
    public void log(ActionLog actionLog) {
        if (ConfigUtils.logSwitch()) {
            if (actionLog.getMemo() != null && actionLog.getMemo().length() > 1024) {
                actionLog.setMemo(actionLog.getMemo().substring(0, 1024));
            }
            actionLogDao.insert(actionLog);
        }
    }

    @Override
    public void trace(Supplier<Integer> profileIdSupplier, String eventName, Supplier<Prop> supplier) {
        ThreadPool.execute(() -> {
            try {
                Integer profileId = profileIdSupplier.get();
                Prop prop = supplier.get();
                Map<String, Object> properties = prop.build();
                Assert.notNull(profileId, "用户id不能为null");
                Profile profile = profileDao.load(Profile.class, profileId);
                UserRole role = userRoleDao.getAssist(profileId);

                Integer roleName = 0;
                // TODO: 子康
                RiseMember validRiseMember = riseMemberDao.loadValidRiseMember(profileId);

                RiseClassMember riseClassMember = riseClassMemberDao.loadActiveRiseClassMember(profileId);
                if (riseClassMember == null) {
                    riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(profileId);
                }

                if (riseClassMember != null) {
                    if (riseClassMember.getClassName() != null) {
                        properties.put("className", riseClassMember.getClassName());
                    }
                    if (riseClassMember.getGroupId() != null) {
                        properties.put("groupId", riseClassMember.getGroupId());
                    }
                }
                if (validRiseMember != null) {
                    roleName = validRiseMember.getMemberTypeId();
                }
                properties.put("roleName", roleName);
                properties.put("isAsst", role != null);
                properties.put("riseId", profile.getRiseId());
                logger.info("trace:\nprofielId:{}\neventName:{}\nprops:{}", profileId, eventName, properties);
                sa.track(profile.getRiseId(), true, eventName, properties);

                // 上线前删掉
//                sa.flush();
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }

    @Override
    public void trace(Integer profileId, String eventName) {
        this.trace(() -> profileId, eventName, OperationLogService::props);
    }

    @Override
    public void trace(Integer profileId, String eventName, Supplier<Prop> supplier) {
        this.trace(() -> profileId, eventName, supplier);
    }

    @Override
    public void profileSet(Integer profileId, String key, Object value) {
        profileSet(() -> profileId, key, value);
    }

    @Override
    public void profileSet(Supplier<Integer> supplier, String key, Object value) {
        ThreadPool.execute(() -> {
            Integer profileId = supplier.get();
            Profile profile = profileDao.load(Profile.class, profileId);
            try {
                sa.profileSet(profile.getRiseId(), true, key, value);
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }
}
