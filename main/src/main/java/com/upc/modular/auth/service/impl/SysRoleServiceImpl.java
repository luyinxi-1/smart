package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.auth.service.IRoleAuthorityListService;
import com.upc.modular.auth.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysTbrole> implements ISysRoleService {

    @Autowired
    private IRoleAuthorityListService roleAuthorityListService;

    @Override
    public void deleteSysRoleByIds(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, " ID列表不能为空");
        }
        for (Long id : ids) {
            List<RoleAuthorityList> list = roleAuthorityListService.list(new LambdaQueryWrapper<RoleAuthorityList>().eq(RoleAuthorityList::getRoleId, id));
            if (!list.isEmpty()) {
                throw new BusinessException(BusinessErrorEnum.BINDING_ERR, " 当前角色已绑定权限信息，请先删除绑定关系！");
            }
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void updateSysRoleById(SysTbrole sysTbrole) {
        if (sysTbrole == null || sysTbrole.getId() == null || sysTbrole.getId() == 0l) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.updateById(sysTbrole);
    }

    @Override
    public R<Page<SysTbrole>> getSysRolePage(SysRoleSearchParam param) {
        Page<SysTbrole> pageInfo = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<SysTbrole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(!StringUtils.isEmpty(param.getRoleName()), SysTbrole::getRoleName, param.getRoleName());
        queryWrapper.eq(param.getStatus() != null, SysTbrole::getStatus, param.getStatus());
        queryWrapper.orderBy(true, Objects.equals(1, param.getIsAsc()), SysTbrole::getAddDatetime);

        Page<SysTbrole> page = this.page(pageInfo, queryWrapper);

        return R.ok(page);
    }
}
