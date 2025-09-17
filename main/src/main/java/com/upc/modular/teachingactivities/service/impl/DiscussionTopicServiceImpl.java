package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teachingactivities.param.*;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.textbook.service.impl.TextbookClassificationServiceImpl;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public List<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param) {
        LambdaQueryWrapper<DiscussionTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getTopicTitle()), DiscussionTopic::getTopicTitle, param.getTopicTitle());
        queryWrapper.eq(param.getType() != null, DiscussionTopic::getType, param.getType());
        queryWrapper.eq(param.getMessageType() != null, DiscussionTopic::getMessageType, param.getMessageType());
        queryWrapper.eq(param.getTextbookId() != null, DiscussionTopic::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, DiscussionTopic::getTextbookCatalogId, param.getTextbookCatalogId());

        List<DiscussionTopic> topicList = this.list(queryWrapper);
        if (topicList.isEmpty()) {
            return new ArrayList<>();
        }

        List<DiscussionTopicReturnParam> returnList = new ArrayList<>();
        for (DiscussionTopic discussionTopic : topicList) {
            DiscussionTopicReturnParam discussionTopicReturnParam = new DiscussionTopicReturnParam();

            if (discussionTopic.getTextbookId() != null) {
                Textbook textbook = textbookService.getById(discussionTopic.getTextbookId());
                discussionTopicReturnParam.setTextbookName(textbook.getTextbookName());
            }
            if (discussionTopic.getTextbookCatalogId() != null) {
                TextbookCatalog textbookCatalog = textbookCatalogService.getById(discussionTopic.getTextbookCatalogId());
                discussionTopicReturnParam.setTextbookCatalogName(textbookCatalog.getCatalogName());
            }

            Integer topicReplyCount = discussionTopicReplyService.getTopicReplyCount(discussionTopic.getId());
            if (topicReplyCount != null) {
                discussionTopicReturnParam.setReplyCount(topicReplyCount);
            }

            returnList.add(discussionTopicReturnParam);
        }


        return returnList;
    }
/*
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
            return discussionTopicMapper.selectWithDetailsByTextbookIds(textbookIdList);
        } else {
            if (textbookIdList.contains(param.getTextbookId())) {
                List<Long> newTextbookIdList = new ArrayList<>();
                newTextbookIdList.add(param.getTextbookId());
                return discussionTopicMapper.selectWithDetailsByTextbookIds(newTextbookIdList);
            }
        }
        return Collections.emptyList();
    }
*/
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

    List<MyJoinDiscussionTopicDiscussionTopicReturnParam> result;
    if (ObjectUtils.isEmpty(param.getTextbookId())) {
        result = discussionTopicMapper.selectWithDetailsByTextbookIds(textbookIdList);
    } else {
        if (textbookIdList.contains(param.getTextbookId())) {
            List<Long> newTextbookIdList = new ArrayList<>();
            newTextbookIdList.add(param.getTextbookId());
            result = discussionTopicMapper.selectWithDetailsByTextbookIds(newTextbookIdList);
        } else {
            result = Collections.emptyList();
        }
    }

    // 如果提供了identityId，则进行额外筛选
    if (ObjectUtils.isNotEmpty(param.getIdentityId()) && CollectionUtils.isNotEmpty(result)) {
        // 从结果中获取所有唯一的创建者ID
        Set<Long> creatorIds = result.stream()
                .map(DiscussionTopic::getCreator)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!creatorIds.isEmpty()) {
            // 查询这些创建者的用户类型信息
            List<SysTbuser> users = sysTbuserService.list(
                    new LambdaQueryWrapper<SysTbuser>()
                            .in(SysTbuser::getId, creatorIds)
            );
            // 分别处理教师和学生用户
            Set<Long> matchingUserIds = new HashSet<>();
            // 获取教师用户ID列表
            List<Long> teacherUserIds = users.stream()
                    .filter(user -> user.getUserType() != null && user.getUserType() == 2) // 2表示教师
                    .map(SysTbuser::getId)
                    .collect(Collectors.toList());
            // 获取学生用户ID列表
            List<Long> studentUserIds = users.stream()
                    .filter(user -> user.getUserType() != null && user.getUserType() == 1) // 1表示学生
                    .map(SysTbuser::getId)
                    .collect(Collectors.toList());

            // 查询匹配identityId的教师
            if (!teacherUserIds.isEmpty()) {
                List<Teacher> matchingTeachers = teacherService.list(
                        new LambdaQueryWrapper<Teacher>()
                                .in(Teacher::getUserId, teacherUserIds)
                                .eq(Teacher::getIdentityId, param.getIdentityId())
                );

                matchingUserIds.addAll(matchingTeachers.stream()
                        .map(Teacher::getUserId)
                        .collect(Collectors.toSet()));
            }

            // 查询匹配identityId的学生
            if (!studentUserIds.isEmpty()) {
                List<Student> matchingStudents = studentService.list(
                        new LambdaQueryWrapper<Student>()
                                .in(Student::getUserId, studentUserIds)
                                .eq(Student::getIdentityId, param.getIdentityId())
                );

                matchingUserIds.addAll(matchingStudents.stream()
                        .map(Student::getUserId)
                        .collect(Collectors.toSet()));
            }
            // 过滤结果，只保留匹配identityId的记录
            return result.stream()
                    .filter(topic -> matchingUserIds.contains(topic.getCreator()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    return result;
}
// ... existing code ...


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

        return discussionTopicReturnParam;
    }


}
