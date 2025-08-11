package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.mapper.RoleAuthorityListMapper;
import com.upc.modular.auth.mapper.SysAuthorityMapper;
import com.upc.modular.auth.param.AuthParam;
import com.upc.modular.auth.param.GetAuthPageParam;
import com.upc.modular.auth.service.ISysAuthorityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    @Autowired
    private SysAuthorityMapper sysAuthorityMapper;

    @Autowired
    private RoleAuthorityListMapper roleAuthorityListMapper;

//    @Override
//    public void deleteSysAuthorityByIds(List<Long> ids) {
//        if (CollectionUtils.isEmpty(ids)) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
//        }
//        this.removeBatchByIds(ids);
//    }

//    @Override
//    public R<List<SysAuthorityTreeReturnParam>> getSysAuthorityPage(SysAuthoritySearchParam param) {
//        // 获取所有权限数据
//        List<SysAuthority> allAuthorities = this.list(
//                new LambdaQueryWrapper<SysAuthority>()
//                        .like(StringUtils.isNotBlank(param.getAccessUrl()), SysAuthority::getAccessUrl, param.getAccessUrl())
//                        .orderBy(true, Objects.equals(1, param.getIsAsc()), SysAuthority::getAddDatetime)
//        );
//
//        List<SysAuthorityTreeReturnParam> rootAuthorities = buildTree(allAuthorities);
//
//        // 返回根节点权限列表，已经构建了树形结构
//        return R.ok(rootAuthorities);
//    }

//    @Override
//    public List<SysAuthority> getSysAuthorityByModelId(Long sysAuthorityModelId) {
//        if (sysAuthorityModelId == null || sysAuthorityModelId == 0L) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
//        }
//        List<SysAuthority> sysAuthorityList = this.list(
//                new LambdaQueryWrapper<SysAuthority>().eq(SysAuthority::getSysAuthorityModelId, sysAuthorityModelId)
//        );
//
//        return sysAuthorityList;
//    }

    @Override
    public void addAuth(AuthParam authParam) {
        if (authParam == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ":请求参数不能为空");
        }
        if (authParam.getAuthModelId() == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":权限模块ID (authModelId)");
        }
        if (StringUtils.isBlank(authParam.getAuthModelName())) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":权限模块名称 (authModelName)");
        }
        if (StringUtils.isBlank(authParam.getAuthName())) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":权限名称 (authName)");
        }
        if (authParam.getSeq() == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":顺序 (seq)");
        }
        if (authParam.getStatus() == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":状态 (status)");
        }
        if (authParam.getAuthType() == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":权限类型 (authType)");
        }
        if (StringUtils.isBlank(authParam.getUrl())) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ":路由 (url)");
        }

//        List<SysAuthority> sysAuths = sysAuthorityMapper.selectList(
//                new MyLambdaQueryWrapper<SysAuthority>()
//                        .eq(SysAuthority::getUrl, authParam.getUrl())
//                        .eq(SysAuthority::getAuthModelId,authParam.getAuthModelId())
//        );
//        if (CollectionUtils.isNotEmpty(sysAuths)) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ":已存在url相同的权限点");
//        }

        // 检查权限点是否已存在。这里使用了 exists 方法，比 selectList 更高效，因为它只需要知道存不存在，而不需要返回具体数据
        boolean isExist = sysAuthorityMapper.exists(
                new MyLambdaQueryWrapper<SysAuthority>()
                        .eq(SysAuthority::getUrl, authParam.getUrl())
                        .eq(SysAuthority::getAuthModelId, authParam.getAuthModelId())
        );
        if (isExist) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "该模块下已存在相同URL的权限点");
        }
        SysAuthority sysAuth = new SysAuthority();
        BeanUtils.copyProperties(authParam, sysAuth);
        sysAuth.setId(null);
        // 程序能运行到这部说明sysAuths一定为空，所以下面的get(0)一定报错
