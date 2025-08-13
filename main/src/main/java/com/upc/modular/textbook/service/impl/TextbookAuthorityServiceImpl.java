package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.institution.service.impl.InstitutionServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookAuthority;
import com.upc.modular.textbook.mapper.TextbookAuthorityMapper;
import com.upc.modular.textbook.param.TextbookAuthoritySearchParam;
import com.upc.modular.textbook.service.ITextbookAuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public Page<TextbookAuthority> getTextbookAuthorityPage(TextbookAuthoritySearchParam param) {
        if (param.getAuthorityType() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择查询的权限类型");
        }
        if (param.getTextbookId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择查询的教材");
        }
        if (param.getAuthorityType() != 1 && param.getAuthorityType() != 2) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "必须要选择权限类型(1表示协作者，2表示可见机构)");
        }

        Page<TextbookAuthority> pageInfo = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<TextbookAuthority> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookAuthority::getAuthorityType, param.getAuthorityType());
        queryWrapper.eq(TextbookAuthority::getTextbookId, param.getTextbookId());

        Page<TextbookAuthority> page = this.page(pageInfo, queryWrapper);

        return page;
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
        if (textbookAuthorities.isEmpty()) {
            return false;
        }
        if (textbook.getTextbookAuthorId() == userId) {
            // 作者本人
            return true;
        }
        for (TextbookAuthority textbookAuthority : textbookAuthorities) {
            if (textbookAuthority.getUserId() == userId) {
                return true;
            }
        }

        return false;
    }


}
