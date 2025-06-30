package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.mapper.RoleAuthorityListMapper;
import com.upc.modular.auth.param.RoleAuthorityAssociationSearchParam;
import com.upc.modular.auth.service.IRoleAuthorityListService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class RoleAuthorityListServiceImpl extends ServiceImpl<RoleAuthorityListMapper, RoleAuthorityList> implements IRoleAuthorityListService {

    @Override
    public void deleteRoleAuthorityAssociation(List<Long> ids) {

    }

    @Override
    public R<Page<RoleAuthorityList>> getRoleAuthorityAssociationPage(RoleAuthorityAssociationSearchParam param) {
        return null;
    }
}
