package com.upc.modular.teachingActivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.mapper.StudentMapper;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teachingActivities.entity.DiscussionTopic;
import com.upc.modular.teachingActivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingActivities.entity.UserLikes;
import com.upc.modular.teachingActivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingActivities.mapper.DiscussionTopicReplyMapper;
import com.upc.modular.teachingActivities.mapper.UserLikesMapper;
import com.upc.modular.teachingActivities.param.*;
import com.upc.modular.teachingActivities.service.IDiscussionTopicReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        // --此处需要手动设置分页
        // 计算当前页的起始索引
        int fromIndex = (int)((param.getCurrent() - 1) * param.getSize());
        int toIndex = (int)Math.min(fromIndex + param.getSize(), discussionTopicReplies.size());

        if (fromIndex >= discussionTopicReplies.size()) {
            Page<DiscussionTopicMyPageReturnParam> emptyPage = new Page<>();
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(discussionTopicReplies.size());
            emptyPage.setCurrent(param.getCurrent());
            emptyPage.setSize(param.getSize());
            return emptyPage;
        }

        List<DiscussionTopicReply> currentPageRecords = discussionTopicReplies.subList(fromIndex, toIndex);
        // 构造 id -> reply 的 map
        Map<Long, DiscussionTopicReply> replyMap = currentPageRecords.stream()
                .collect(Collectors.toMap(DiscussionTopicReply::getId, r -> r));

        // 找出所有 topicId
        Set<Long> allTopicIds = new HashSet<>();

        // 存储每条回复最终指向的 topicId（用于绑定标题）
        Map<Long, Long> replyIdToTopicIdMap = new HashMap<>();

        for (DiscussionTopicReply reply : currentPageRecords) {
            Long topicId = resolveTopicId(reply, replyMap);
            if (topicId != null) {
                allTopicIds.add(topicId);
                replyIdToTopicIdMap.put(reply.getId(), topicId);
            }
        }

        // 查 topic 表，获取标题
        Map<Long, String> topicIdToTitleMap;
        if (!allTopicIds.isEmpty()) {
            List<DiscussionTopic> topics = discussionTopicMapper.selectList(
                    new LambdaQueryWrapper<DiscussionTopic>().in(DiscussionTopic::getId, allTopicIds)
            );
            topicIdToTitleMap = topics.stream().collect(Collectors.toMap(
                    DiscussionTopic::getId, DiscussionTopic::getTopicTitle));
        } else {
            topicIdToTitleMap = new HashMap<>();
        }

        // 获取点赞数
        List<DiscussionTopicReply> allChildReplies = discussionTopicReplyMapper.selectList(
                new LambdaQueryWrapper<DiscussionTopicReply>()
                        .eq(DiscussionTopicReply::getType, 2)
                        .in(DiscussionTopicReply::getTopicId, currentPageRecords.stream().map(DiscussionTopicReply::getId).collect(Collectors.toSet()))
        );

        Map<Long, Long> replyCountMap = allChildReplies.stream()
                .collect(Collectors.groupingBy(
                        DiscussionTopicReply::getTopicId,
                        Collectors.counting()
                ));

        // 获取回复数
        Set<Long> replyIds = currentPageRecords.stream()
                .map(DiscussionTopicReply::getId)
                .collect(Collectors.toSet());

        List<UserLikes> replyLikes = userLikesMapper.selectList(
                new LambdaQueryWrapper<UserLikes>()
                        .eq(UserLikes::getType, 2) // 只查回复的点赞
                        .in(UserLikes::getCorrelationId, replyIds)
        );

        Map<Long, Long> likeCountMap = replyLikes.stream()
                .collect(Collectors.groupingBy(
                        UserLikes::getCorrelationId,
                        Collectors.counting()
                ));

        // 封装结果
        List<DiscussionTopicMyPageReturnParam> resultList = currentPageRecords.stream().map(reply -> {
            DiscussionTopicMyPageReturnParam vo = new DiscussionTopicMyPageReturnParam();
            vo.setReplyId(reply.getId());
            vo.setReplyContent(reply.getReplyContent());
            vo.setAddDatetime(reply.getAddDatetime());

            Long resolvedTopicId = replyIdToTopicIdMap.get(reply.getId());
            String topicTitle = topicIdToTitleMap.getOrDefault(resolvedTopicId, "【话题不存在】");
            vo.setTopicTitle(topicTitle);

            vo.setLikeNumber(likeCountMap.getOrDefault(reply.getId(), 0L).intValue());
            vo.setReplyNumber(replyCountMap.getOrDefault(reply.getId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());

        Page<DiscussionTopicMyPageReturnParam> pageResult = new Page<>();
        pageResult.setRecords(resultList);
        pageResult.setTotal(discussionTopicReplies.size());
        pageResult.setCurrent(param.getCurrent());
        pageResult.setSize(param.getSize());

        return pageResult;

    }

    @Override
    public Page<DiscussionTopicReplyPageReturnParam> getReply(DiscussionTopicReplyPageSearchParam param) {

        // 参数校验
        if (param == null || param.getTopicId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "话题ID不能为空");
        }

        Long loginUserId = UserUtils.get().getId();   // 允许为空

        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());

        // 拉取一级回复（type = 1）
        List<DiscussionTopicReply> allReplies = discussionTopicReplyMapper.selectList(
                new LambdaQueryWrapper<DiscussionTopicReply>()
                        .eq(DiscussionTopicReply::getTopicId, param.getTopicId())
                        .eq(DiscussionTopicReply::getType, 1)
        );
        if (allReplies.isEmpty()) {
            return new Page<>(current, size);   // 空页
        }

        // 统计点赞 / 子回复数
        Set<Long> replyIds   = allReplies.stream().map(DiscussionTopicReply::getId).collect(Collectors.toSet());
        Set<Long> creatorIds = allReplies.stream().map(DiscussionTopicReply::getCreator).collect(Collectors.toSet());

        Map<Long, Long> likeCountMap = userLikesMapper.selectList(
                new LambdaQueryWrapper<UserLikes>()
                        .eq(UserLikes::getType, 2)
                        .in(UserLikes::getCorrelationId, replyIds)
        ).stream().collect(Collectors.groupingBy(UserLikes::getCorrelationId, Collectors.counting()));

        Map<Long, Long> replyCountMap = discussionTopicReplyMapper.selectList(
                new LambdaQueryWrapper<DiscussionTopicReply>()
                        .eq(DiscussionTopicReply::getType, 2)
                        .in(DiscussionTopicReply::getTopicId, replyIds)
        ).stream().collect(Collectors.groupingBy(DiscussionTopicReply::getTopicId, Collectors.counting()));

        // 姓名映射：学生 → 老师 → 匿名
        // 先 student
        Map<Long, String> nameMap = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getUserId, creatorIds)
        ).stream().collect(Collectors.toMap(Student::getUserId, Student::getName));

        // 再 teacher 补缺
        Set<Long> missingIds = creatorIds.stream()
                .filter(uid -> !nameMap.containsKey(uid))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            Map<Long, String> teacherNameMap = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>().in(Teacher::getUserId, missingIds)
            ).stream().collect(Collectors.toMap(Teacher::getUserId, Teacher::getName));
            nameMap.putAll(teacherNameMap);
        }

        Function<Long, String> safeName = uid -> nameMap.getOrDefault(uid, "【匿名】");

        // VO 封装
        List<DiscussionTopicReplyPageReturnParam> voList = allReplies.stream().map(reply -> {
            DiscussionTopicReplyPageReturnParam vo = new DiscussionTopicReplyPageReturnParam();
            BeanUtils.copyProperties(reply, vo);

            vo.setLikeNumber(likeCountMap.getOrDefault(reply.getId(), 0L).intValue());
            vo.setReplyNumber(replyCountMap.getOrDefault(reply.getId(), 0L).intValue());
            vo.setCreatorName(safeName.apply(reply.getCreator()));
            vo.setIsMine((loginUserId != null && Objects.equals(reply.getCreator(), loginUserId)) ? 1 : 0);

            return vo;
        }).collect(Collectors.toList());

        // 排序
        if (param.getOrder() != null && param.getOrder() == 1) {          // 1 = 按点赞数倒序
            voList.sort(Comparator.comparingInt(DiscussionTopicReplyPageReturnParam::getLikeNumber).reversed());
        } else {                                                          // 默认：时间倒序
            voList.sort(Comparator.comparing(DiscussionTopicReplyPageReturnParam::getAddDatetime).reversed());
        }

        // 手动分页
        int from = (int) ((current - 1) * size);
        if (from >= voList.size()) {                                      // 越界直接空页
            return new Page<>(current, size);
        }
        int to   = Math.min(from + (int) size, voList.size());
        List<DiscussionTopicReplyPageReturnParam> pageRecords = voList.subList(from, to);

        // 返回 Page
        Page<DiscussionTopicReplyPageReturnParam> resultPage = new Page<>();
        resultPage.setCurrent(current);
        resultPage.setSize(size);
        resultPage.setTotal(voList.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
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
        int depthGuard = 0, maxDepth = 6;   // 最多 6 层防炸（基本够用）

        while (!frontierIds.isEmpty() && depthGuard++ < maxDepth) {
            List<DiscussionTopicReply> layer = discussionTopicReplyMapper.selectList(
                    new LambdaQueryWrapper<DiscussionTopicReply>()
                            .eq(DiscussionTopicReply::getType, 2)
                            .in(DiscussionTopicReply::getTopicId, frontierIds)
            );
            frontierIds = new HashSet<>();
            for (DiscussionTopicReply r : layer) {
                if (chainReplyMap.putIfAbsent(r.getId(), r) == null) {   // 新节点才继续往下
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

        // 姓名映射：学生 → 老师 → 匿名
        Map<Long, String> nameMap = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getUserId, creatorIds)
        ).stream().collect(Collectors.toMap(Student::getUserId, Student::getName));

        Set<Long> missingIds = creatorIds.stream()
                .filter(uid -> !nameMap.containsKey(uid))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            Map<Long, String> teacherNameMap = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>().in(Teacher::getUserId, missingIds)
            ).stream().collect(Collectors.toMap(Teacher::getUserId, Teacher::getName));
            nameMap.putAll(teacherNameMap);
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
            vo.setLikeNumber(likeCountMap.getOrDefault(r.getId(), 0L).intValue());
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

        // 姓名映射：student → teacher → 匿名
        // 收集所有待查 creatorId
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.add(myReply.getCreator());
        childReplies.forEach(r -> creatorIds.add(r.getCreator()));
        if (parentReply != null) creatorIds.add(parentReply.getCreator());

        // 先 student 表
        Map<Long, String> nameMap = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getUserId, creatorIds)
        ).stream().collect(Collectors.toMap(Student::getUserId, Student::getName));

        // 再 teacher 表补缺
        Set<Long> missingIds = creatorIds.stream()
                .filter(uid -> !nameMap.containsKey(uid))
                .collect(Collectors.toSet());
        if (!missingIds.isEmpty()) {
            Map<Long, String> teacherNameMap = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>().in(Teacher::getUserId, missingIds)
            ).stream().collect(Collectors.toMap(Teacher::getUserId, Teacher::getName));
            nameMap.putAll(teacherNameMap);
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
        );

        // 子回复
        childReplies.forEach(child -> replyList.add(
                new DiscussionTopicMyReplyList()
                        .setId(child.getId())
                        .setReplyContent(child.getReplyContent())
                        .setCreatorName(safeName.apply(child.getCreator()))
                        .setAddDatetime(child.getAddDatetime())
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

}
