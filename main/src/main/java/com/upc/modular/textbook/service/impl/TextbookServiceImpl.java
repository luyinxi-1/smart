package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.institution.service.impl.InstitutionServiceImpl;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.teachingactivities.param.DiscussionTopicSecondReplyPageReturnParam;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookAuthority;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.entity.UserFavorites;
import com.upc.modular.textbook.mapper.TextbookAuthorityMapper;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.service.ITextbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
 * @since 2025-07-08
 */
@Service
public class TextbookServiceImpl extends ServiceImpl<TextbookMapper, Textbook> implements ITextbookService {

    @Autowired
    private TextbookMapper textbookMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TextbookClassificationServiceImpl textbookClassificationService;
    @Autowired
    private TextbookAuthorityMapper textbookAuthorityMapper;
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private InstitutionServiceImpl institutionService;




    @Override
    public void insert(Textbook textbook) {
        if (ObjectUtils.isEmpty(textbook)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        if (ObjectUtils.isEmpty(textbook.getReleaseStatus())) {
            textbook.setReleaseStatus(0);
        }
        if (ObjectUtils.isEmpty(textbook.getReviewStatus())) {
            textbook.setReviewStatus(0);
        }
        if (ObjectUtils.isEmpty(textbook.getTextbookAuthorId())) {
            List<Teacher> teachers = teacherMapper.selectList(new MyLambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, UserUtils.get().getId()));
            if (ObjectUtils.isNotEmpty(teachers) && ObjectUtils.isNotEmpty(teachers.get(0).getId())) {
                textbook.setTextbookAuthorId(teachers.get(0).getId());
            }
        }
        this.save(textbook);
    }

    @Override
    public void deleteDictItemByIds(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        textbookMapper.deleteBatchIds(idParam.getIdList());
    }

