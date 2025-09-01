package com.upc.modular.group.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.controller.param.pageGroupVo;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@RestController
@RequestMapping("/group")
@Api(tags = "班级管理模块")
public class GroupController {
    @Autowired
    private IGroupService groupService;



    @ApiOperation(value = "批量添加班级信息")
    @PostMapping("/insert")
    public R insert(@RequestBody List<Group> groupsList) {
        if (groupsList == null || groupsList.isEmpty()) {
            return R.fail("班级列表不能为空。");
        }
        boolean success = groupService.insertstudentlist(groupsList);
        return success ? R.ok("成功添加 " + groupsList.size() + " 个班级。") : R.fail("新增失败。");
    }


    @ApiOperation(value = "根据ID批量删除班级信息")
    @PostMapping("/batchDelete")
    public R<String> batchDelete(@RequestBody IdParam idParam) {
        if (idParam == null || idParam.getIdList() == null || idParam.getIdList().isEmpty()) {
            return R.fail("要删除的ID列表不能为空。");
        }
        boolean success = groupService.batchDelectStudents(idParam.getIdList());
        return success ? R.ok("删除成功。") : R.fail("删除失败。");
    }


    @ApiOperation(value = "根据ID获取班级信息")
    @GetMapping("/getById")
    public R<Group> getById(@RequestParam("groupId") Long groupId) {
        Group group = groupService.getByIdStudents(groupId);
        return group != null ? R.ok(group) : R.fail("未找到该班级信息。");
    }

    @ApiOperation(value = "更新班级信息")
    @PostMapping("/update")
    public R<String> update(@RequestBody Group group) {
        if (group.getId() == null) {
            return R.fail("更新时班级信息时ID是必需的。");
        }
        boolean success = groupService.updateByIdStudents(group);
        return success ? R.ok("修改成功。") : R.fail("修改失败。");
    }


    @ApiOperation(value = "分页按条件查询班级信息")
    @PostMapping("/getByidPage")
    public R<PageBaseReturnParam<pageGroupVo>> getPage(@RequestBody pageGroup dictType) {
        Page<pageGroupVo> page = groupService.selectgetByidPage(dictType);
        PageBaseReturnParam<pageGroupVo> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "按照班级查询班级关联人数")
    @PostMapping("/getByiduser")
    public R<Map<String,Long>> ByIdGetUsers(@RequestParam("groupId") Long groupId) {
        Map<String,Long> result = groupService.getUserTypeCountByClassId(groupId);
        return R.ok(result);
    }

}
