package com.upc.modular.group.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.group.controller.param.GetMyClasssReturnParam;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.controller.param.pageUserClassList;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.group.service.IUserClassListService;
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
@RequestMapping("/user-class-list")
@Api(tags = "班级用户关联")
public class UserClassListController {
    @Autowired
    private IUserClassListService userClassListService;

    @ApiOperation(value = "批量添加班级用户信息")
    @PostMapping("/insert")
    public R insert(@RequestBody List<UserClassList> userClassLists) {
        if (userClassLists == null || userClassLists.isEmpty()) {
            return R.fail("班级用户列表不能为空。");
        }
        boolean success = userClassListService.insertstudentlist(userClassLists);
        return success ? R.ok("成功添加 " + userClassLists.size() + " 个班级用户信息。") : R.fail("新增失败。");
    }


    @ApiOperation(value = "根据ID批量删除班级用户信息")
    @PostMapping("/batchDelete")
    public R<String> batchDelete(@RequestBody IdParam idParam) {
        if (idParam == null || idParam.getIdList() == null || idParam.getIdList().isEmpty()) {
            return R.fail("要删除的ID列表不能为空。");
        }
        boolean success = userClassListService.batchDelectStudents(idParam.getIdList());
        return success ? R.ok("删除成功。") : R.fail("删除失败。");
    }



    @ApiOperation(value = "根据ID获取班级用户信息")
    @GetMapping("/getById")
    public R<UserClassList> getById(@RequestParam("groupId") Long groupId) {
        UserClassList userClassList = userClassListService.getByIdStudents(groupId);
        return userClassList != null ? R.ok(userClassList) : R.fail("未找到该班级信息。");
    }

    @ApiOperation(value = "更新班级信息")
    @PostMapping("/update")
    public R<String> update(@RequestBody UserClassList userClassList) {
        if (userClassList.getId() == null) {
            return R.fail("更新时班级用户信息时ID是必需的。");
        }
        boolean success = userClassListService.updateByIdStudents(userClassList);
        return success ? R.ok("修改成功。") : R.fail("修改失败。");
    }


    @ApiOperation(value = "分页按条件查询班级用户信息")
    @PostMapping("/getByidPage")
    public R<PageBaseReturnParam<UserClassList>> getPage(@RequestBody pageUserClassList dictType) {
        Page<UserClassList> page = userClassListService.selectgetByidPage(dictType);
        PageBaseReturnParam<UserClassList> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查看我的班级")
    @PostMapping("/getMyClass")
    public R<List<GetMyClasssReturnParam>> getMyClass() {
        return R.ok(userClassListService.getMyClass());
    }


}