    @Override
    public void updateTextbook(Textbook textbook) {
        if (ObjectUtils.isEmpty(textbook) || ObjectUtils.isEmpty(textbook.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        this.updateById(textbook);
    }

    @Override
    public Page<TextbookPageReturnParam> getPage(TextbookPageSearchParam param) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getUserType())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户类型为空");
        }
        List<TextbookPageReturnParam> textbookPageReturnParams = new ArrayList<>();
        if (ObjectUtils.isEmpty(param.getClassificationId())) {
            textbookPageReturnParams = textbookMapper.selectTextbookPage(param, Collections.emptyList(), UserUtils.get().getUserType());
        } else {
            List<Long> classificationIds = textbookClassificationService.selectTextbookClassificationSubtreeIdList(param.getClassificationId());
             textbookPageReturnParams = textbookMapper.selectTextbookPage(param, classificationIds, UserUtils.get().getUserType());
        }
        List<TextbookPageReturnParam> returnParams = new ArrayList<>();
        if (UserUtils.get().getUserType() == 0) {
            returnParams.addAll(textbookPageReturnParams);
        } else {
            for (TextbookPageReturnParam returnParam : textbookPageReturnParams) {
                Integer integer = textbookAuthorityEditJudge(returnParam.getId(), UserUtils.get().getId());
                if (integer == 1) {
                    returnParam.setViewStatus(1);
                    returnParams.add(returnParam);
                }
                if (integer == 2){
                    returnParam.setViewStatus(2);
                    returnParams.add(returnParam);
                }
            }
        }
        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());
        int from = (int) ((current - 1) * size);
        if (from >= returnParams.size()) {
            return new Page<>(current, size);   // 越界空页
        }
        int to = Math.min(from + (int) size, returnParams.size());
        List<TextbookPageReturnParam> pageRecords = returnParams.subList(from, to);

        // 组装 Page
        Page<TextbookPageReturnParam> resultPage = new Page<>();
        resultPage.setCurrent(current);
        resultPage.setSize(size);
        resultPage.setTotal(returnParams.size());
        resultPage.setRecords(pageRecords);
        return resultPage;


    }

    @Override
    public List<Textbook> getNewTextbook(int getNumber) {
        MyLambdaQueryWrapper<Textbook> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByDesc(Textbook::getAddDatetime)
                .eq(Textbook::getReleaseStatus, 1)
                .eq(Textbook::getReviewStatus, 1)
                .last("LIMIT " + getNumber);
        return textbookMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public TextbookPageReturnParam getOneTextbookDetails(Long textbookId) {
        if (ObjectUtils.isEmpty(UserUtils.get()) || ObjectUtils.isEmpty(UserUtils.get().getUserType())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户类型为空");
        }
        return textbookMapper.getOneTextbookDetails(textbookId, UserUtils.get().getUserType());
    }


    /**
     * 这是从 TextbookAuthorityServiceImpl 复制过来的权限判断逻辑
     * @param textBookId 教材ID
     * @param userId 用户ID
     * @return boolean 是否有权限
     */
    private boolean hasPermission(Long textBookId, Long userId) {
        // 这部分逻辑与您的 textbookAuthorityJudge 完全相同
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null || tbuser.getInstitutionId() == null) {
            // 如果用户或其机构信息不存在，视为无权限
            return false;
        }
        Long userInstitutionId = tbuser.getInstitutionId();

        // 查询该教材所有“机构可见”的权限记录
        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textBookId);
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 2);
        // 注意：这里我们使用注入的 textbookAuthorityMapper 来查询
        List<TextbookAuthority> textbookAuthorities = textbookAuthorityMapper.selectList(queryWrapper);

        if (textbookAuthorities.isEmpty()) {
            return false;
        }

        // 遍历所有可见机构设置，判断用户所属机构是否被包含
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            Long visibleInstituteId = textbookAuthority.getVisibleInstituteId();
            if (visibleInstituteId != null) {
                boolean result = institutionService.judgeInclusion(userInstitutionId, visibleInstituteId);
                if (result) {
                    return true; // 只要有一个满足条件，就代表有权限
                }
            }
        }
        return false;
    }

    @Override
    public Page<Textbook> getpageTextbookCenter(UserFavoritesPageSearch param) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        Long currentUserId = userInfoToRedis.getId();

        List<Textbook> allTextbooks = this.list(); // 从数据库加载所有教材到内存
        List<Textbook> authorizedTextbooks = new ArrayList<>();
        for (Textbook textbook : allTextbooks) {
            // 调用您指定的权限判断接口

            boolean hasAuthority = this.hasPermission(textbook.getId(), currentUserId);
            // 作者本人默认拥有权限
            boolean isAuthor = currentUserId.equals(textbook.getTextbookAuthorId());

            if (hasAuthority || isAuthor) {
                authorizedTextbooks.add(textbook);
            }
        }
        List<Textbook> filteredTextbooks;
        final Long classification = param.getClassification();
        final String textbookName = param.getTextbookName();

        final boolean isClassificationEmpty = (classification == null);
        final boolean isTextbookNameEmpty = (textbookName == null || textbookName.trim().isEmpty());
        filteredTextbooks = authorizedTextbooks.stream()
                .filter(textbook -> {
                    // 情况一：两个查询条件都为空，不过滤，全部返回
                    if (isClassificationEmpty && isTextbookNameEmpty) {
                        return true;
                    }
                    // 情况二：分类不为空，名称为空
                    if (!isClassificationEmpty && isTextbookNameEmpty) {
                        return classification.equals(textbook.getClassification());
                    }
                    // 情况三：分类为空，名称不为空
                    if (isClassificationEmpty && !isTextbookNameEmpty) {
                        // contains 实现模糊查询
                        return textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                    }
                    // 情况四：两个查询条件都不为空，必须同时满足
                    if (!isClassificationEmpty && !isTextbookNameEmpty) {
                        boolean classificationMatch = classification.equals(textbook.getClassification());
                        boolean nameMatch = textbook.getTextbookName() != null && textbook.getTextbookName().contains(textbookName.trim());
                        return classificationMatch && nameMatch;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (param.getIsAsc() != null && param.getIsAsc() == 1) {
            filteredTextbooks.sort(Comparator.comparing(Textbook::getAddDatetime)); // 升序
        } else {
            filteredTextbooks.sort(Comparator.comparing(Textbook::getAddDatetime).reversed()); // 降序
        }
        long total = filteredTextbooks.size();
        long current = param.getCurrent();
        long size = param.getSize();

        long fromIndex = (current - 1) * size;
        if (fromIndex >= total) {
            Page<Textbook> resultPage = new Page<>(current, size, 0);
            resultPage.setRecords(new ArrayList<>());
            return resultPage;
        }
        long toIndex = Math.min(fromIndex + size, total);

        List<Textbook> pageRecords = filteredTextbooks.subList((int)fromIndex, (int)toIndex);

        // ==================== 步骤 6: 封装并返回Page对象 ====================
        Page<Textbook> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(pageRecords);

        return resultPage;
    }

    public Integer textbookAuthorityEditJudge(Long textBookId, Long userId) {
        if (textBookId == null || userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关用户信息有误");
        }
        Textbook textbook = textbookMapper.selectOne(new MyLambdaQueryWrapper<Textbook>().eq(Textbook::getId, textBookId));
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        }

        Teacher teacher = teacherMapper.selectOne(new MyLambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId));
        if (teacher == null || ObjectUtils.isEmpty(teacher.getId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教师信息有误");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textbook.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 1);
        List<TextbookAuthority> textbookAuthorities = textbookAuthorityMapper.selectList(queryWrapper);
        if (Objects.equals(textbook.getTextbookAuthorId(), teacher.getId())) {
            // 作者本人
            return 1;
        }
        if (Objects.equals(textbook.getCreator(), userId)) {
            return 1;
        }
        if (textbookAuthorities.isEmpty()) {
            return 2;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            if (Objects.equals(textbookAuthority.getUserId(), userId)) {
                return 1;
            }
        }

        return 0;
    }
}
