package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.service.ISysAuthorityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
    private ISysAuthorityService sysAuthorityService;

    @ApiOperation(value = "删除角色权限关联关系")
    @PostMapping("/deleteRoleAuthority")
    public R deleteSysAuthorityByIds(@RequestBody List<Long> ids) {
        sysAuthorityService.deleteSysAuthorityByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增权限")
    @PostMapping("/insertSysAuthority")
    public R insertSysAuthority(@RequestBody List<SysAuthority> sysAuthoritys) {
        if (sysAuthoritys.isEmpty()) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        sysAuthorityService.saveBatch(sysAuthoritys);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "按关键字分页查询权限数据")
    @PostMapping("/getSysAuthorityPage")
    public R<Page<SysAuthority>> getSysAuthorityPage(@RequestBody SysAuthoritySearchParam param) {
        return sysAuthorityService.getSysAuthorityPage(param);
    }


}
