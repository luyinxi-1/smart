package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.institution.service.impl.InstitutionServiceImpl;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teachingactivities.param.DiscussionTopicSecondReplyPageReturnParam;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookAuthority;
import com.upc.modular.textbook.mapper.TextbookAuthorityMapper;
import com.upc.modular.textbook.param.TextbookAuthorityReturnParam;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
public class TextbookAuthorityServiceImpl extends ServiceImpl<TextbookAuthorityMapper, TextbookAuthority> implements ITextbookAuthorityService {

    @Override
    public void deleteTextbookAuthorityByIds(List<Long> ids) {

        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void insertTextbookAuthority(TextbookAuthority textbookAuthority) {

        if (textbookAuthority == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (textbookAuthority.getTextbookId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材主表不能为空");
        }
        if (textbookAuthority.getAuthorityType() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择权限类型");
        }
        if (textbookAuthority.getAuthorityType() != 1 && textbookAuthority.getAuthorityType() != 2) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择权限类型(1表示协作者，2表示可见机构)");
        }
        if (textbookAuthority.getAuthorityType() == 1 && textbookAuthority.getUserId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "协作者不能为空");
        }
        if (textbookAuthority.getAuthorityType() == 2 && textbookAuthority.getVisibleInstituteId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "机构不能为空");
        }

        this.save(textbookAuthority);
    }

//    @Override
//    public void updateTextbookAuthorityById(TextbookAuthority textbookAuthority) {
//        if (textbookAuthority == null) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
//        }
//
//        this.updateById(textbookAuthority);
//    }

    @Autowired
    private ITeacherService teacherService;
    @Autowired
    private IInstitutionService iInstitutionService;

    @Override
    public Page<TextbookAuthorityReturnParam> getTextbookAuthorityPage(TextbookAuthoritySearchParam param) {
        if (param.getAuthorityType() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择查询的权限类型");
        }
        if (param.getTextbookId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择查询的教材");
        }
        if (param.getAuthorityType() != 1 && param.getAuthorityType() != 2) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择权限类型(1表示协作者，2表示可见机构)");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getAuthorityType, param.getAuthorityType());
        queryWrapper.eq(TextbookAuthority::getTextbookId, param.getTextbookId());
        List<TextbookAuthority> textbookAuthorityList = this.list(queryWrapper);

        if (textbookAuthorityList.isEmpty()) {
            return new Page<>();
        }

        List<TextbookAuthorityReturnParam> resultList = new ArrayList<>();
        if (param.getAuthorityType() == 1) {
            for (TextbookAuthority textbookAuthority : textbookAuthorityList) {
                TextbookAuthorityReturnParam returnParam = new TextbookAuthorityReturnParam();
                BeanUtils.copyProperties(textbookAuthority, returnParam);
                if (textbookAuthority == null || textbookAuthority.getUserId() == null) {
                    continue;
                }
                Teacher teacher = teacherService.getOne(new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, textbookAuthority.getUserId()));
                returnParam.setTeacher(teacher);
                resultList.add(returnParam);
            }
        } else if (param.getAuthorityType() == 2) {
            for (TextbookAuthority textbookAuthority : textbookAuthorityList) {
                TextbookAuthorityReturnParam returnParam = new TextbookAuthorityReturnParam();
                BeanUtils.copyProperties(textbookAuthority, returnParam);
                resultList.add(returnParam);
            }
        }

        // 字符串模糊匹配功能：
        String filterName = param.getTeacherNameOrInstituteName();
        if (StringUtils.isNotBlank(filterName)) {
            if (param.getAuthorityType() == 1) {
                resultList = resultList.stream().filter(returnParam -> {
                    if (returnParam == null || returnParam.getTeacher() == null || StringUtils.isBlank(returnParam.getTeacher().getName())) {
                        return false;
                    }
                    String teacherName = returnParam.getTeacher().getName();
                    return teacherName.contains(filterName);
                }).collect(Collectors.toList());

            } else if (param.getAuthorityType() == 2) {
                resultList = resultList.stream().filter(returnParam -> {
                    if (returnParam == null || returnParam.getVisibleInstituteId() == null) {
                        return false;
                    }
                    Institution institution = iInstitutionService.getById(returnParam.getVisibleInstituteId());
                    if (institution == null || StringUtils.isBlank(institution.getInstitutionName())) {
                        return false;
                    }
                    String institutionName = institution.getInstitutionName();
                    return institutionName.contains(filterName);
                }).collect(Collectors.toList());
            }
        }

        // 手动分页
        long current = Math.max(1, param.getCurrent());
        long size    = Math.max(1, param.getSize());
        int from = (int) ((current - 1) * size);
        if (from >= resultList.size()) {
            return new Page<>(current, size);   // 越界空页
        }
        int to = Math.min(from + (int) size, resultList.size());
        List<TextbookAuthorityReturnParam> pageRecords = resultList.subList(from, to);

        // 组装 Page
        Page<TextbookAuthorityReturnParam> resultPage = new Page<>();
        resultPage.setCurrent(current);
        resultPage.setSize(size);
        resultPage.setTotal(resultList.size());
        resultPage.setRecords(pageRecords);
        return resultPage;
    }

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private InstitutionServiceImpl institutionService;

    /**
     * 判断指定用户是否有权限访问指定教材
     * @param textBookId
     * @param userId
     * @return
     */
    @Override
    public boolean textbookAuthorityJudge(Long textBookId, Long userId) {
        if (textBookId == null || userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关用户信息有误");
        }
        Long userInstitutionId = tbuser.getInstitutionId();

        Textbook textbook = textbookService.getById(textBookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textbook.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 2);
        List<TextbookAuthority> textbookAuthorities = this.list(queryWrapper);
        if (textbookAuthorities.isEmpty()) {
            return false;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            Long visibleInstituteId = textbookAuthority.getVisibleInstituteId();
            boolean result = institutionService.judgeInclusion(userInstitutionId, visibleInstituteId);

            if (result) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean textbookAuthorityEditJudge(Long textBookId, Long userId) {
        if (textBookId == null || userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbuser tbuser = sysUserService.getById(userId);
        if (tbuser == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关用户信息有误");
        }


        Textbook textbook = textbookService.getById(textBookId);
        if (textbook == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "相关教材信息有误");
        }

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getTextbookId, textbook.getId());
        queryWrapper.eq(TextbookAuthority::getAuthorityType, 1);
        List<TextbookAuthority> textbookAuthorities = this.list(queryWrapper);
        if (textbook.getTextbookAuthorId() == userId) {
            // 作者本人
            return true;
        }
        if (textbook.getCreator() == userId) {
            return true;
        }
        if (textbookAuthorities.isEmpty()) {
            return false;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            if (textbookAuthority.getUserId() == userId) {
                return true;
            }
        }

        return false;
    }


}
