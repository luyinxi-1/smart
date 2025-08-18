package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.auth.param.UserRoleListPageSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface IUserRoleListService extends IService<UserRoleList> {

    Boolean insert(UserRoleList userRoleList);

    Boolean batchDelete(IdParam idParam);

    Boolean updateUserRoleList(UserRoleList userRoleList);

    Page<UserRoleListPageReturnParam> getPage(UserRoleListPageSearchParam param);

    List<Long> getUserRoleList(Long userId);
}
