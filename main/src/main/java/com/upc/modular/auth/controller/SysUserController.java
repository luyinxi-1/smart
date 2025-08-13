package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.common.utils.UserInfoToRedis;
import com.upc.common.utils.UserUtils;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.param.GetUserIsInInstitutionParam;
import com.upc.modular.auth.param.ImportSysUserReturnParam;
import com.upc.modular.auth.param.SysUserPageSearchParam;
import com.upc.modular.auth.param.UserLoginParam;
import com.upc.modular.auth.param.tree.UserAuthTree;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.auth.service.impl.SysRoleServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
    @Autowired
    private SysRoleServiceImpl sysRoleService;

    @PostMapping("/login")
    @ApiOperation("登录")
    public R<String> login(@RequestBody UserLoginParam userLogin, HttpServletRequest request) {
        return R.ok(sysUserService.login(userLogin, request));
    }

    @PostMapping("/updatePassword")
    @ApiOperation("修改密码")
    public R updatePassword(@RequestParam String oldPassword, String newPassword) {
        return sysUserService.updatePassword(oldPassword, newPassword);
    }

    @ApiOperation(value = "删除用户")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        sysUserService.batchDelete(idParam.getIdList());
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改用户信息")
    @PostMapping("/update")
    public R update(@RequestBody SysTbuser param) {
        sysUserService.updateById(param);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询用户信息")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<SysTbuser>> getPage(@RequestBody SysUserPageSearchParam param) {
        Page<SysTbuser> page = sysUserService.getPage(param);
        PageBaseReturnParam<SysTbuser> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查询用户是否在该机构里")
    @PostMapping("/getUserIsInInstitution")
    public R<Boolean> getUserIsInInstitution(@RequestBody GetUserIsInInstitutionParam param) {
        Boolean result = sysUserService.getUserIsInInstitution(param);
        return R.ok(result);
    }

    @ApiOperation(value = "新增用户")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody SysTbuser sysTbuser) {
        return R.ok(sysUserService.insert(sysTbuser));
    }

    @GetMapping("/getUserAuthTree")
    @ApiOperation("获取用户权限树")
    public R<UserAuthTree> getUserAuthTree() {
        return R.ok(sysRoleService.getUserAuthTree());
    }

    @GetMapping("/getUserInfo")
    @ApiOperation("获取当前用户信息")
    public R getUserInfo() {
        UserInfoToRedis userInfoToRedis = UserUtils.get();
        return R.ok(userInfoToRedis);
    }
}
