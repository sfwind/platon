package com.iquanwai.platon.biz.domain.log;

import com.google.common.collect.Lists;
import com.iquanwai.platon.biz.dao.common.ActionLogDao;
import com.iquanwai.platon.biz.dao.common.ClassMemberDao;
import com.iquanwai.platon.biz.dao.common.OperationLogDao;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.domain.common.member.RiseMemberTypeRepo;
import com.iquanwai.platon.biz.domain.fragmentation.manager.RiseMemberManager;
import com.iquanwai.platon.biz.po.ClassMember;
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

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
    private ProfileDao profileDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RiseMemberManager riseMemberManager;
    @Autowired
    private ClassMemberDao classMemberDao;
    @Autowired
    private RiseMemberTypeRepo riseMemberTypeRepo;


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

                List<RiseMember> riseMemberList = riseMemberManager.member(profileId);
                if (!riseMemberList.isEmpty()) {
                    properties.put("roleNames", riseMemberList
                            .stream()
                            .map(RiseMember::getMemberTypeId)
                            .map(Object::toString)
                            .distinct()
                            .collect(Collectors.toList()));
                } else {
                    properties.put("roleNames", Lists.newArrayList("0"));
                }

                List<ClassMember> classMembers = classMemberDao.loadActiveByProfileId(profileId);
                if (classMembers.isEmpty()) {
                    ClassMember exist = classMemberDao.loadLatestByProfileId(profileId);
                    if (exist != null) {
                        classMembers = Lists.newArrayList(exist);
                    }
                }
                if (!classMembers.isEmpty()) {
                    classMembers.forEach(item -> {
                        if (item.getClassName() != null) {
                            properties.put(riseMemberManager.classNameKey(item.getMemberTypeId()), item.getClassName());
                        }
                        if (item.getGroupId() != null) {
                            properties.put(riseMemberManager.groupIdKey(item.getMemberTypeId()), item.getGroupId());
                        }
                    });
                }
                properties.put("isAsst", role != null);
                properties.put("riseId", profile.getRiseId());

                logger.info("trace:\nprofielId:{}\neventName:{}\nprops:{}", profileId, eventName, properties);
                sa.track(profile.getRiseId(), true, eventName, properties);

                // 上线前删掉
                if (ConfigUtils.isDevelopment()) {
                    sa.flush();
                }
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
                logger.info("trace:\nprofielId:{}\nkey:{}\nvalue:{}", profileId, key, value);
                sa.profileSet(profile.getRiseId(), true, key, value);
                if (ConfigUtils.isDevelopment()) {
                    sa.flush();
                }
            } catch (InvalidArgumentException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });
    }
}
