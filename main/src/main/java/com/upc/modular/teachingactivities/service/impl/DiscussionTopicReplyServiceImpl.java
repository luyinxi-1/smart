package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.mapper.SysUserMapper;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingactivities.entity.UserLikes;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicReplyMapper;
import com.upc.modular.teachingactivities.mapper.UserLikesMapper;
import com.upc.modular.teachingactivities.param.*;
import com.upc.modular.teachingactivities.service.IDiscussionTopicReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.mapper.TextbookMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Service
public class DiscussionTopicReplyServiceImpl extends ServiceImpl<DiscussionTopicReplyMapper, DiscussionTopicReply> implements IDiscussionTopicReplyService {

    @Autowired
    private DiscussionTopicReplyMapper discussionTopicReplyMapper;

    @Autowired
    private DiscussionTopicMapper discussionTopicMapper;

    @Autowired
    private UserLikesMapper userLikesMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private SysUserMapper sysTbUserMapper;

    @Autowired
    private TextbookMapper textbookMapper;

    @Override
    public Boolean insert(DiscussionTopicReply reply) {
        if (ObjectUtils.isEmpty(reply)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.save(reply);
    }

    @Override
    public Boolean deleteDictItemByIds(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        List<Long> idList = idParam.getIdList();
        return this.removeBatchByIds(idList);
    }

    @Override
    public Page<DiscussionTopicMyPageReturnParam> getMyReply(DiscussionTopicMyPageSearchParam param) {
        if (ObjectUtils.isEmpty(UserUtils.get().getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户id为空");
        }
        Long id = UserUtils.get().getId();
        List<DiscussionTopicReply> discussionTopicReplies = discussionTopicReplyMapper.selectList(new MyLambdaQueryWrapper<DiscussionTopicReply>()
                .eq(DiscussionTopicReply::getCreator, id)
                .between(
                        ObjectUtils.isNotEmpty(param.getStartTime()) && ObjectUtils.isNotEmpty(param.getEndTime()),
                        DiscussionTopicReply::getAddDatetime,
                        param.getStartTime(),
                        param.getEndTime()
                )
                .orderBy(true, param.getIsAsc() == 1, DiscussionTopicReply::getAddDatetime)
        );

        if (discussionTopicReplies.isEmpty()) {
            return new Page<>();
        }
        // 查 topic 表，获取标题和教材信息
        Map<Long, String> topicIdToTitleMap = new HashMap<>();
        // 定义用于存储教材信息的Map
        Map<Long, String> topicIdToTextbookNameMap = new HashMap<>();

        // 找出所有 topicId (为了后续查询topic和textbook)
        Map<Long, Long> allReplyIdToTopicIdMap = new HashMap<>();
        Set<Long> allTopicIds = new HashSet<>();
        // 使用一个临时的Map来辅助解析，避免重复计算
        Map<Long, DiscussionTopicReply> allReplyMap = discussionTopicReplies.stream()
                .collect(Collectors.toMap(DiscussionTopicReply::getId, r -> r));

        for (DiscussionTopicReply reply : discussionTopicReplies) {
            Long topicId = resolveTopicId(reply, allReplyMap); // 解析根话题ID
            if (topicId != null) {
                allTopicIds.add(topicId);
                allReplyIdToTopicIdMap.put(reply.getId(), topicId);
            }
        }

        if (!allTopicIds.isEmpty()) {
            List<DiscussionTopic> topics = discussionTopicMapper.selectList(
                    new LambdaQueryWrapper<DiscussionTopic>().in(DiscussionTopic::getId, allTopicIds)
            );

            // 提取所有相关的 textbookId
            Set<Long> textbookIds = topics.stream()
                    .map(DiscussionTopic::getTextbookId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, String> textbookIdToNameMap = new HashMap<>();
            if (!textbookIds.isEmpty()) {
                List<Textbook> textbooks = textbookMapper.selectList(
                        new LambdaQueryWrapper<Textbook>().in(Textbook::getId, textbookIds)
                );
                textbookIdToNameMap = textbooks.stream()
                        .collect(Collectors.toMap(Textbook::getId, Textbook::getTextbookName));
            }

            // 构造 topicId -> title 和 topicId -> textbookName 的映射
            for (DiscussionTopic topic : topics) {
                topicIdToTitleMap.put(topic.getId(), topic.getTopicTitle());
                if (topic.getTextbookId() != null) {
                    String textbookName = textbookIdToNameMap.getOrDefault(topic.getTextbookId(), "【教材不存在】");
                    topicIdToTextbookNameMap.put(topic.getId(), textbookName);
                }
            }
        }

        // 进行过滤和封装
        List<DiscussionTopicMyPageReturnParam> allResultList = discussionTopicReplies.stream()
                .map(reply -> {
                    DiscussionTopicMyPageReturnParam vo = new DiscussionTopicMyPageReturnParam();
                    // 基本信息
                    vo.setReplyId(reply.getId());
                    vo.setReplyContent(reply.getReplyContent());
                    vo.setAddDatetime(reply.getAddDatetime());

                    // 话题和教材信息
                    Long resolvedTopicId = allReplyIdToTopicIdMap.get(reply.getId());
                    String topicTitle = topicIdToTitleMap.getOrDefault(resolvedTopicId, "【话题不存在】");
                    String textbookName = topicIdToTextbookNameMap.getOrDefault(resolvedTopicId, "【教材不存在】");
                    vo.setTopicTitle(topicTitle);
                    vo.setTextbookName(textbookName);

                    return vo;
                })
                .filter(vo -> {
                    // 如果参数中有教材名称，则在这里进行过滤
                    if (StringUtils.isNotEmpty(param.getTextbookName())) {
                        // 如果教材名称不匹配，则过滤掉。注意处理null的情况。
                        return param.getTextbookName().equals(vo.getTextbookName());
                    }
                    // 如果没有传教材名称，则不过滤
                    return true;
                })
                .collect(Collectors.toList());


        // -- 手动设置分页 (基于过滤后的列表) --
        if (allResultList.isEmpty()) {
            return new Page<>();
        }

        int total = allResultList.size();
        int fromIndex = (int)((param.getCurrent() - 1) * param.getSize());
        // 防止索引越界
        if (fromIndex >= total) {
            Page<DiscussionTopicMyPageReturnParam> emptyPage = new Page<>();
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(total);
            emptyPage.setCurrent(param.getCurrent());
            emptyPage.setSize(param.getSize());
            return emptyPage;
        }
        int toIndex = Math.min(fromIndex + param.getSize().intValue(), total);

        // 获取当前页的记录
        List<DiscussionTopicMyPageReturnParam> currentPageResultList = allResultList.subList(fromIndex, toIndex);

        // -- 只需要为当前页的记录查询点赞和回复数 --
        Set<Long> currentPageReplyIds = currentPageResultList.stream()
                .map(DiscussionTopicMyPageReturnParam::getReplyId)
                .collect(Collectors.toSet());

        if (!currentPageReplyIds.isEmpty()) {
            // 获取点赞数
            Map<Long, Long> likeCountMap = userLikesMapper.selectList(
                    new LambdaQueryWrapper<UserLikes>()
                            .eq(UserLikes::getType, 2)
                            .in(UserLikes::getCorrelationId, currentPageReplyIds)
            ).stream().collect(Collectors.groupingBy(UserLikes::getCorrelationId, Collectors.counting()));

            // 获取回复数
            Map<Long, Long> replyCountMap = discussionTopicReplyMapper.selectList(
                    new LambdaQueryWrapper<DiscussionTopicReply>()
                            .eq(DiscussionTopicReply::getType, 2)
                            .in(DiscussionTopicReply::getTopicId, currentPageReplyIds)
            ).stream().collect(Collectors.groupingBy(DiscussionTopicReply::getTopicId, Collectors.counting()));

            // 填充点赞和回复数
            currentPageResultList.forEach(vo -> {
                vo.setLikeNumber(likeCountMap.getOrDefault(vo.getReplyId(), 0L).intValue());
                vo.setReplyNumber(replyCountMap.getOrDefault(vo.getReplyId(), 0L).intValue());
            });
        }

        // 封装最终的分页结果
        Page<DiscussionTopicMyPageReturnParam> pageResult = new Page<>();
        pageResult.setRecords(currentPageResultList);
        pageResult.setTotal(total); // total是过滤后的总数
        pageResult.setCurrent(param.getCurrent());
        pageResult.setSize(param.getSize());

        return pageResult;
    }

    @Override
    public Page<DiscussionTopicReplyPageReturnParam> getReply(DiscussionTopicReplyPageSearchParam param) {
        if (param == null || param.getTopicId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "话题ID不能为空");
        }

        Long loginUserId = Optional.ofNullable(UserUtils.get()).map(u -> u.getId()).orElse(null);
        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());
        long offset  = (current - 1) * size;

        long total = discussionTopicReplyMapper.countRootReplies(param.getTopicId());
        Page<DiscussionTopicReplyPageReturnParam> page = new Page<>(current, size, total);
        if (total == 0) {
            page.setRecords(Collections.emptyList());
            return page;
        }

        List<DiscussionTopicReplyPageReturnParam> records =
                discussionTopicReplyMapper.selectReplyPageWithDescCount(
                        param.getTopicId(), loginUserId, param.getOrder(), size, offset
                );
        page.setRecords(records);
        return page;
    }





    @Override
    public Page<DiscussionTopicSecondReplyPageReturnParam> getSecondReply(DiscussionTopicSecondReplyPageSearchParam param) {

        // 参数 & 登录校验
        if (param == null || param.getReplyId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "replyId 不能为空");
        }

        if (UserUtils.get().getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "当前用户未登录");
        }
        Long loginUserId = UserUtils.get().getId();
        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());

        // BFS 拉取整条二级回复链
        Set<Long> frontierIds = new HashSet<>(Collections.singleton(param.getReplyId()));
        Map<Long, DiscussionTopicReply> chainReplyMap = new LinkedHashMap<>();  // 保留插入顺序
        int depthGuard = 0, maxDepth = 50;   // 最多 6 层防炸（基本够用）

        while (!frontierIds.isEmpty() && depthGuard++ < maxDepth) {
            List<DiscussionTopicReply> layer = discussionTopicReplyMapper.selectList(
                    new LambdaQueryWrapper<DiscussionTopicReply>()
                            .eq(DiscussionTopicReply::getType, 2)
                            .eq(DiscussionTopicReply::getIsShield, 0)
                            .in(DiscussionTopicReply::getTopicId, frontierIds)
            );
            frontierIds = new HashSet<>();
            for (DiscussionTopicReply r : layer) {
                if (chainReplyMap.putIfAbsent(r.getId(), r) == null && r.getIsShield() != 1) {   // 新节点才继续往下
                    frontierIds.add(r.getId());
                }
            }
        }
        if (chainReplyMap.isEmpty()) {
            return new Page<>(current, size);
        }

        List<DiscussionTopicReply> chainReplies = new ArrayList<>(chainReplyMap.values());

        // 点赞统计
        Set<Long> replyIds   = chainReplies.stream().map(DiscussionTopicReply::getId).collect(Collectors.toSet());
        Set<Long> creatorIds = chainReplies.stream().map(DiscussionTopicReply::getCreator).collect(Collectors.toSet());

        Map<Long, Long> likeCountMap = userLikesMapper.selectList(
                new LambdaQueryWrapper<UserLikes>()
                        .eq(UserLikes::getType, 2)
                        .in(UserLikes::getCorrelationId, replyIds)
        ).stream().collect(Collectors.groupingBy(UserLikes::getCorrelationId, Collectors.counting()));

        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, String> roleMap = new HashMap<>();
        Map<Long, String> pictureMap = new HashMap<>();

        // 姓名映射：学生 → 老师 → 匿名
        if (!creatorIds.isEmpty()) {
            // 先从学生表查询
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>().in(Student::getUserId, creatorIds)
            );
            students.forEach(student -> {
                nameMap.put(student.getUserId(), student.getName());
                roleMap.put(student.getUserId(), "学生");
            });

            // 再用老师补全
            Set<Long> missingIds = creatorIds.stream()
                    .filter(uid -> !nameMap.containsKey(uid))
                    .collect(Collectors.toSet());
            if (!missingIds.isEmpty()) {
                List<Teacher> teachers = teacherMapper.selectList(
                        new LambdaQueryWrapper<Teacher>().in(Teacher::getUserId, missingIds)
                );
                teachers.forEach(teacher -> {
                    nameMap.put(teacher.getUserId(), teacher.getName());
                    roleMap.put(teacher.getUserId(), "教师");
                });
            }

            List<SysTbuser> users = sysTbUserMapper.selectList(
                    new LambdaQueryWrapper<SysTbuser>().in(SysTbuser::getId, creatorIds)
            );
            // 将用户列表转换为 UserId -> UserPicture 的 Map
            users.forEach(user -> {
                if (user.getUserPicture() != null) {
                    pictureMap.put(user.getId(), user.getUserPicture());
                }
            });
        }
        Function<Long, String> safeName = uid -> nameMap.getOrDefault(uid, "【匿名】");

        // 时间升序排序
        chainReplies.sort(Comparator.comparing(DiscussionTopicReply::getAddDatetime));

        // 构造 VO
        Map<Long, String> parentNameMap = chainReplies.stream()
                .collect(Collectors.toMap(DiscussionTopicReply::getId,
                        r -> safeName.apply(r.getCreator())));

        List<DiscussionTopicSecondReplyPageReturnParam> voAll = chainReplies.stream().map(r -> {
            DiscussionTopicSecondReplyPageReturnParam vo = new DiscussionTopicSecondReplyPageReturnParam();
            BeanUtils.copyProperties(r, vo);

            // 拼接 @ 被回复人
            if (!Objects.equals(r.getTopicId(), param.getReplyId())) {
                String targetName = parentNameMap.getOrDefault(r.getTopicId(), "【匿名】");
                vo.setReplyContent("@" + targetName + "：" + r.getReplyContent());
            }

            vo.setCreatorName(safeName.apply(r.getCreator()));
            vo.setCreatorRole(roleMap.getOrDefault(r.getCreator(), "匿名")); // 设置角色
            vo.setLikeNumber(likeCountMap.getOrDefault(r.getId(), 0L).intValue());
            vo.setUserPicture(pictureMap.getOrDefault(r.getCreator(), null)); // 设置头像，若无则使用默认值
            vo.setIsMine(Objects.equals(r.getCreator(), loginUserId) ? 1 : 0);
            return vo;
        }).collect(Collectors.toList());

        // 手动分页
        int from = (int) ((current - 1) * size);
        if (from >= voAll.size()) {
            return new Page<>(current, size);   // 越界空页
        }
        int to = Math.min(from + (int) size, voAll.size());
        List<DiscussionTopicSecondReplyPageReturnParam> pageRecords = voAll.subList(from, to);

        // 组装 Page
        Page<DiscussionTopicSecondReplyPageReturnParam> resultPage = new Page<>();
        resultPage.setCurrent(current);
        resultPage.setSize(size);
        resultPage.setTotal(voAll.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
    }


    @Override
    public R<DiscussionTopicMyReturnParam> getMyReplyContent(DiscussionTopicMySearchParam param) {

        // 参数 & 登录校验
        if (param == null || param.getReplyId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "replyId 不能为空");
        }
        Long loginUserId = UserUtils.get().getId();
        if (loginUserId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "当前用户未登录");
        }

        // 查询“我这条回复”
        DiscussionTopicReply myReply = discussionTopicReplyMapper.selectById(param.getReplyId());
        if (myReply == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "回复不存在");
        }
        if (!Objects.equals(myReply.getCreator(), loginUserId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该回复不属于当前用户");
        }

        // 解析话题 &（可选）父回复
        Long rootTopicId;
        DiscussionTopicReply parentReply = null;   // 仅 myReply.type == 2 时使用
        if (myReply.getType() == 1) {
            rootTopicId = myReply.getTopicId();
        } else {
            parentReply = discussionTopicReplyMapper.selectById(myReply.getTopicId());
            rootTopicId = resolveRootTopicId(parentReply);
        }

        DiscussionTopic topic = discussionTopicMapper.selectById(rootTopicId);
        String topicTitle = topic != null ? topic.getTopicTitle() : "【未知话题】";

        // 查询子回复
        List<DiscussionTopicReply> childReplies = discussionTopicReplyMapper.selectList(
                new LambdaQueryWrapper<DiscussionTopicReply>()
                        .eq(DiscussionTopicReply::getType, 2)
                        .eq(DiscussionTopicReply::getIsShield, 0)
                        .eq(DiscussionTopicReply::getTopicId, myReply.getId())
                        .orderByAsc(DiscussionTopicReply::getAddDatetime)
        );

        // 统计点赞
        Set<Long> idsForLike = new HashSet<>();
        idsForLike.add(myReply.getId());
        childReplies.forEach(r -> idsForLike.add(r.getId()));

        Map<Long, Long> likeCountMap = userLikesMapper.selectList(
                new LambdaQueryWrapper<UserLikes>()
                        .eq(UserLikes::getType, 2)
                        .in(UserLikes::getCorrelationId, idsForLike)
        ).stream().collect(Collectors.groupingBy(
                UserLikes::getCorrelationId, Collectors.counting()
        ));

        Map<Long, String> nameMap = new HashMap<>();
        Map<Long, String> roleMap = new HashMap<>();
        Map<Long, String> pictureMap = new HashMap<>();

        // 姓名映射：student → teacher → 匿名
        // 收集所有待查 creatorId
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.add(myReply.getCreator());
        childReplies.forEach(r -> creatorIds.add(r.getCreator()));
        if (parentReply != null) creatorIds.add(parentReply.getCreator());

        // 先 student 表
        if (!creatorIds.isEmpty()) {
            // 先从学生表查询
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>().in(Student::getUserId, creatorIds)
            );
            students.forEach(student -> {
                nameMap.put(student.getUserId(), student.getName());
                roleMap.put(student.getUserId(), "学生");
            });

            // 再用老师表补全
            Set<Long> missingIds = creatorIds.stream()
                    .filter(uid -> !nameMap.containsKey(uid))
                    .collect(Collectors.toSet());
            if (!missingIds.isEmpty()) {
                Map<Long, String> teacherNameMap = teacherMapper.selectList(
                        new LambdaQueryWrapper<Teacher>().in(Teacher::getUserId, missingIds)
                ).stream().collect(Collectors.toMap(Teacher::getUserId, Teacher::getName));

                // 填充老师的姓名和角色
                teacherNameMap.forEach((userId, name) -> {
                    nameMap.put(userId, name);
                    roleMap.put(userId, "教师");
                });
            }
            List<SysTbuser> users = sysTbUserMapper.selectList(
                    new LambdaQueryWrapper<SysTbuser>().in(SysTbuser::getId, creatorIds)
            );
            // 将用户列表转换为 UserId -> UserPicture 的 Map
            users.forEach(user -> {
                if (user.getUserPicture() != null) {
                    pictureMap.put(user.getId(), user.getUserPicture());
                }
            });
        }

        // 安全获取姓名函数
        Function<Long, String> safeName = uid -> nameMap.getOrDefault(uid, "【匿名】");

        // 组装 replyList
        List<DiscussionTopicMyReplyList> replyList = new ArrayList<>();

        // 我的这条
        replyList.add(new DiscussionTopicMyReplyList()
                .setId(myReply.getId())
                .setReplyContent(myReply.getReplyContent())
                .setCreatorName(safeName.apply(myReply.getCreator()))
                .setAddDatetime(myReply.getAddDatetime())
                .setLikeNumber(likeCountMap.getOrDefault(myReply.getId(), 0L).intValue())
                .setUserPicture(pictureMap.getOrDefault(myReply.getCreator(), null)) // 设置头像，若无则使用默认值
        );

        // 子回复
        childReplies.forEach(child -> replyList.add(
                new DiscussionTopicMyReplyList()
                        .setId(child.getId())
                        .setReplyContent(child.getReplyContent())
                        .setCreatorName(safeName.apply(child.getCreator()))
                        .setCreatorRole(roleMap.getOrDefault(child.getCreator(), "匿名"))
                        .setAddDatetime(child.getAddDatetime())
                        .setUserPicture(pictureMap.getOrDefault(myReply.getCreator(), null))
                        .setLikeNumber(likeCountMap.getOrDefault(child.getId(), 0L).intValue())
        ));

        // DTO 返回
        DiscussionTopicMyReturnParam dto = new DiscussionTopicMyReturnParam()
                .setTopicTitle(topicTitle)
                .setReplyList(replyList)
                .setIsMine(1);   // 个人中心固定本人

        if (myReply.getType() == 2 && parentReply != null) {
            dto.setReplyContent(parentReply.getReplyContent())
                    .setReplyAuthor(safeName.apply(parentReply.getCreator()))
                    .setReplyAuthorRole(roleMap.getOrDefault(parentReply.getCreator(), "匿名")) // 设置父回复作者的角色
                    .setReplyAuthorPicture(pictureMap.getOrDefault(myReply.getCreator(), null))
                    .setReplyDateTime(parentReply.getAddDatetime());
        }

        return R.ok(dto);
    }

    @Override
    public Boolean updateReply(DiscussionTopicReply reply) {
        if (ObjectUtils.isEmpty(reply) || ObjectUtils.isEmpty(reply.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(reply);
    }

    //  向上追溯到顶层 type = 1
    private Long resolveRootTopicId(DiscussionTopicReply reply) {
        int maxDepth = 10;
        int depth = 0;
        DiscussionTopicReply cur = reply;
        while (cur != null && cur.getType() == 2 && depth < maxDepth) {
            cur = discussionTopicReplyMapper.selectById(cur.getTopicId());
            depth++;
        }
        if (cur != null && cur.getType() == 1) {
            return cur.getTopicId();
        }
        return null;
    }

    //  向上追溯到顶层 type = 1
    private Long resolveTopicId(DiscussionTopicReply reply, Map<Long, DiscussionTopicReply> replyMap) {
        int maxDepth = 10; // 防止死循环
        int depth = 0;
        while (reply != null && reply.getType() == 2 && depth < maxDepth) {
            DiscussionTopicReply parentReply = replyMap.get(reply.getTopicId());
            if (parentReply == null) break;
            reply = parentReply;
            depth++;
        }
        // 此时 reply 应该是 type = 1 的
        if (reply != null && reply.getType() == 1) {
            return reply.getTopicId();
        }
        return null;
    }

    public Integer getTopicReplyCount(Long topicId) {
        if (ObjectUtils.isEmpty(topicId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "topicIdId 不能为空");
        }
        Long total = discussionTopicReplyMapper.countTopicWithReplies(topicId);
        return total.intValue();

    }

    private Page<DiscussionTopicMyPageReturnParam> emptyPage(DiscussionTopicMyPageSearchParam p, long total) {
        Page<DiscussionTopicMyPageReturnParam> page = new Page<>();
        page.setRecords(Collections.emptyList());
        page.setTotal(total);
        page.setCurrent(p.getCurrent());
        page.setSize(p.getSize());
        return page;
    }


}
