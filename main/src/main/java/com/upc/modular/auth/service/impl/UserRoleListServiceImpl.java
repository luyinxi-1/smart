package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.mapper.UserRoleListMapper;
import com.upc.modular.auth.param.UserRoleListInsertParam;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.auth.param.UserRoleListPageSearchParam;
import com.upc.modular.auth.service.ISysRoleService;
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

    @Autowired
    private ISysRoleService sysRoleService;
    @Override
    public Boolean insert(UserRoleListInsertParam param) {
        if (ObjectUtils.isEmpty(param) || ObjectUtils.isEmpty(param.getUserId()) || ObjectUtils.isEmpty(param.getRoleId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        this.removeById(param.getUserId());
        for (Long id : param.getRoleId()) {
            UserRoleList userRoleList = new UserRoleList();
            userRoleList.setRoleId(id);
            userRoleList.setUserId(param.getUserId());
            this.save(userRoleList);
        }
        return true;
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
    public Boolean updateUserRoleList(UserRoleListInsertParam userRoleList) {
        if (ObjectUtils.isEmpty(userRoleList) || ObjectUtils.isEmpty(userRoleList.getUserId())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "传参为空");
        }
        this.remove(new MyLambdaQueryWrapper<UserRoleList>().eq(UserRoleList::getUserId, userRoleList.getUserId()));
        for (Long id : userRoleList.getRoleId()) {
            UserRoleList param = new UserRoleList();
            param.setRoleId(id);
            param.setUserId(userRoleList.getUserId());
            this.save(param);
        }
        return true;
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
    @Override
    public void insertDefaultRole(Long id, Integer userType) {
        UserRoleList userRoleList = new UserRoleList();
        if (userType == 0) {
            List<SysTbrole> student = sysRoleService.list(new MyLambdaQueryWrapper<SysTbrole>().eq(SysTbrole::getStatus, 1).eq(SysTbrole::getRoleCode, "admin"));
            userRoleList.setRoleId(student.get(0).getId());
            userRoleList.setUserId(id);
        }
        if (userType == 1) {
            List<SysTbrole> student = sysRoleService.list(new MyLambdaQueryWrapper<SysTbrole>().eq(SysTbrole::getStatus, 1).eq(SysTbrole::getRoleCode, "student"));
            userRoleList.setRoleId(student.get(0).getId());
            userRoleList.setUserId(id);
        }
        if (userType == 2) {
            List<SysTbrole> student = sysRoleService.list(new MyLambdaQueryWrapper<SysTbrole>().eq(SysTbrole::getStatus, 1).eq(SysTbrole::getRoleCode, "teacher"));
            userRoleList.setRoleId(student.get(0).getId());
            userRoleList.setUserId(id);
        }
        this.save(userRoleList);
    }
}
