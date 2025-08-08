package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.entity.SysAuthorityModel;
import com.upc.modular.auth.param.AuthModelParam;
import com.upc.modular.auth.param.AuthModelTreeNode;
import com.upc.modular.auth.service.ISysAuthorityModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
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
    @Autowired
    private ISysAuthorityModelService sysAuthorityModelService;

//    @ApiOperation(value = "新增权限组")
//    @PostMapping("/insertSysAuthorityModel")
//    public R insertSysAuthorityModel(@RequestBody SysAuthorityModel sysAuthorityModel) {
//        if (sysAuthorityModel == null) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
//        }
//        sysAuthorityModel.setStatus(1);
//        sysAuthorityModelService.save(sysAuthorityModel);
//        return R.commonReturn(200, "新增成功", "");
//    }
//
//
//    @ApiOperation(value = "查询权限组信息")
//    @PostMapping("/getSysAuthorityModelById")
//    public R getSysAuthorityModelById(@RequestParam Long id) {
//        if (id == null || id == 0L) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
//        }
//        SysAuthorityModel authorityModel = sysAuthorityModelService.getById(id);
//        return R.commonReturn(200, "查询成功", authorityModel);
//    }
//
//    @ApiOperation(value = "查询所有权限组")
//    @PostMapping("/getSysAuthorityModelList")
//    public R<List<SysAuthorityModel>> getSysAuthorityModelById() {
//        List<SysAuthorityModel> authorityModelList = sysAuthorityModelService.list();
//        return R.commonReturn(200, "查询成功", authorityModelList);
//    }
//
//    @ApiOperation(value = "删除权限组")
//    @PostMapping("/deleteSysAuthorityModelById")
//    public R deleteSysAuthorityModelById(@RequestParam Long id) {
//        if (id == null || id == 0L) {
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
//        }
//        sysAuthorityModelService.removeById(id);
//        return R.commonReturn(200, "删除成功", "");
//    }
//
//    @ApiOperation(value = "修改权限组信息")
//    @PostMapping("/updateSysAuthorityModelById")
//    public R updateSysAuthorityModelById(@RequestBody SysAuthorityModel sysAuthorityModel) {
//        sysAuthorityModelService.updateById(sysAuthorityModel);
//        return R.commonReturn(200, "修改成功", "");
//    }

    @PostMapping("/addModel")
    @ApiOperation("添加权限模块")
    public R<String> addModel(@RequestBody @Validated AuthModelParam authModelParam) {
        sysAuthorityModelService.addModel(authModelParam);
        return R.ok();
    }

    @PostMapping("/deleteModelsByIdList")
    @ApiOperation("根据list删除权限模块，其下的权限会一起删除")
    public R<String> deleteModelsByIdList(@RequestParam("idList")
                                          @NotEmpty(message = "数组不能为空")
                                          List<Integer> idList) {
        sysAuthorityModelService.deleteModelsByIdList(idList);
        return R.ok();
    }

    @PostMapping("/updateModelById")
    @ApiOperation("根据id更改权限模块信息")
    public R<String> updateModelById(@RequestBody AuthModelParam authModelParam) {
        sysAuthorityModelService.updateModelById(authModelParam);
        return R.ok();
    }

    @GetMapping("/getModelPage")
    @ApiOperation("查询权限模块列表")
    public R<List<AuthModelTreeNode>> getModelPage(@RequestParam("parentId") @ApiParam("上级模块id（最上级传0）") Long parentId) {
        return R.ok(sysAuthorityModelService.getModelPage(parentId));
    }
}
