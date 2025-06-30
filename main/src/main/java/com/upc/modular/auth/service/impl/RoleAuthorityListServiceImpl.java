package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.entity.SysAuthority;
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
        if(CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public R<Page<RoleAuthorityList>> getRoleAuthorityAssociationPage(RoleAuthorityAssociationSearchParam param) {
        Page<RoleAuthorityList> pageInfo = new Page(param.getCurrent(), param.getSize());
        Page<RoleAuthorityList> page = this.page(pageInfo,
                new LambdaQueryWrapper<RoleAuthorityList>().eq(param.getRoleId() != null && param.getRoleId() != 0L, RoleAuthorityList::getRoleId, param.getRoleId())
        );

        return R.ok(page);
    }
}
