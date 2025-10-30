package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teachingactivities.param.*;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.entity.UserLikes;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.param.TextbookAuthorityDetailReturnParam;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.textbook.service.impl.TextbookClassificationServiceImpl;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
public class DiscussionTopicServiceImpl extends ServiceImpl<DiscussionTopicMapper, DiscussionTopic> implements IDiscussionTopicService {

    @Autowired
    private ISysUserService sysTbuserService;
    @Autowired
    private ITeacherService teacherService;
    @Autowired
    private IStudentService studentService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ITextbookCatalogService textbookCatalogService;
    @Autowired
    private DiscussionTopicReplyServiceImpl discussionTopicReplyService;
    @Autowired
    private TextbookClassificationServiceImpl textbookClassificationService;
    @Autowired
    private ITextbookAuthorityService textbookAuthorityService;
    @Autowired
    private DiscussionTopicMapper discussionTopicMapper;
    @Autowired
    private com.upc.modular.teachingactivities.mapper.UserLikesMapper userLikesMapper;

    @Override
    public void deleteDiscussionTopicByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public Long insertDiscussionTopic(DiscussionTopic discussionTopic) {
        if (discussionTopic == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (StringUtils.isBlank(discussionTopic.getTopicTitle()) || StringUtils.isBlank(discussionTopic.getTopicContent())){
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "标题或内容不能为空");
        }
        this.save(discussionTopic);

        return discussionTopic.getId();
    }

    @Override
    public void updateDiscussionTopicById(DiscussionTopic discussionTopic) {
        if (discussionTopic == null || discussionTopic.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
//        if (StringUtils.isBlank(discussionTopic.getTopicTitle()) || StringUtils.isBlank(discussionTopic.getTopicContent())){
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "标题或内容不能为空");
//        }
        // 确保不保存章节ID，即使前端传入了该参数
        discussionTopic.setTextbookCatalogId(null);
        this.updateById(discussionTopic);
    }

