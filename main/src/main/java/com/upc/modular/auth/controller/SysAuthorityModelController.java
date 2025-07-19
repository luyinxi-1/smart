package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.service.ISysAuthorityModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xth
 * @since 2025-07-19
 */
@RestController
@RequestMapping("/sys-authority-model")
@Api(tags = "权限组管理")
public class SysAuthorityModelController {
    @Resource
    private ISysAuthorityModelService sysAuthorityModelService;

    @ApiOperation(value = "新增权限组")
    @PostMapping("/insertSysAuthorityModel")
    public R insertSysAuthorityModel(@RequestBody SysAuthorityModel sysAuthorityModel) {
        if (sysAuthorityModel == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        sysAuthorityModel.setStatus(1);
        sysAuthorityModelService.save(sysAuthorityModel);
        return R.commonReturn(200, "新增成功", "");
    }


    @ApiOperation(value = "查询权限组信息")
    @PostMapping("/getSysAuthorityModelById")
    public R getSysAuthorityModelById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        SysAuthorityModel authorityModel = sysAuthorityModelService.getById(id);
        return R.commonReturn(200, "查询成功", authorityModel);
    }

    @ApiOperation(value = "查询所有权限组")
    @PostMapping("/getSysAuthorityModelList")
    public R<List<SysAuthorityModel>> getSysTbroleModelById() {
        List<SysAuthorityModel> authorityModelList = sysAuthorityModelService.list();
        return R.commonReturn(200, "查询成功", authorityModelList);
    }

    @ApiOperation(value = "删除权限组")
    @PostMapping("/deleteSysAuthorityModelById")
    public R deleteSysAuthorityModelById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        sysAuthorityModelService.removeById(id);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改角色组信息")
    @PostMapping("/updateSysAuthorityModelById")
    public R updateSysAuthorityModelById(@RequestBody SysAuthorityModel sysAuthorityModel) {
        sysAuthorityModelService.updateById(sysAuthorityModel);
        return R.commonReturn(200, "修改成功", "");
    }
}
