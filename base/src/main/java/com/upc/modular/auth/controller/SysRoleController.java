package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.entity.SysRole;
import com.upc.modular.auth.param.SysRoleSearchParam;
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

    @ApiOperation(value = "查询角色信息")
    @PostMapping("/geSysRoleById")
    public R geSysRoleById(@RequestParam("sysRoleId") Integer sysRoleId) {
        SysRole sysRole = sysRoleService.getById(sysRoleId);
        return R.commonReturn(200, "查询成功", sysRole);
    }

    @ApiOperation(value = "修改角色信息")
    @PostMapping("/updateSysRoleById")
    public R updateSysRoleById(@RequestBody SysRole sysRole) {
        sysRoleService.updateSysRoleById(sysRole);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页按条件查询字典类型")
    @PostMapping("/getSysRolePage")
    public R<Page<SysRole>> getSysRolePage(@RequestBody SysRoleSearchParam param) {
        return sysRoleService.getSysRolePage(param);
    }

}
