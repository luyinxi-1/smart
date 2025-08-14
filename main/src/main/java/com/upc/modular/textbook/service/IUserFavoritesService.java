package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.UserFavorites;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.UserFavoritesPageSearch;
import com.upc.modular.textbook.param.UserFavoritesVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fwx
 * @since 2025-08-14
 */
public interface IUserFavoritesService extends IService<UserFavorites> {

    void insertUserFavorites(Long textbookId);

    void deleteUserFavorites(IdParam idParam);

    Page<UserFavoritesVO> getPage(UserFavoritesPageSearch param);
}
