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
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.mapper.TeacherMapper;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.entity.UserFavorites;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.upc.modular.textbook.service.ITextbookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
public class TextbookServiceImpl extends ServiceImpl<TextbookMapper, Textbook> implements ITextbookService {

    @Autowired
    private TextbookMapper textbookMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private TextbookClassificationServiceImpl textbookClassificationService;
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
        Page<TextbookPageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        if (ObjectUtils.isEmpty(param.getClassificationId())) {
            return textbookMapper.selectTextbookPage(page, param, Collections.emptyList());
        }
        List<Long> classificationIds = textbookClassificationService.selectTextbookClassificationSubtreeIdList(param.getClassificationId());
        return textbookMapper.selectTextbookPage(page, param, classificationIds);

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
        return textbookMapper.getOneTextbookDetails(textbookId);
    }

    @Override
    public Page<Textbook> getpageTextbookCenter(UserFavoritesPageSearch param) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }

        Long currentUserId = userInfoToRedis.getId();

        Page<Textbook> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<Textbook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(wq -> wq.eq(Textbook::getTextbookAuthorId, currentUserId)
                .or()
                .inSql(Textbook::getId, "SELECT textbook_id FROM textbook_authority WHERE user_id = " + currentUserId + " AND authority_type = 1"));
        if (param.getClassification() != null) {
            queryWrapper.eq(Textbook::getClassification, param.getClassification());
        }
        if (StringUtils.isNotBlank(param.getTextbookName())) {
            queryWrapper.like(Textbook::getTextbookName, param.getTextbookName().trim());
        }

        if (param.getIsAsc() != null && param.getIsAsc() == 1) {
            queryWrapper.orderByAsc(Textbook::getAddDatetime);
        } else {
            // Default to descending order by creation date.
            queryWrapper.orderByDesc(Textbook::getAddDatetime);
        }
        return this.page(page, queryWrapper);

    }
}
