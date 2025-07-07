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
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.param.SysAuthorityTreeReturnParam;
import com.upc.modular.auth.service.ISysAuthorityService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
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
public class SysAuthorityServiceImpl extends ServiceImpl<SysAuthorityMapper, SysAuthority> implements ISysAuthorityService {

    @Override
    public void deleteSysAuthorityByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public R<List<SysAuthorityTreeReturnParam>> getSysAuthorityPage(SysAuthoritySearchParam param) {
        // 获取所有权限数据
        List<SysAuthority> allAuthorities = this.list(
                new LambdaQueryWrapper<SysAuthority>()
                        .like(StringUtils.isNotBlank(param.getAccessUrl()), SysAuthority::getAccessUrl, param.getAccessUrl())
                        .orderBy(true, Objects.equals(1, param.getIsAsc()), SysAuthority::getAddDatetime)
        );

        List<SysAuthorityTreeReturnParam> rootAuthorities = buildTree(allAuthorities);

        // 返回根节点权限列表，已经构建了树形结构
        return R.ok(rootAuthorities);
    }

    public List<SysAuthorityTreeReturnParam> buildTree(List<SysAuthority> allAuthorities) {
        // 用来存储权限ID与对应树形节点的映射
        Map<Long, SysAuthorityTreeReturnParam> authorityMap = new HashMap<>();
        List<SysAuthorityTreeReturnParam> rootAuthorities = new ArrayList<>();

        // 存储父节点ID与其子节点的映射
        Map<Long, Long> parentMap = allAuthorities.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        SysAuthority::getId,
                        item -> item.getFatherId() == null ? 0L : item.getFatherId()
                ));

        // 0. 检测是否存在循环嵌套
        for (SysAuthority authority : allAuthorities) {
            if (hasCycle(authority.getId(), parentMap, new HashSet<>())) {
                throw new BusinessException(BusinessErrorEnum.HAS_CYCLE_ERR);
            }
        }

        // 1. 先将所有权限节点放入 map 中
        for (SysAuthority authority : allAuthorities) {
            SysAuthorityTreeReturnParam treeAuthority = new SysAuthorityTreeReturnParam();
            BeanUtils.copyProperties(authority, treeAuthority);
            treeAuthority.setSysAuthorityList(new ArrayList<>()); // 初始化子权限列表
            authorityMap.put(authority.getId(), treeAuthority);
        }

        // 2. 构建树形结构
        for (SysAuthority authority : allAuthorities) {
            SysAuthorityTreeReturnParam treeAuthority = authorityMap.get(authority.getId());
            Long parentId = authority.getFatherId();

            if (parentId == null || parentId == 0) {
                // 如果没有父节点，则为根节点
                rootAuthorities.add(treeAuthority);
            } else {
                SysAuthorityTreeReturnParam parent = authorityMap.get(parentId);
                if (parent != null) {
                    parent.getSysAuthorityList().add(treeAuthority); // 添加为父节点的子节点
                }
            }
        }

        return rootAuthorities;
    }

    // 检测是否存在循环依赖
    private boolean hasCycle(Long nodeId, Map<Long, Long> parentMap, Set<Long> visited) {
        Set<Long> path = new HashSet<>();
        while (nodeId != null && nodeId != 0) {
            if (path.contains(nodeId)) {
                return true; // 检测到环
            }
            path.add(nodeId);
            nodeId = parentMap.get(nodeId);
        }
        return false;
    }

}

