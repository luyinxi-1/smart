package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.RoleAuthorityList;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.param.RoleAuthorityAssociationSearchParam;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.service.IRoleAuthorityListService;
import com.upc.modular.auth.service.ISysAuthorityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/role-authority-list")
@Api(tags = "角色权限关联")
public class RoleAuthorityListController {

    @Autowired
    private IRoleAuthorityListService roleAuthorityListService;

    @ApiOperation(value = "删除角色权限关联关系")
    @PostMapping("/deleteRoleAuthorityAssociation")
    public R deleteRoleAuthorityAssociation(@RequestBody List<Long> ids) {
        roleAuthorityListService.deleteRoleAuthorityAssociation(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增角色权限关联关系")
    @PostMapping("/insertRoleAuthorityAssociation")
    public R insertRoleAuthorityAssociation(@RequestBody List<RoleAuthorityList> roleAuthorityAssociations) {
        if (roleAuthorityAssociations.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        roleAuthorityListService.saveBatch(roleAuthorityAssociations);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "条件分页查询权限数据")
    @PostMapping("/getRoleAuthorityAssociationPage")
    public R<Page<RoleAuthorityList>> getRoleAuthorityAssociationPage(@RequestBody RoleAuthorityAssociationSearchParam param) {
        return roleAuthorityListService.getRoleAuthorityAssociationPage(param);
    }


}
