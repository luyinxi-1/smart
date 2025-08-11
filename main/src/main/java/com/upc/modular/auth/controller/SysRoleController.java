package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.auth.param.tree.AuthNode;
import com.upc.modular.auth.service.ISysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/sys-role")
@Api(tags = "角色管理")
public class SysRoleController {

    @Autowired
    private ISysRoleService sysRoleService;

    @ApiOperation(value = "删除角色")
    @PostMapping("/deleteSysRoleByIds")
    public R deleteSysRoleByIds(@RequestBody List<Long> ids) {
        sysRoleService.deleteSysRoleByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增角色")
    @PostMapping("/insertSysRole")
    public R insertSysRole(@RequestBody SysTbrole sysTbrole) {
        if (sysTbrole == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        sysTbrole.setStatus(1);
        sysRoleService.save(sysTbrole);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "查询角色信息")
    @PostMapping("/getSysRoleById")
    public R getSysRoleById(@RequestParam("sysRoleId") Long sysRoleId) {
        if (sysRoleId == null || sysRoleId == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysTbrole sysTbrole = sysRoleService.getById(sysRoleId);
        return R.commonReturn(200, "查询成功", sysTbrole);
    }

    @ApiOperation(value = "修改角色信息")
    @PostMapping("/updateSysRoleById")
    public R updateSysRoleById(@RequestBody SysTbrole sysTbrole) {
        sysRoleService.updateSysRoleById(sysTbrole);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页按条件查询角色信息")
    @PostMapping("/getSysRolePage")
    public R<Page<SysTbrole>> getSysRolePage(@RequestBody SysRoleSearchParam param) {
        return sysRoleService.getSysRolePage(param);
    }

    @GetMapping("/getRoleAuthTree/{roleId}")
    @ApiOperation("获取角色的权限树")
    public R<List<AuthNode>> getRoleAuths(@PathVariable Long roleId) {
        return R.ok(sysRoleService.getRoleAuths(roleId));
    }
}
