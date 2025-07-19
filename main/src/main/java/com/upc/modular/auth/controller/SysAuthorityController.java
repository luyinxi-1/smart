package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthority;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.param.SysAuthoritySearchParam;
import com.upc.modular.auth.param.SysAuthorityTreeReturnParam;
import com.upc.modular.auth.service.ISysAuthorityService;
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
@RequestMapping("/sys-authority")
@Api(tags = "用户权限")
public class SysAuthorityController {

    @Autowired
    private ISysAuthorityService sysAuthorityService;

//    @ApiOperation(value = "删除权限")
//    @PostMapping("/deleteSysAuthorityByIds")
//    public R deleteSysAuthorityByIds(@RequestBody List<Long> ids) {
//        sysAuthorityService.deleteSysAuthorityByIds(ids);
//        return R.commonReturn(200, "删除成功", "");
//    }

    @ApiOperation(value = "新增权限")
    @PostMapping("/insertSysAuthority")
    public R insertSysAuthority(@RequestBody SysAuthority sysAuthority) {
        if (sysAuthority == null || !StringUtils.isNotBlank(sysAuthority.getAccessUrl())) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        sysAuthorityService.save(sysAuthority);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "按关键字分页查询权限数据")
    @PostMapping("/getSysAuthorityPage")
    public R<List<SysAuthorityTreeReturnParam>> getSysAuthorityPage(@RequestBody SysAuthoritySearchParam param) {
        return sysAuthorityService.getSysAuthorityPage(param);
    }

    @ApiOperation(value = "根据权限组id查询权限信息")
    @PostMapping("/getSysAuthorityByModelId")
    public R<List<SysAuthority>> getSysAuthorityByModelId(@RequestParam Long SysAuthorityModelId) {

        List<SysAuthority> sysAuthorityList = sysAuthorityService.getSysAuthorityByModelId(SysAuthorityModelId);
        return R.ok(sysAuthorityList);
    }

}