//        sysAuth.setAuthModelName(sysAuths.get(0).getAuthModelName());
        sysAuthorityMapper.insert(sysAuth);
    }

    @Override
    public void deleteAuths(List<Integer> idList) {
        //先删除连表中的数据
        roleAuthorityListMapper.delete(new MyLambdaQueryWrapper<RoleAuthorityList>()
                .in(RoleAuthorityList::getAuthorityId, idList));
        //然后删除sys_auth中的数据
        sysAuthorityMapper.deleteBatchIds(idList);
    }

    @Override
    public PageBaseReturnParam<SysAuthority> getAuths(GetAuthPageParam getAuthPageParam) {
        Page<SysAuthority> authGetPage = new Page<>(getAuthPageParam.getCurrent(), getAuthPageParam.getSize());
        Page<SysAuthority> sysAuthPageReturn = sysAuthorityMapper.selectPage(authGetPage,
                new MyLambdaQueryWrapper<SysAuthority>()
                        // 对于 Long/Integer/Enum 等对象类型，判断是否为 null
                        .eq(getAuthPageParam.getAuthModelId() != null, SysAuthority::getAuthModelId, getAuthPageParam.getAuthModelId())
                        .eq(getAuthPageParam.getStatus() != null, SysAuthority::getStatus, getAuthPageParam.getStatus())
                        .eq(getAuthPageParam.getAuthType() != null, SysAuthority::getAuthType, getAuthPageParam.getAuthType())
                        // 对于 String 类型，使用 like 查询，判断是否 "isNotBlank" (非null、非空、非空白)
                        .like(StringUtils.isNotBlank(getAuthPageParam.getAuthModelName()), SysAuthority::getAuthModelName, getAuthPageParam.getAuthModelName())
                        .like(StringUtils.isNotBlank(getAuthPageParam.getAuthName()), SysAuthority::getAuthName, getAuthPageParam.getAuthName())
                        .eq(StringUtils.isNotBlank(getAuthPageParam.getUrl()), SysAuthority::getUrl, getAuthPageParam.getUrl())
                        .eq(StringUtils.isNotBlank(getAuthPageParam.getAccessUrl()), SysAuthority::getAccessUrl, getAuthPageParam.getAccessUrl())
                        .orderByAsc(SysAuthority::getSeq)
        );
//        PageBaseReturnParam<AuthParam> authParamPageBaseReturnParam = new PageBaseReturnParam<AuthParam>()
//                .setPageNo(sysAuthPageReturn.getCurrent())
//                .setTotal(sysAuthPageReturn.getTotal())
//                .setData(sysAuthPageReturn.getRecords().stream()
//                        .map(item -> {
//                            AuthParam authParam = new AuthParam();
//                            BeanUtils.copyProperties(item, authParam);
//                            return authParam;
//                        })
//                        .collect(Collectors.toList())
//                );
        PageBaseReturnParam<SysAuthority> p = PageBaseReturnParam.ok(sysAuthPageReturn);
        return p;
    }

    @Override
    public void updateByAuthId(AuthParam authParam) {
        Long authId = authParam.getId();
        if (authId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, ":更新时id不能为空");
        }

        SysAuthority existingAuth = sysAuthorityMapper.selectById(authId);
        if (existingAuth == null) {
            throw new BusinessException(BusinessErrorEnum.IS_EMPTY, ",要更新的权限记录不存在，ID: " + authId);
        }

        SysAuthority sysAuth = new SysAuthority();
        BeanUtils.copyProperties(authParam, sysAuth);
        sysAuthorityMapper.updateById(sysAuth);
    }


//    public List<SysAuthorityTreeReturnParam> buildTree(List<SysAuthority> allAuthorities) {
//        // 用来存储权限ID与对应树形节点的映射
//        Map<Long, SysAuthorityTreeReturnParam> authorityMap = new HashMap<>();
//        List<SysAuthorityTreeReturnParam> rootAuthorities = new ArrayList<>();
//
//        // 存储父节点ID与其子节点的映射
//        Map<Long, Long> parentMap = allAuthorities.stream()
//                .filter(item -> item.getId() != null)
//                .collect(Collectors.toMap(
//                        SysAuthority::getId,
//                        item -> item.getFatherId() == null ? 0L : item.getFatherId()
//                ));
//
//        // 0. 检测是否存在循环嵌套
//        for (SysAuthority authority : allAuthorities) {
//            if (hasCycle(authority.getId(), parentMap, new HashSet<>())) {
//                throw new BusinessException(BusinessErrorEnum.HAS_CYCLE_ERR);
//            }
//        }
//
//        // 1. 先将所有权限节点放入 map 中
//        for (SysAuthority authority : allAuthorities) {
//            SysAuthorityTreeReturnParam treeAuthority = new SysAuthorityTreeReturnParam();
//            BeanUtils.copyProperties(authority, treeAuthority);
//            treeAuthority.setSysAuthorityList(new ArrayList<>()); // 初始化子权限列表
//            authorityMap.put(authority.getId(), treeAuthority);
//        }
//
//        // 2. 构建树形结构
//        for (SysAuthority authority : allAuthorities) {
//            SysAuthorityTreeReturnParam treeAuthority = authorityMap.get(authority.getId());
//            Long parentId = authority.getFatherId();
//
//            if (parentId == null || parentId == 0) {
//                // 如果没有父节点，则为根节点
//                rootAuthorities.add(treeAuthority);
//            } else {
//                SysAuthorityTreeReturnParam parent = authorityMap.get(parentId);
//                if (parent != null) {
//                    parent.getSysAuthorityList().add(treeAuthority); // 添加为父节点的子节点
//                }
//            }
//        }
//
//        return rootAuthorities;
//    }

    // 检测是否存在循环依赖
//    private boolean hasCycle(Long nodeId, Map<Long, Long> parentMap, Set<Long> visited) {
//        Set<Long> path = new HashSet<>();
//        while (nodeId != null && nodeId != 0) {
//            if (path.contains(nodeId)) {
//                return true; // 检测到环
//            }
//            path.add(nodeId);
//            nodeId = parentMap.get(nodeId);
//        }
//        return false;
//    }


//    public void saveSysAuthority(String accessUrl, String accessName, Long fatherId) {
//        SysAuthority sysAuthority = new SysAuthority();
//        sysAuthority.setAccessUrl(accessUrl);
//        sysAuthority.setAccessName(accessName);
//        sysAuthority.setFatherId(fatherId);
//
//        this.save(sysAuthority);
//    }
//
//    public Long findSysAuthorityIdByUrl(String accessUrl) {
//        QueryWrapper<SysAuthority> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("access_url", accessUrl);
//
//        List<SysAuthority> list = this.list(queryWrapper);
//        if (list.isEmpty()) {
//            return null;
//        }
//
//        return list.get(0).getId();
//    }

}

