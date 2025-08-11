package com.upc.modular.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.auth.param.tree.AuthNode;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface ISysRoleService extends IService<SysTbrole> {

    void deleteSysRoleByIds(List<Long> ids);

    void updateSysRoleById(SysTbrole sysTbrole);

    R<Page<SysTbrole>> getSysRolePage(SysRoleSearchParam param);

    /**
     * 获取角色的功能权限
     */
    List<AuthNode> getRoleAuths(Long roleId);

    /**
     * 更新角色权限树
     */
    void updateRoleAuthTree(Long roleId, List<Long> idList);
}
