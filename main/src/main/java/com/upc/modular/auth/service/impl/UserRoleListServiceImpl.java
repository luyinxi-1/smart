package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.UserRoleListMapper;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.auth.param.UserRoleListPageSearchParam;
import com.upc.modular.auth.service.IUserRoleListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
public class UserRoleListServiceImpl extends ServiceImpl<UserRoleListMapper, UserRoleList> implements IUserRoleListService {
    @Autowired
    private UserRoleListMapper userRoleListMapper;
    @Override
    public Boolean insert(UserRoleList userRoleList) {
        if (ObjectUtils.isEmpty(userRoleList) || ObjectUtils.isEmpty(userRoleList.getUserId()) || ObjectUtils.isEmpty(userRoleList.getRoleId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.save(userRoleList);
    }

    @Override
    public Boolean batchDelete(IdParam idParam) {
        if (ObjectUtils.isEmpty(idParam) || ObjectUtils.isEmpty(idParam.getIdList())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "id列表不能为空");
        }
        List<Long> idList = idParam.getIdList();
        return this.removeBatchByIds(idList);
    }

    @Override
    public Boolean updateUserRoleList(UserRoleList userRoleList) {
        if (ObjectUtils.isEmpty(userRoleList)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        return this.updateById(userRoleList);
    }

    @Override
    public Page<UserRoleListPageReturnParam> getPage(UserRoleListPageSearchParam param) {
        Page<UserRoleListPageReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        return userRoleListMapper.getPage(page, param);
    }

    @Override
    public List<Long> getUserRoleList(Long userId) {
        if (userId == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }

        LambdaQueryWrapper<UserRoleList> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRoleList::getUserId, userId);
        List<UserRoleList> userRoleLists = this.list(queryWrapper);

        if (userRoleLists.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> RoleList = userRoleLists.stream().map(UserRoleList::getRoleId).collect(Collectors.toList());


        return RoleList;
    }
}
