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
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.mapper.RoleAuthorityListMapper;
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.mapper.SysAuthorityModelMapper;
import com.upc.modular.auth.mapper.SysRoleMapper;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.auth.param.tree.AuthNode;
import com.upc.modular.auth.service.IRoleAuthorityListService;
import com.upc.modular.auth.service.ISysRoleService;
import com.upc.modular.auth.utils.MyBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
    @Autowired
    private RoleAuthorityListMapper roleAuthorityListMapper;
    @Autowired
    private SysAuthorityModelMapper sysAuthorityModelMapper;
    @Autowired
    private SysAuthorityMapper sysAuthorityMapper;

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


    @Override
    public List<AuthNode> getRoleAuthTree(Long roleId) { // 传入的roleId为将被分配权限的角色ID

        /*
         * 1. 查询出传入的 roleId 所拥有的权限ID列表，用于后续标记“已拥有”状态。
         * 2. 查询出所有的权限模块（AuthModel），作为权限树的骨架（目录）。
         * 3. 查询出所有的具体权限（Auth），作为权限树的叶子节点（可勾选的操作）。
         * 4. 将权限模块和具体权限组装成一个完整的树形结构。
         * 5. 在组装过程中，根据步骤1的结果，正确设置每个权限节点的 isHad 属性。
         */

        // 1. 查出被操作角色（roleId）当前拥有的所有权限ID
        List<Long> authIdList = roleAuthorityListMapper.selectList(
                new LambdaQueryWrapper<RoleAuthorityList>()
                        .eq(RoleAuthorityList::getRoleId, roleId)
        ).stream().map(RoleAuthorityList::getAuthorityId).collect(Collectors.toList());

        // 2. 查出所有的权限模块，并按序号排序
        List<SysAuthorityModel> sysAuthModels = sysAuthorityModelMapper.selectList(
                new LambdaQueryWrapper<SysAuthorityModel>()
                        .orderBy(true, true, SysAuthorityModel::getSeq)
        );

        // 3. 查出所有的具体权限，并按序号排序
        List<SysAuthority> sysAuths = sysAuthorityMapper.selectList(
                new LambdaQueryWrapper<SysAuthority>()
                        .orderBy(true, true, SysAuthority::getSeq)
        );

        // 4. 开始构建权限树，首先初始化顶层节点
        List<AuthNode> authNodes = new ArrayList<>();
        for (SysAuthorityModel item : sysAuthModels) {
            // parentId 为 0 的是顶层模块
            if (Objects.equals(item.getParentId(), 0L)) {
                authNodes.add(
                        MyBeanUtils.copy(item, new AuthNode())
                                .setAuthName(item.getAuthModelName())
                                .setType(0) // 0 代表模块
                );
            }
        }

        // 5. 将所有具体权限（SysAuth）按其父模块ID（authModelId）进行分组
        ConcurrentMap<Long, List<AuthNode>> authMap = sysAuths.stream()
                .map(sysAuth ->
                        // 将 SysAuth 转换为 AuthNode，并设置其属性
                        MyBeanUtils.copy(sysAuth, new AuthNode())
                                .setType(sysAuth.getAuthType())
                                // 核心：如果角色已拥有该权限，则isHad为true，前端checkbox将显示为选中状态
                                .setIsHad(authIdList.contains(sysAuth.getId()))
                                .setParentId(sysAuth.getAuthModelId())) // 关联到父模块
                .collect(Collectors.groupingByConcurrent(AuthNode::getParentId));

        // 6. 递归地为每个模块节点填充其子模块和子权限
        authNodes.forEach(
                item -> item.getModelChildren(sysAuthModels, authMap)
        );

        return authNodes;
    }


    @Override
    public void updateRoleAuthTree(Long roleId, List<Long> idList) {
        // 查出数据库中对应的权限列表
        List<Long> authIdList = roleAuthorityListMapper.selectList(
                new LambdaQueryWrapper<RoleAuthorityList>()
                        .select(RoleAuthorityList::getAuthorityId)
                        .eq(RoleAuthorityList::getRoleId, roleId)
        ).stream().map(RoleAuthorityList::getAuthorityId).collect(Collectors.toList());
        // 找出新增的
        List<RoleAuthorityList> insertList = new ArrayList<>();
        idList.forEach(item->{
            if (!authIdList.contains(item)) {
                insertList.add(new RoleAuthorityList()
                        .setRoleId(roleId)
                        .setAuthorityId(item)
                );
            }
        });
        //找出删除的
        List<RoleAuthorityList> deleteList = new ArrayList<>();
        authIdList.forEach(item -> {
            if (!idList.contains(item)) {
                deleteList.add(new RoleAuthorityList()
                        .setRoleId(roleId)
                        .setAuthorityId(item)
                );
            }
        });
        roleAuthorityListService.saveBatch(insertList);
        //如果是空的就不删除
        if (org.springframework.util.CollectionUtils.isEmpty(deleteList)) {
            return;
        }
        roleAuthorityListMapper.myDeleteBatch(deleteList);
    }
}