    @Override
    public Page<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param) {
        Long userId = UserUtils.get().getId();

        Page<DiscussionTopic> page = new Page<>(param.getCurrent(), param.getSize());
        QueryWrapper<DiscussionTopic> queryWrapper = new QueryWrapper<>();

        queryWrapper.like(StringUtils.isNotBlank(param.getTopicTitle()), "topic_title", param.getTopicTitle());
        queryWrapper.eq(param.getType() != null, "type", param.getType());
        queryWrapper.eq(param.getMessageType() != null, "message_type", param.getMessageType());
        queryWrapper.eq(param.getTextbookId() != null, "textbook_id", param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, "textbook_catalog_id", param.getTextbookCatalogId());
        queryWrapper.eq(param.getIdentityType() != null, "identity_type", param.getIdentityType());

        // 排序逻辑
        Integer sortType = param.getSortType();
        if (sortType == null) {
            sortType = 0; // 如果前端未提供排序类型，默认为0
        }

        switch (sortType) {
            case 1:
                // 按最新一条回复时间排序
                // 将完整的 ORDER BY 子句放入 last() 中
                queryWrapper.last("ORDER BY (SELECT MAX(operation_datetime) FROM discussion_topic_reply WHERE discussion_topic_reply.topic_id = discussion_topic.id) DESC NULLS LAST, add_datetime DESC");
                break;
            case 2:
                // 按回复数排序
                queryWrapper.last("ORDER BY (SELECT COUNT(id) FROM discussion_topic_reply WHERE discussion_topic_reply.topic_id = discussion_topic.id) DESC, add_datetime DESC");
                break;
            default:
                // 默认(sortType=0)排序方式，因为语法简单，可以继续使用 orderByDesc
                queryWrapper.orderByDesc("add_datetime");
                break;
        }


        IPage<DiscussionTopic> topicPage = this.page(page, queryWrapper);

        // 权限过滤：只返回用户有权限查看的教学活动
        List<DiscussionTopic> filteredTopics = topicPage.getRecords().stream()
                .filter(topic -> {
                    // 1. 如果用户是该教学活动的创建人，则有权限查看
                    if (topic.getCreator() != null && topic.getCreator().equals(userId)) {
                        return true;
                    }
                    
                    // 2. 检查用户是否是教材的协作者
                    if (topic.getTextbookId() != null) {
                        // 创建查询参数
                        TextbookAuthoritySearchParam searchParam = new TextbookAuthoritySearchParam();
                        searchParam.setTextbookId(topic.getTextbookId());
                        searchParam.setAuthorityType(1); // 1表示协作者
                        searchParam.setCurrent(1L);
                        searchParam.setSize(100L); // 设置足够大的页大小以获取所有记录
                        
                        // 调用getTextbookAuthorityPage方法查询协作者列表
                        Page<TextbookAuthorityDetailReturnParam> authorityPage = textbookAuthorityService.getTextbookAuthorityPage(searchParam);
                        
                        // 检查返回的协作者列表中是否包含当前用户
                        if (authorityPage.getRecords() != null && !authorityPage.getRecords().isEmpty()) {
                            return authorityPage.getRecords().stream()
                                    .anyMatch(authority -> authority.getTeacher() != null && 
                                            authority.getTeacher().getUserId() != null && 
                                            authority.getTeacher().getUserId().equals(userId));
                        }
                    }
                    
                    // 如果没有通过上述任一权限检查，则无权限查看
                    return false;
                })
                .collect(Collectors.toList());

        if (filteredTopics.isEmpty()) {
            return new Page<>(param.getCurrent(), param.getSize(), 0);
        }

        // 数据转换
        List<DiscussionTopicReturnParam> returnList = filteredTopics.stream()
                .map(discussionTopic -> {
                    DiscussionTopicReturnParam returnParam = new DiscussionTopicReturnParam();

                    // 基础属性映射
                    returnParam.setId(discussionTopic.getId());
                    returnParam.setActivityName(discussionTopic.getTopicTitle());
                    returnParam.setActivityType(discussionTopic.getType());
                    if (discussionTopic.getAddDatetime() != null) {
                        returnParam.setAddDatetime(discussionTopic.getAddDatetime().toString());
                    }

                    // 关联查询与填充：教材名称
                    if (discussionTopic.getTextbookId() != null) {
                        Textbook textbook = textbookService.getById(discussionTopic.getTextbookId());
                        if (textbook != null) {
                            returnParam.setTextbookName(textbook.getTextbookName());
                        }
                    }

                    // 关联查询与填充：教材目录
                    if (discussionTopic.getTextbookCatalogId() != null) {
                        TextbookCatalog textbookCatalog = textbookCatalogService.getById(discussionTopic.getTextbookCatalogId());
                        if (textbookCatalog != null && textbookCatalog.getCatalogName() != null) {
                            String catalogName = Jsoup.parse(textbookCatalog.getCatalogName()).text();
                            returnParam.setTextbookCatalogName(catalogName);
                        } else {
                            returnParam.setTextbookCatalogName("");
                        }
                    }

                    // 关联查询与填充：回复数
                    Integer topicReplyCount = discussionTopicReplyService.getTopicReplyCount(discussionTopic.getId());
                    returnParam.setReplyCount(topicReplyCount != null ? topicReplyCount : 0);

                    return returnParam;
                })
                .collect(Collectors.toList());

        // 构建并返回最终的分页结果，注意total应该是过滤后的数量
        Page<DiscussionTopicReturnParam> resultPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), filteredTopics.size());
        resultPage.setRecords(returnList);
        return resultPage;
    }
    @Override
    public List<MyJoinDiscussionTopicDiscussionTopicReturnParam> selectMyJoinDiscussionTopic(MyJoinDiscussionTopicSearchParam param) {
        Long userId = UserUtils.get().getId();
        if (ObjectUtils.isEmpty(param.getClassificationId())) {
            param.setClassificationId(0L);
        }
        List<Long> classificationIdList = textbookClassificationService.selectTextbookClassificationSubtreeIdList(param.getClassificationId());

        List<Long> textbookIdList = textbookService.list(
                        new LambdaQueryWrapper<Textbook>().select(Textbook::getId)
                                .like(ObjectUtils.isNotEmpty(param.getTextbookName()), Textbook::getTextbookName, param.getTextbookName())
                                .in(ObjectUtils.isNotEmpty(classificationIdList), Textbook::getClassification, classificationIdList)
                ).stream()
                .map(Textbook::getId)
                .filter(Objects::nonNull)
                .filter(id -> textbookAuthorityService.textbookAuthorityJudge(id, userId))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(textbookIdList)) {
            return Collections.emptyList();
        }

        if (ObjectUtils.isEmpty(param.getTextbookId())) {
            return discussionTopicMapper.selectWithDetailsByTextbookIds(textbookIdList, param.getIdentityType());
        } else {
            if (textbookIdList.contains(param.getTextbookId())) {
                List<Long> newTextbookIdList = new ArrayList<>();
                newTextbookIdList.add(param.getTextbookId());
                return discussionTopicMapper.selectWithDetailsByTextbookIds(newTextbookIdList, param.getIdentityType());
            }
        }
        return Collections.emptyList();
    }


    @Override
    public DiscussionTopicSecondReturnParam getSecondTextbookById(Long id) {
        DiscussionTopicSecondReturnParam discussionTopicReturnParam = new DiscussionTopicSecondReturnParam();
        DiscussionTopic discussionTopic = this.getById(id);

        if (discussionTopic == null) {
            throw new BusinessException(BusinessErrorEnum.FOREIGN_KEY_NOT_FOUND, "讨论话题不存在");
        }

        BeanUtils.copyProperties(discussionTopic, discussionTopicReturnParam);

        // 处理教材信息
        if (discussionTopic.getTextbookId() != null) {
            Textbook textbook = textbookService.getById(discussionTopic.getTextbookId());
            if (textbook != null) {
                discussionTopicReturnParam.setTextbookName(textbook.getTextbookName());
            }
        }

        // 处理教材目录信息 - 添加空值检查
        if (discussionTopic.getTextbookCatalogId() != null) {
            TextbookCatalog textbookCatalog = textbookCatalogService.getById(discussionTopic.getTextbookCatalogId());
            if (textbookCatalog != null && textbookCatalog.getCatalogName() != null) {
                String catalogName = Jsoup.parse(textbookCatalog.getCatalogName()).text();
                discussionTopicReturnParam.setTextbookCatalogName(catalogName);
            } else {
                discussionTopicReturnParam.setTextbookCatalogName("");
            }
        } else {
            discussionTopicReturnParam.setTextbookCatalogName("");
        }

        // 处理创建者信息
        if (discussionTopic.getCreator() != null) {
            SysTbuser sysTbuser = sysTbuserService.getById(discussionTopic.getCreator());
            if (sysTbuser != null) {
                discussionTopicReturnParam.setNickName(sysTbuser.getNickname());
            }
        }
        
        // 添加点赞数统计
        Long likeCount = userLikesMapper.selectCount(
                new LambdaQueryWrapper<UserLikes>()
                        .eq(UserLikes::getType, 1)  // 类型1表示教学活动
                        .eq(UserLikes::getCorrelationId, id)
        );
        discussionTopicReturnParam.setLikeNumber(likeCount != null ? likeCount.intValue() : 0);
        
        // 添加回复数统计
        Integer replyCount = discussionTopicReplyService.getTopicReplyCount(id);
        discussionTopicReturnParam.setReplyNumber(replyCount != null ? replyCount : 0);

        return discussionTopicReturnParam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateCatalog(List<DiscussionTopicBatchUpdateCatalogParam> params) {
        // 1. 基本参数校验
        if (CollectionUtils.isEmpty(params)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "请求参数列表不能为空");
        }

        // 2. 遍历参数列表进行逐个更新
        for (DiscussionTopicBatchUpdateCatalogParam param : params) {
            if (param.getId() == null) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教学活动ID不能为空");
            }

            // 校验章节ID和临时UUID至少要有一个
            if (param.getTextbookCatalogId() == null && StringUtils.isEmpty(param.getTextbookCatalogUuid())) {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                        "更新ID为 " + param.getId() + " 的活动时，章节ID和章节UUID必须至少提供一个");
            }

            Long finalCatalogId;

            // 优先使用章节ID
            if (param.getTextbookCatalogId() != null) {
                finalCatalogId = param.getTextbookCatalogId();
            } else {
                // 如果章节ID为空，则使用临时UUID去数据库查询对应的ID
                TextbookCatalog textbookCatalog = textbookCatalogService.getOne(new LambdaQueryWrapper<TextbookCatalog>()
                        .eq(TextbookCatalog::getCatalogUuid, param.getTextbookCatalogUuid()));

                // 如果根据UUID没有找到对应的章节，则抛出异常
                if (textbookCatalog == null) {
                    throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR,
                            "根据提供的UUID: " + param.getTextbookCatalogUuid() + " 未找到对应的章节");
                }
                finalCatalogId = textbookCatalog.getId();
            }

            // 3. 执行单条更新
            DiscussionTopic updateEntity = new DiscussionTopic();
            updateEntity.setId(param.getId());
            updateEntity.setTextbookCatalogId(finalCatalogId);

            // 使用 updateById 方法进行更新，更为精确和高效
            this.updateById(updateEntity);
        }
    }

}
