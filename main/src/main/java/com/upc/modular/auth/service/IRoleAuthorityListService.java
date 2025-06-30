package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.param.RoleAuthorityAssociationSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface IRoleAuthorityListService extends IService<RoleAuthorityList> {

    void deleteRoleAuthorityAssociation(List<Long> ids);

    R<Page<RoleAuthorityList>> getRoleAuthorityAssociationPage(RoleAuthorityAssociationSearchParam param);
}
