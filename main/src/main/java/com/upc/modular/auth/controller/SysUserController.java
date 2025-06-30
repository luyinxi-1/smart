package com.upc.modular.auth.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.param.UserLoginParam;
import com.upc.modular.auth.service.ISysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/sys-user")
@Api(tags = "用户管理")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @PostMapping("/login")
    @ApiOperation("登录")
    public R<String> login(@RequestBody UserLoginParam userLogin, HttpServletRequest request) {
        return R.ok(sysUserService.login(userLogin, request));
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        sysUserService.batchDelete(idParam.getIdList());
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改用户信息")
    @PutMapping("/update")
    public R update(@RequestBody SysTbuser param) {
        sysUserService.updateById(param);
        return R.commonReturn(200, "修改成功", "");
    }



}
