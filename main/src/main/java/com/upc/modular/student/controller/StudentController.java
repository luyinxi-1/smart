package com.upc.modular.student.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.SysDictTypePageSearchParam;
import com.upc.modular.auth.entity.SysDictType;
import com.upc.modular.auth.entity.SysTbrole;
import com.upc.modular.auth.param.SysRoleSearchParam;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
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
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private IStudentService studentService;

    @ApiOperation(value = "批量添加学生用户信息")
    @PostMapping("/insert")
    public R insert(@RequestBody List<Student> studentList) {
        if (studentList == null || studentList.isEmpty()) {
            return R.fail("学生列表不能为空。");
        }
        boolean success = studentService.insertstudentlist(studentList);
        return success ? R.ok("成功添加 " + studentList.size() + " 名学生。") : R.fail("新增失败。");
    }


    @ApiOperation(value = "根据ID批量删除学生记录")
    @DeleteMapping("/batchDelete")
    public R<String> batchDelete(@RequestBody IdParam idParam) {
        if (idParam == null || idParam.getIdList() == null || idParam.getIdList().isEmpty()) {
            return R.fail("要删除的ID列表不能为空。");
        }
        boolean success = studentService.batchDelectStudents(idParam.getIdList());
        return success ? R.ok("删除成功。") : R.fail("删除失败。");
    }



    @ApiOperation(value = "根据ID获取学生信息")
    @GetMapping("/getById")
    public R<Student> getById(@RequestParam("studentId") Long studentId) {
        Student student = studentService.getByIdStudents(studentId);
        return student != null ? R.ok(student) : R.fail("未找到该学生信息。");
    }

    @ApiOperation(value = "更新学生信息")
    @PutMapping("/update")
    public R<String> update(@RequestBody Student student) {
        if (student.getId() == null) {
            return R.fail("更新时学生ID是必需的。");
        }
        boolean success = studentService.updateByIdStudents(student);
        return success ? R.ok("修改成功。") : R.fail("修改失败。");
    }


    @ApiOperation(value = "分页按条件查询学生信息")
    @PostMapping("/getByidPage")
    public R<PageBaseReturnParam<Student>> getPage(@RequestBody pageStudent dictType) {
        Page<Student> page = studentService.selectgetByidPage(dictType);
        PageBaseReturnParam<Student> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }


}
