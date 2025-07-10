package com.upc.modular.auth.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.UserRoleList;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.auth.param.UserRoleListPageSearchParam;
import com.upc.modular.auth.service.IUserRoleListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/user-role-list")
@Api(tags = "用户角色关联")
public class UserRoleListController {

    @Autowired
    private IUserRoleListService userRoleListService;

    @ApiOperation(value = "新增用户角色关联")
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody UserRoleList userRoleList) {
        return R.ok(userRoleListService.insert(userRoleList));
    }

    @ApiOperation(value = "删除用户角色关联")
    @DeleteMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody IdParam idParam) {
        return R.ok(userRoleListService.batchDelete(idParam));
    }

    @ApiOperation(value = "更新用户角色关联")
    @PutMapping("/update")
    public R<Boolean> updateUserRoleList(@RequestBody UserRoleList userRoleList) {
        return R.ok(userRoleListService.updateUserRoleList(userRoleList));
    }

    @ApiOperation(value = "分页查询用户角色关联")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<UserRoleListPageReturnParam>> getPage(@RequestBody UserRoleListPageSearchParam param) {
        Page<UserRoleListPageReturnParam> page = userRoleListService.getPage(param);
        PageBaseReturnParam<UserRoleListPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
}
