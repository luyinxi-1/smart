package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        // 确保不保存章节ID，即使前端传入了该参数
        discussionTopic.setTextbookCatalogId(null);
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
    public Page<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param) {
        Long userId = UserUtils.get().getId();

        Page<DiscussionTopic> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<DiscussionTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getTopicTitle()), DiscussionTopic::getTopicTitle, param.getTopicTitle());
        queryWrapper.eq(param.getType() != null, DiscussionTopic::getType, param.getType());
        queryWrapper.eq(param.getMessageType() != null, DiscussionTopic::getMessageType, param.getMessageType());
        queryWrapper.eq(param.getTextbookId() != null, DiscussionTopic::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, DiscussionTopic::getTextbookCatalogId, param.getTextbookCatalogId());
        queryWrapper.eq(param.getIdentityType() != null, DiscussionTopic::getIdentityType, param.getIdentityType());

        // 执行分页查询
        IPage<DiscussionTopic> topicPage = this.page(page, queryWrapper);

        // 如果查询结果为空，返回一个空的分页对象
        if (topicPage.getRecords().isEmpty()) {
            // 使用 param.getCurrent() 和 param.getSize()
            return new Page<>(param.getCurrent(), param.getSize(), topicPage.getTotal());
        }

        // 转换成 DiscussionTopicReturnParam 列表
        List<DiscussionTopicReturnParam> returnList = topicPage.getRecords().stream()
                .map(discussionTopic -> {
                    DiscussionTopicReturnParam discussionTopicReturnParam = new DiscussionTopicReturnParam();

                    if(discussionTopic.getId() != null){
                        discussionTopicReturnParam.setId(discussionTopic.getId());
                    }else{
                        discussionTopicReturnParam.setId(0L);
                    }

                    if (discussionTopic.getTextbookId() != null) {
                        Textbook textbook = textbookService.getById(discussionTopic.getTextbookId());
                        if (textbook != null) {
                            discussionTopicReturnParam.setTextbookName(textbook.getTextbookName());
                        }
                    }

                    if (discussionTopic.getTextbookCatalogId() != null) {
                        TextbookCatalog textbookCatalog = textbookCatalogService.getById(discussionTopic.getTextbookCatalogId());
                        if (textbookCatalog != null && textbookCatalog.getCatalogName() != null) {
                            String catalogName = Jsoup.parse(textbookCatalog.getCatalogName()).text();
                            discussionTopicReturnParam.setTextbookCatalogName(catalogName);
                        } else {
                            discussionTopicReturnParam.setTextbookCatalogName("");
                        }
                    }
                    if (discussionTopic.getTopicTitle() != null) {
                        discussionTopicReturnParam.setActivityName(discussionTopic.getTopicTitle());
                    }
                    if (discussionTopic.getType() != null) {
                        discussionTopicReturnParam.setActivityType(discussionTopic.getType());
                    }
                    if (discussionTopic.getCreator() != null) {
                        SysTbuser sysTbuser = sysTbuserService.getById(discussionTopic.getCreator());
                        if (sysTbuser != null) {
                            discussionTopicReturnParam.setCreatorName(sysTbuser.getNickname());
                        }
                        // 添加判断是否为当前用户创建
                        discussionTopicReturnParam.setIsCreatedByCurrentUser(discussionTopic.getCreator().equals(userId));
                    }
                    if (discussionTopic.getAddDatetime() != null) {
                        discussionTopicReturnParam.setAddDatetime(discussionTopic.getAddDatetime().toString());
                    }

                    Integer topicReplyCount = discussionTopicReplyService.getTopicReplyCount(discussionTopic.getId());
                    if (topicReplyCount != null) {
                        discussionTopicReturnParam.setReplyCount(topicReplyCount);
                    }
                    return discussionTopicReturnParam;
                })
                .collect(Collectors.toList());

        // 对结果列表进行二次筛选，基于ActivityName（即topicTitle）进行模糊查询
        if (StringUtils.isNotBlank(param.getTopicTitle())) {
            returnList = returnList.stream()
                    .filter(item -> item.getActivityName() != null &&
                            item.getActivityName().toLowerCase().contains(param.getTopicTitle().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // 构建并返回新的 Page 对象
        Page<DiscussionTopicReturnParam> resultPage = new Page<>(topicPage.getCurrent(), topicPage.getSize(), topicPage.getTotal());
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
    public void batchUpdateCatalog(DiscussionTopicBatchUpdateCatalogParam param) {
        if (param == null || CollectionUtils.isEmpty(param.getIds()) || param.getTextbookCatalogId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        
        // 批量更新章节ID
        LambdaQueryWrapper<DiscussionTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DiscussionTopic::getId, param.getIds());
        
        DiscussionTopic updateEntity = new DiscussionTopic();
        updateEntity.setTextbookCatalogId(param.getTextbookCatalogId());
        
        this.update(updateEntity, queryWrapper);
    }

}
