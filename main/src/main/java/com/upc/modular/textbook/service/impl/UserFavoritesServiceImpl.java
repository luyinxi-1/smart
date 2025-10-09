package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.UserFavorites;
import com.upc.modular.textbook.mapper.TextbookMapper;
import com.upc.modular.textbook.mapper.UserFavoritesMapper;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.param.UserFavoritesVO;
import com.upc.modular.textbook.service.ITextbookService;
import com.upc.modular.textbook.service.IUserFavoritesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fwx
 * @since 2025-08-14
 */
@Service
public class UserFavoritesServiceImpl extends ServiceImpl<UserFavoritesMapper, UserFavorites> implements IUserFavoritesService {
    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private UserFavoritesMapper userFavoritesMapper;
    @Override
    public void insertUserFavorites(Long textbookId) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        if (ObjectUtils.isEmpty(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        Textbook textbook = textbookService.getById(textbookId);
        if (ObjectUtils.isEmpty(textbook)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "未找到ID为 " + textbookId + " 的教材");
        }
        UserFavorites userFavorites =new UserFavorites();

        userFavorites.setUserId(userInfoToRedis.getId())
                .setTextbookId(textbook.getId())
                .setClassification(textbook.getClassification())
                .setTextbookName(textbook.getTextbookName());
        boolean saveResult = this.save(userFavorites);
        if (!saveResult) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "收藏失败");
        }

    }

    @Override
    public void deleteUserFavorites(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        userFavoritesMapper.deleteBatchIds(idParam.getIdList());
    }

    @Override
    public void deleteUserFavoritesByTextbookId(Long textbookId) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        if (ObjectUtils.isEmpty(textbookId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "教材ID不能为空");
        }

        // 根据用户ID和教材ID删除收藏记录
        LambdaQueryWrapper<UserFavorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavorites::getUserId, userInfoToRedis.getId())
                .eq(UserFavorites::getTextbookId, textbookId);

        boolean deleteResult = this.remove(queryWrapper);
        if (!deleteResult) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "删除收藏失败，可能该教材未被收藏");
        }
    }

    @Override
    public Page<UserFavoritesVO> getPage(UserFavoritesPageSearch param) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }

        Page<UserFavorites> page = new Page<>(param.getCurrent(), param.getSize());

        LambdaQueryWrapper<UserFavorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavorites::getUserId, userInfoToRedis.getId());

        if (param.getClassification() != null) {
            queryWrapper.eq(UserFavorites::getClassification, param.getClassification());
        }
        if (param.getTextbookName() != null && !param.getTextbookName().trim().isEmpty()) {
            queryWrapper.like(UserFavorites::getTextbookName, param.getTextbookName().trim());
        }

        if (param.getIsAsc() != null && param.getIsAsc() == 1) {
            queryWrapper.orderByAsc(UserFavorites::getAddDatetime);
        } else {
            queryWrapper.orderByDesc(UserFavorites::getAddDatetime);
        }
        Page<UserFavorites> favoritesPage = this.page(page, queryWrapper);

        List<Long> textbookIds = favoritesPage.getRecords().stream()
                .map(UserFavorites::getTextbookId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Textbook> textbookMap = textbookIds.isEmpty()
                ? Collections.emptyMap()
                : textbookService.listByIds(textbookIds).stream()
                .collect(Collectors.toMap(Textbook::getId, t -> t));

        List<UserFavoritesVO> voList = favoritesPage.getRecords().stream().map(fav -> {
            UserFavoritesVO vo = new UserFavoritesVO();
            vo.setId(fav.getId());
            vo.setUserId(fav.getUserId());
            vo.setTextbookId(fav.getTextbookId());
            vo.setTextbookName(fav.getTextbookName());
            vo.setClassification(fav.getClassification());
            vo.setAddDatetime(fav.getAddDatetime());
            vo.setTextbook(textbookMap.get(fav.getTextbookId()));
            return vo;
        }).collect(Collectors.toList());

        // 6. 构造返回分页对象
        Page<UserFavoritesVO> voPage = new Page<>();
        voPage.setCurrent(favoritesPage.getCurrent());
        voPage.setSize(favoritesPage.getSize());
        voPage.setTotal(favoritesPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Boolean isFavoriteTextbook(Long textBookId) {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        if (ObjectUtils.isEmpty(userInfoToRedis)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "用户未登录");
        }
        if (textBookId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        LambdaQueryWrapper<UserFavorites> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFavorites::getUserId, userInfoToRedis.getId());
        queryWrapper.eq(UserFavorites::getTextbookId, textBookId);
        long count = this.count(queryWrapper);
        return count > 0;
    }
}
