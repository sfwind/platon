package com.iquanwai.platon.biz.domain.fragmentation.practice;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.iquanwai.platon.biz.dao.common.ProfileDao;
import com.iquanwai.platon.biz.dao.common.UserRoleDao;
import com.iquanwai.platon.biz.dao.fragmentation.*;
import com.iquanwai.platon.biz.domain.fragmentation.message.MessageService;
import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.po.*;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.po.common.Role;
import com.iquanwai.platon.biz.po.common.UserRole;
import com.iquanwai.platon.biz.util.DateUtils;
import com.iquanwai.platon.biz.util.ExecutorUtil;
import com.iquanwai.platon.biz.util.page.Page;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

/**
 * Created by justin on 17/2/8.
 */
@Service
public class PracticeDiscussServiceImpl implements PracticeDiscussService {
    @Autowired
    private WarmupPracticeDiscussDao warmupPracticeDiscussDao;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private KnowledgeDiscussDao knowledgeDiscussDao;
    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private ApplicationSubmitDao applicationSubmitDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private RiseClassMemberDao riseClassMemberDao;
    @Autowired
    private WarmupPracticeDao warmupPracticeDao;
    @Autowired
    private RiseMemberDao riseMemberDao;
    @Autowired
    private UserRoleDao userRoleDao;


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void discuss(Integer profileId, Integer warmupPracticeId, String comment, Integer repliedId) {
        WarmupPracticeDiscuss warmupPracticeDiscuss = new WarmupPracticeDiscuss();
        warmupPracticeDiscuss.setWarmupPracticeId(warmupPracticeId);
        warmupPracticeDiscuss.setComment(comment);
        warmupPracticeDiscuss.setDel(0);
        warmupPracticeDiscuss.setProfileId(profileId);
        if (repliedId != null) {
            // 回复讨论
            WarmupPracticeDiscuss repliedDiscuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, repliedId);
            if (repliedDiscuss != null) {
                warmupPracticeDiscuss.setRepliedId(repliedId);
                warmupPracticeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                warmupPracticeDiscuss.setRepliedProfileId(repliedDiscuss.getProfileId());
                warmupPracticeDiscuss.setRepliedDel(0);
                warmupPracticeDiscuss.setOriginDiscussId(repliedDiscuss.getOriginDiscussId());
                operationLogService.trace(profileId, "replyWarumupDiscuss", () -> {
                    OperationLogService.Prop prop = OperationLogService.props();
                    RiseMember riseMember = riseMemberDao.loadValidRiseMember(repliedDiscuss.getProfileId());
                    WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, warmupPracticeId);
                    RiseClassMember riseClassMember = riseClassMemberDao.loadLatestRiseClassMember(repliedDiscuss.getProfileId());
                    if (riseClassMember != null) {
                        if (riseClassMember.getClassName() != null) {
                            prop.add("repliedClassName", riseClassMember.getClassName());
                        }
                        if (riseClassMember.getGroupId() != null) {
                            prop.add("repliedGroupId", riseClassMember.getGroupId());
                        }
                    }
                    prop.add("repliedRolename", riseMember == null ? 0 : riseMember.getMemberTypeId());
                    prop.add("warmupId", warmupPracticeId);
                    prop.add("problemId", warmupPractice.getProblemId());
                    Profile profile = accountService.getProfile(repliedDiscuss.getProfileId());
                    prop.add("repliedRiseId", profile.getRiseId());
                    return prop;
                });
            }
        } else {
            // 提交讨论
            operationLogService.trace(profileId, "discussWarmup", () -> {
                OperationLogService.Prop prop = OperationLogService.props();
                WarmupPractice warmupPractice = warmupPracticeDao.load(WarmupPractice.class, warmupPracticeId);
                prop.add("problemId", warmupPractice.getProblemId());
                prop.add("warmupId", warmupPracticeId);
                return prop;
            });
        }
        warmupPracticeDiscuss.setPriority(0);
        Integer id = warmupPracticeDiscussDao.insert(warmupPracticeDiscuss);
        if (warmupPracticeDiscuss.getOriginDiscussId() == null) {
            //如果没有回复其它评论,则originDiscussId=自身
            warmupPracticeDiscussDao.updateOriginDiscussId(id, id);
        }

        //发送回复通知
        if (repliedId != null && !profileId.equals(warmupPracticeDiscuss.getRepliedProfileId())) {
            String url = "/rise/static/message/warmup/reply?commentId={0}&warmupPracticeId={1}";
            url = MessageFormat.format(url, Objects.toString(id), Objects.toString(warmupPracticeId));
            String message = "回复了我的巩固练习问题";
            messageService.sendMessage(message, Objects.toString(warmupPracticeDiscuss.getRepliedProfileId()),
                    Objects.toString(profileId), url);
        }
    }

    @Override
    public void discussKnowledge(Integer profileId, Integer knowledgeId, String comment, Integer repliedId) {
        KnowledgeDiscuss knowledgeDiscuss = new KnowledgeDiscuss();
        knowledgeDiscuss.setKnowledgeId(knowledgeId);
        knowledgeDiscuss.setComment(comment);
        knowledgeDiscuss.setDel(0);
        knowledgeDiscuss.setProfileId(profileId);
        if (repliedId != null) {
            KnowledgeDiscuss repliedDiscuss = knowledgeDiscussDao.load(KnowledgeDiscuss.class, repliedId);
            if (repliedDiscuss != null) {
                knowledgeDiscuss.setRepliedId(repliedId);
                knowledgeDiscuss.setRepliedComment(repliedDiscuss.getComment());
                knowledgeDiscuss.setRepliedProfileId(repliedDiscuss.getProfileId());
            }
        }
        knowledgeDiscuss.setPriority(0);
        Integer id = knowledgeDiscussDao.insert(knowledgeDiscuss);

        //发送回复通知
        if (repliedId != null && !profileId.equals(knowledgeDiscuss.getRepliedProfileId())) {
            String url = "/rise/static/message/knowledge/reply?commentId={0}&knowledgeId={1}";
            url = MessageFormat.format(url, Objects.toString(id), Objects.toString(knowledgeId));
            String message = "回复了我的知识理解问题";
            messageService.sendMessage(message, Objects.toString(knowledgeDiscuss.getRepliedProfileId()),
                    Objects.toString(profileId), url);
        }
    }

    @Override
    public List<WarmupComment> loadDiscuss(Integer profileId, Integer warmupPracticeId, Page page) {
        List<WarmupPracticeDiscuss> discussList = warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page);
        fulfilDiscuss(discussList);

        return buildWarmupComment(profileId, discussList);
    }

    private List<WarmupComment> buildWarmupComment(Integer profileId, List<WarmupPracticeDiscuss> discussList) {
        List<WarmupComment> warmupComments = Lists.newArrayList();

        Map<Integer, WarmupPracticeDiscuss> discussMap = Maps.newHashMap();
        discussList.forEach(warmupPracticeDiscuss ->
                discussMap.put(warmupPracticeDiscuss.getId(), warmupPracticeDiscuss));

        // 过滤所有起始评论已删除的评论
        discussList = discussList.stream().filter(warmupPracticeDiscuss ->
                discussMap.get(warmupPracticeDiscuss.getId()) != null)
                .collect(Collectors.toList());

        //找到所有讨论的第一条
        discussList.forEach(discuss -> {
            // 设置isMine字段
            if (discuss.getProfileId().equals(profileId)) {
                discuss.setIsMine(true);
            } else {
                discuss.setIsMine(false);
            }
            // 清空用户id
            discuss.setProfileId(null);
            discuss.setRepliedProfileId(null);
            //讨论的第一条
            if (discuss.getOriginDiscussId() != null && discuss.getOriginDiscussId() == discuss.getId()) {
                ModelMapper modelMapper = new ModelMapper();
                WarmupComment warmupComment = modelMapper.map(discuss, WarmupComment.class);
                // 设置isPriority字段
                if (warmupComment.getPriority() != null && warmupComment.getPriority() == 1) {
                    warmupComment.setPriorityComment(1);
                } else {
                    warmupComment.setPriorityComment(0);
                }
                warmupComments.add(warmupComment);
            }
        });

        discussList.forEach(discuss -> {
            //不是讨论的第一条
            if (discuss.getOriginDiscussId() != null && discuss.getOriginDiscussId() != discuss.getId()) {
                warmupComments.forEach(warmupComment -> {
                    if (warmupComment.getId() == discuss.getOriginDiscussId()) {
                        warmupComment.getWarmupPracticeDiscussList().add(discuss);
                        // 设置isPriority字段
                        if (discuss.getPriority() == 1) {
                            warmupComment.setPriorityComment(1);
                        }
                    }
                });
            }
        });

        // 第一条评论排序 优质答案排序优先,非优质答案按时间倒序
        warmupComments.sort((o1, o2) -> {
            if (!o1.getPriorityComment().equals(o2.getPriorityComment())) {
                return o2.getPriorityComment() - o1.getPriorityComment();
            }
            return o2.getAddTime().compareTo(o1.getAddTime());
        });

        // 相关讨论的排序 按时间顺序
        warmupComments.forEach(warmupComment -> {
            List<WarmupPracticeDiscuss> warmupDiscussList = warmupComment.getWarmupPracticeDiscussList();
            warmupDiscussList.sort((o1, o2) -> o1.getAddTime().compareTo(o2.getAddTime()));
        });

        return warmupComments;
    }

    @Override
    public List<KnowledgeDiscuss> loadKnowledgeDiscusses(Integer knowledgeId, Page page) {
        List<KnowledgeDiscuss> discussesList = knowledgeDiscussDao.loadDiscuss(knowledgeId, page);
        fulfilDiscuss(discussesList);
        return discussesList;
    }

    @Override
    public Integer deleteKnowledgeDiscussById(Integer discussId) {
        //标记回复该评论的评论
        knowledgeDiscussDao.markRepliedCommentDelete(discussId);
        // 删除KnowledgeDiscuss记录，将del字段置为1
        return knowledgeDiscussDao.updateDelById(1, discussId);
    }

    @Override
    public Map<Integer, List<WarmupComment>> loadDiscuss(Integer profileId, List<Integer> warmupPracticeIds, Page page) {
        Map<Integer, List<WarmupComment>> result = Maps.newHashMap();

        //并发获取评论提高效率
        warmupPracticeIds.forEach(warmupPracticeId -> {
            FutureTask futureTask = new FutureTask(() -> warmupPracticeDiscussDao.loadDiscuss(warmupPracticeId, page));
            futureTask.run();
            try {
                List<WarmupPracticeDiscuss> discuss = (List<WarmupPracticeDiscuss>) futureTask.get();
                fulfilDiscuss(discuss);
                List<WarmupComment> warmupComments = buildWarmupComment(profileId, discuss);
                result.put(warmupPracticeId, warmupComments);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });

        return result;
    }

    @Override
    public WarmupPracticeDiscuss loadDiscuss(Integer discussId) {
        WarmupPracticeDiscuss discuss = warmupPracticeDiscussDao.load(WarmupPracticeDiscuss.class, discussId);
        if (discuss != null) {
            fulfilDiscuss(discuss);
            discuss.setReferenceId(discuss.getWarmupPracticeId());
        }
        return discuss;
    }

    @Override
    public void deleteComment(Integer discussId) {
        //删除评论
        warmupPracticeDiscussDao.deleteComment(discussId);
        //标记回复该评论的评论
        warmupPracticeDiscussDao.markRepliedCommentDelete(discussId);
    }

    @Override
    public KnowledgeDiscuss loadKnowledgeDiscuss(Integer discussId) {
        KnowledgeDiscuss discuss = knowledgeDiscussDao.load(KnowledgeDiscuss.class, discussId);
        if (discuss != null) {
            fulfilDiscuss(discuss);
            discuss.setReferenceId(discuss.getKnowledgeId());
        }
        return discuss;
    }

    @Override
    public List<PersonalDiscuss> loadPersonalKnowledgeDiscussList(Integer profileId, Integer knowledgeId) {
        List<PersonalDiscuss> personalElements = Lists.newArrayList();

        List<KnowledgeDiscuss> personalDiscusses = knowledgeDiscussDao.loadDiscussesByProfileId(profileId, knowledgeId);
        List<Integer> personalIds = personalDiscusses.stream().map(KnowledgeDiscuss::getId).collect(Collectors.toList());

        // 根据自己的评论，查看所有评论自己的内容
        List<KnowledgeDiscuss> commentDiscuss = knowledgeDiscussDao.loadKnowledgeDiscussByRepliedIds(personalIds);
        Map<Integer, List<KnowledgeDiscuss>> commentDiscussMap = commentDiscuss.stream().collect(Collectors.groupingBy(KnowledgeDiscuss::getRepliedId));

        // 获取所有涉及到的人员 ProfileId, 以及 Profile 对应 map 集合
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.addAll(personalDiscusses.stream().map(KnowledgeDiscuss::getProfileId).collect(Collectors.toList()));
        profileIds.addAll(commentDiscuss.stream().map(KnowledgeDiscuss::getProfileId).collect(Collectors.toList()));
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key1));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        personalDiscusses.forEach(discuss -> {
            PersonalDiscuss personalDiscuss = new PersonalDiscuss();
            personalDiscuss.setDiscuss(convertDiscussToElement(discuss, involvedProfileMap, asstProfileIds));
            List<KnowledgeDiscuss> knowledgeComments = commentDiscussMap.getOrDefault(discuss.getId(), Lists.newArrayList());
            List<DiscussElement> discussElementsComments = knowledgeComments.stream()
                    .map(comment -> convertDiscussToElement(comment, involvedProfileMap, asstProfileIds))
                    .filter(Objects::nonNull).collect(Collectors.toList());
            personalDiscuss.setComments(discussElementsComments);
            personalElements.add(personalDiscuss);
        });
        return personalElements;
    }

    @Override
    public List<DiscussElementsPair> loadPriorityKnowledgeDiscuss(Integer knowledgeId) {
        List<DiscussElementsPair> discussElementsPairs = Lists.newArrayList();

        // 精彩评论
        List<KnowledgeDiscuss> priorityDiscuss = knowledgeDiscussDao.loadPriorityKnowledgeDiscuss(knowledgeId);

        // 被回复的评论
        List<Integer> repliedIds = priorityDiscuss.stream()
                .filter(priority -> priority.getRepliedId() != null && priority.getDel() == 0)
                .map(KnowledgeDiscuss::getRepliedId)
                .collect(Collectors.toList());
        List<KnowledgeDiscuss> repliedDiscuss = knowledgeDiscussDao.loadKnowledgeDiscussByIds(repliedIds);
        Map<Integer, KnowledgeDiscuss> repliedDiscussMap = repliedDiscuss.stream()
                .collect(Collectors.toMap(KnowledgeDiscuss::getId, knowledgeDiscuss -> knowledgeDiscuss, (key1, key2) -> key1));


        // 获取所有涉及到的人员 ProfileId, 以及 Profile 对应 map 集合
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.addAll(priorityDiscuss.stream().map(KnowledgeDiscuss::getProfileId).collect(Collectors.toList()));
        profileIds.addAll(repliedDiscuss.stream().map(KnowledgeDiscuss::getProfileId).collect(Collectors.toList()));
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key1));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        priorityDiscuss.forEach(discuss -> {
            DiscussElementsPair discussElementsPair = new DiscussElementsPair();
            discussElementsPair.setPriorityDiscuss(convertDiscussToElement(discuss, involvedProfileMap, asstProfileIds));
            if (discuss.getRepliedId() != null && discuss.getRepliedDel() == 0) {
                discussElementsPair.setOriginDiscuss(convertDiscussToElement(repliedDiscussMap.get(discuss.getRepliedId()), involvedProfileMap, asstProfileIds));
            }
            discussElementsPairs.add(discussElementsPair);
        });

        return discussElementsPairs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, WarmupDiscussDistrict> loadWarmUpDiscuss(Integer profileId, List<Integer> warmUpPracticeIds) {
        Map<Integer, WarmupDiscussDistrict> discussListMap = Maps.newHashMap();

        Map<Integer, FutureTask<WarmupDiscussDistrict>> futureTaskMap = Maps.newHashMap();
        warmUpPracticeIds.forEach(warmUpPracticeId -> {
            FutureTask futureTask = new FutureTask(() -> {
                WarmupDiscussDistrict warmupDiscussDistrict = new WarmupDiscussDistrict();

                warmupDiscussDistrict.setPersonal(loadPersonalWarmUpDiscuss(profileId, warmUpPracticeId));
                warmupDiscussDistrict.setPriorities(loadPriorityWarmUpDiscuss(warmUpPracticeId));
                return warmupDiscussDistrict;
            });
            ExecutorUtil.submit(futureTask);
            futureTaskMap.put(warmUpPracticeId, futureTask);
        });

        for (Integer key : futureTaskMap.keySet()) {
            try {
                WarmupDiscussDistrict warmupDiscussDistrict = futureTaskMap.get(key).get();
                if (warmupDiscussDistrict != null) {
                    discussListMap.put(key, warmupDiscussDistrict);
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        return discussListMap;
    }

    @Override
    public WarmupDiscussDistrict loadSingleWarmUpDiscuss(Integer profileId, Integer warmUpPracticeId) {
        WarmupDiscussDistrict warmupDiscussDistrict = new WarmupDiscussDistrict();
        warmupDiscussDistrict.setPersonal(loadPersonalWarmUpDiscuss(profileId, warmUpPracticeId));
        warmupDiscussDistrict.setPriorities(loadPriorityWarmUpDiscuss(warmUpPracticeId));
        return warmupDiscussDistrict;
    }


    private List<PersonalDiscuss> loadPersonalWarmUpDiscuss(Integer profileId, Integer warmUpPracticeId) {
        List<PersonalDiscuss> personalElements = Lists.newArrayList();

        List<WarmupPracticeDiscuss> personalDiscusses = warmupPracticeDiscussDao.loadDiscussByProfileId(profileId, warmUpPracticeId);
        List<Integer> personalIds = personalDiscusses.stream().map(WarmupPracticeDiscuss::getId).collect(Collectors.toList());

        // 根据自己的评论，查看所有评论自己的内容
        List<WarmupPracticeDiscuss> commentDiscuss = warmupPracticeDiscussDao.loadDiscussByRepliedIds(personalIds);
        Map<Integer, List<WarmupPracticeDiscuss>> commentDiscussMap = commentDiscuss.stream().collect(Collectors.groupingBy(WarmupPracticeDiscuss::getRepliedId));

        // 获取所有涉及到的人员 ProfileId, 以及 Profile 对应 map 集合
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.addAll(personalDiscusses.stream().map(WarmupPracticeDiscuss::getProfileId).collect(Collectors.toList()));
        profileIds.addAll(commentDiscuss.stream().map(WarmupPracticeDiscuss::getProfileId).collect(Collectors.toList()));
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key1));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        personalDiscusses.forEach(discuss -> {
            PersonalDiscuss personalDiscuss = new PersonalDiscuss();
            personalDiscuss.setDiscuss(convertDiscussToElement(discuss, involvedProfileMap, asstProfileIds));
            List<WarmupPracticeDiscuss> warmupPracticeDiscusses = commentDiscussMap.getOrDefault(discuss.getId(), Lists.newArrayList());
            List<DiscussElement> discussElementsComments = warmupPracticeDiscusses.stream()
                    .map(comment -> convertDiscussToElement(comment, involvedProfileMap, asstProfileIds))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            personalDiscuss.setComments(discussElementsComments);
            personalElements.add(personalDiscuss);
        });
        return personalElements;
    }

    private List<DiscussElementsPair> loadPriorityWarmUpDiscuss(Integer warmUpPracticeId) {
        List<DiscussElementsPair> discussElementsPairs = Lists.newArrayList();

        // 获取全部的精彩评论
        List<WarmupPracticeDiscuss> priorityDiscuss = warmupPracticeDiscussDao.loadPriorityWarmUpDiscuss(warmUpPracticeId);

        // 被回复的评论
        List<Integer> repliedIds = priorityDiscuss.stream()
                .filter(priority -> priority.getRepliedId() != null && priority.getDel() == 0)
                .map(WarmupPracticeDiscuss::getRepliedId)
                .collect(Collectors.toList());
        List<WarmupPracticeDiscuss> repliedDiscuss = warmupPracticeDiscussDao.loadWarmupDiscussById(repliedIds);
        Map<Integer, WarmupPracticeDiscuss> repliedDiscussMap = repliedDiscuss.stream()
                .collect(Collectors.toMap(WarmupPracticeDiscuss::getId, warmupPracticeDiscuss -> warmupPracticeDiscuss, (key1, key2) -> key1));

        // 获取所有涉及到的人员 ProfileId, 以及 Profile 对应 map 集合
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.addAll(priorityDiscuss.stream().map(WarmupPracticeDiscuss::getProfileId).collect(Collectors.toList()));
        profileIds.addAll(repliedDiscuss.stream().map(WarmupPracticeDiscuss::getProfileId).collect(Collectors.toList()));
        List<Profile> profiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = profiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile, (key1, key2) -> key1));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        priorityDiscuss.forEach(discuss -> {
            DiscussElementsPair discussElementsPair = new DiscussElementsPair();
            discussElementsPair.setPriorityDiscuss(convertDiscussToElement(discuss, involvedProfileMap, asstProfileIds));
            if (discuss.getRepliedId() != null && discuss.getRepliedDel() == 0) {
                discussElementsPair.setOriginDiscuss(convertDiscussToElement(repliedDiscussMap.get(discuss.getRepliedId()), involvedProfileMap, asstProfileIds));
            }
            discussElementsPairs.add(discussElementsPair);
        });
        return discussElementsPairs;
    }

    @Override
    public List<PersonalDiscuss> loadPersonalApplicationSubmitDiscussList(Integer profileId, Integer applicationId, Integer planId) {
        // 维护数据结构的一致性（方便前端组件统一）
        List<PersonalDiscuss> personalElements = Lists.newArrayList();
        PersonalDiscuss personalDiscuss = new PersonalDiscuss();
        // 获取个人作业以及相关评论
        ApplicationSubmit applicationSubmit = applicationSubmitDao.load(applicationId, planId, profileId);
        if (applicationSubmit == null) {
            return Lists.newArrayList();
        }

        List<Comment> comments = commentDao.loadApplicationComments(applicationSubmit.getId());
        // 获取作业以及评论涉及的人员
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.add(applicationSubmit.getProfileId());
        profileIds.addAll(comments.stream().map(Comment::getCommentProfileId).collect(Collectors.toList()));
        List<Profile> involvedProfiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = involvedProfiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        // 转换用户作业数据
        DiscussElement discussElement = new DiscussElement();
        discussElement.setContent(applicationSubmit.getContent());
        Profile submitProfile = involvedProfileMap.getOrDefault(applicationSubmit.getProfileId(), null);
        if (submitProfile != null) {
            discussElement.setNickname(submitProfile.getNickname());
            discussElement.setAvatar(submitProfile.getHeadimgurl());
            discussElement.setContent(applicationSubmit.getContent());
            discussElement.setPublishTime(applicationSubmit.getPublishTime());
            discussElement.setIsAsst(asstProfileIds.contains(submitProfile.getId()));
            personalDiscuss.setDiscuss(discussElement);
        }

        // 专用评论作业
        List<DiscussElement> discussElements = Lists.newArrayList();
        comments.forEach(comment -> {
            DiscussElement element = new DiscussElement();
            Profile singleProfile = involvedProfileMap.getOrDefault(comment.getCommentProfileId(), null);
            if (singleProfile != null) {
                element.setNickname(singleProfile.getNickname());
                element.setAvatar(singleProfile.getHeadimgurl());
                element.setContent(comment.getContent());
                element.setPublishTime(comment.getAddTime());
                element.setIsAsst(asstProfileIds.contains(singleProfile.getId()));
                discussElements.add(element);
            }
        });

        personalDiscuss.setComments(discussElements);

        personalElements.add(personalDiscuss);
        return personalElements;
    }

    // 获取精华区的作业和讨论
    @Override
    public List<DiscussElementsPair> loadPriorityApplicationSubmitDiscussList(Integer applicationId) {
        List<DiscussElementsPair> discussElementsPairs = Lists.newArrayList();

        List<ApplicationSubmit> applicationSubmits = applicationSubmitDao.loadPriorityApplicationSubmitsByApplicationId(applicationId);
        List<Integer> submitsIds = applicationSubmits.stream().map(ApplicationSubmit::getId).collect(Collectors.toList());

        List<Comment> comments = commentDao.loadAllCommentsByReferenceIds(submitsIds);
        Map<Integer, List<Comment>> commentsMap = comments.stream().collect(Collectors.groupingBy(Comment::getReferencedId));

        // 获取所有涉及的人员数据
        List<Integer> profileIds = Lists.newArrayList();
        profileIds.addAll(applicationSubmits.stream().map(ApplicationSubmit::getProfileId).collect(Collectors.toList()));
        profileIds.addAll(comments.stream().map(Comment::getCommentProfileId).collect(Collectors.toList()));
        List<Profile> involvedProfiles = profileDao.queryAccounts(profileIds);
        Map<Integer, Profile> involvedProfileMap = involvedProfiles.stream().collect(Collectors.toMap(Profile::getId, profile -> profile));

        // 所有助教人员的 ProfileId 集合
        List<Integer> asstProfileIds = userRoleDao.loadUserRoleByRoleIds(Role.loadAsstRoleIds()).stream().map(UserRole::getProfileId).collect(Collectors.toList());

        // 拼装数据
        applicationSubmits.forEach(applicationSubmit -> {
            DiscussElementsPair discussElementsPair = new DiscussElementsPair();

            Integer referenceId = applicationSubmit.getId();
            List<Comment> referenceComments = commentsMap.getOrDefault(referenceId, Lists.newArrayList());

            Profile priorityProfile = involvedProfileMap.getOrDefault(applicationSubmit.getProfileId(), null);
            if (priorityProfile != null) {
                DiscussElement priorityDiscuss = new DiscussElement();
                priorityDiscuss.setContent(applicationSubmit.getContent());
                priorityDiscuss.setPublishTime(applicationSubmit.getPublishTime());
                priorityDiscuss.setAvatar(priorityProfile.getHeadimgurl());
                priorityDiscuss.setNickname(priorityProfile.getNickname());
                priorityDiscuss.setIsAsst(asstProfileIds.contains(priorityProfile.getId()));
                discussElementsPair.setPriorityDiscuss(priorityDiscuss);
            }

            List<DiscussElement> discussComments = Lists.newArrayList();
            referenceComments.forEach(comment -> {
                Profile discussProfile = involvedProfileMap.getOrDefault(comment.getCommentProfileId(), null);
                if (discussProfile != null) {
                    DiscussElement discussComment = new DiscussElement();
                    discussComment.setContent(comment.getContent());
                    discussComment.setPublishTime(comment.getAddTime());
                    discussComment.setAvatar(discussProfile.getHeadimgurl());
                    discussComment.setNickname(discussProfile.getNickname());
                    discussComment.setIsAsst(asstProfileIds.contains(discussProfile.getId()));
                    discussComments.add(discussComment);
                }
            });

            discussElementsPair.setMultiComments(discussComments);
            discussElementsPairs.add(discussElementsPair);
        });

        return discussElementsPairs;
    }

    // 将知识点评论转化为评论对象
    private <T extends AbstractComment> DiscussElement convertDiscussToElement(T abstractComment, Map<Integer, Profile> involvedProfileMap, List<Integer> asstProfileIds) {
        if (abstractComment.getProfileId() == null || involvedProfileMap.get(abstractComment.getProfileId()) == null) {
            // 失效数据，返回 null
            return null;
        } else {
            DiscussElement discussElement = new DiscussElement();
            Profile profile = involvedProfileMap.get(abstractComment.getProfileId());
            discussElement.setAvatar(profile.getHeadimgurl());
            discussElement.setNickname(profile.getNickname());
            discussElement.setPublishTime(abstractComment.getAddTime());
            discussElement.setContent(abstractComment.getComment());
            discussElement.setIsAsst(asstProfileIds.contains(abstractComment.getProfileId()));
            return discussElement;
        }
    }

    //填充评论的其他字段
    private void fulfilDiscuss(List<? extends AbstractComment> discuss) {
        List<Integer> profileIds = Lists.newArrayList();
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            if (!profileIds.contains(warmupPracticeDiscuss.getProfileId())) {
                profileIds.add(warmupPracticeDiscuss.getProfileId());
            }
            if (warmupPracticeDiscuss.getRepliedProfileId() != null) {
                if (!profileIds.contains(warmupPracticeDiscuss.getRepliedProfileId())) {
                    profileIds.add(warmupPracticeDiscuss.getRepliedProfileId());
                }
            }
        });
        //批量获取用户信息
        List<Profile> accounts = accountService.getProfiles(profileIds);
        //设置名称、头像和时间
        discuss.stream().forEach(warmupPracticeDiscuss -> {
            accounts.stream().forEach(account -> {
                if (warmupPracticeDiscuss.getProfileId() != null &&
                        account.getId() == warmupPracticeDiscuss.getProfileId()) {
                    warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
                    warmupPracticeDiscuss.setName(account.getNickname());
                    warmupPracticeDiscuss.setRole(account.getRole());
                    warmupPracticeDiscuss.setSignature(account.getSignature());
                }
                if (warmupPracticeDiscuss.getRepliedProfileId() != null &&
                        account.getId() == warmupPracticeDiscuss.getRepliedProfileId()) {
                    warmupPracticeDiscuss.setRepliedName(account.getNickname());
                }
            });
            warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
        });
    }

    private void fulfilDiscuss(AbstractComment warmupPracticeDiscuss) {
        Profile account = accountService.getProfile(warmupPracticeDiscuss.getProfileId());
        //设置名称、头像和时间
        if (warmupPracticeDiscuss.getProfileId() != null &&
                account.getId() == warmupPracticeDiscuss.getProfileId()) {
            warmupPracticeDiscuss.setAvatar(account.getHeadimgurl());
            warmupPracticeDiscuss.setName(account.getNickname());
            warmupPracticeDiscuss.setRole(account.getRole());
            warmupPracticeDiscuss.setSignature(account.getSignature());
        }
        if (warmupPracticeDiscuss.getRepliedProfileId() != null &&
                account.getId() == warmupPracticeDiscuss.getRepliedProfileId()) {
            warmupPracticeDiscuss.setRepliedName(account.getNickname());
        }

        warmupPracticeDiscuss.setDiscussTime(DateUtils.parseDateToString(warmupPracticeDiscuss.getAddTime()));
    }

}
