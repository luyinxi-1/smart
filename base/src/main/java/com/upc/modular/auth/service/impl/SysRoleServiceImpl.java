package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysRole;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.auth.service.ISysRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Override
    public void deleteSysRoleByIds(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void updateSysRoleById(SysRole sysRole) {
        if (sysRole == null || sysRole.getId() == null || sysRole.getId() == 0l) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.updateById(sysRole);
    }

    @Override
    public R<Page<SysRole>> getSysRolePage(SysRoleSearchParam param) {
        Page<SysRole> pageInfo = new Page<>(param.getCurrent(), param.getSize());
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(param.getRoleName()), SysRole::getRoleName, param.getRoleName());
        queryWrapper.eq(param.getStatus() != null, SysRole::getStatus, param.getStatus());

        Page<SysRole> page = this.page(pageInfo, queryWrapper);

        return R.ok(page);
    }
}
