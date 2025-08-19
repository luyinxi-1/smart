package com.upc.modular.student.controller;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.student.controller.param.GetStudentIsInInstitutionParam;
import com.upc.modular.student.controller.param.dto.StudentExportDto;
import com.upc.modular.student.controller.param.dto.StudentGenerateDto;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.GenerateUserResultVoStudent;
import com.upc.modular.student.controller.param.vo.ImportStudentReturnVo;
import com.upc.modular.student.controller.param.vo.StudentExcelVo;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.IStudentService;
import com.upc.modular.teacher.dto.BatchUpdateStatusDto;
import com.upc.modular.teacher.dto.GetTeacherIsInInstitutionParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */



@Api(tags = "学生管理")
@RestController
@RequestMapping("/student")
public class StudentController {
    @Autowired
    private IStudentService studentService;

    @ApiOperation(value = "添加学生用户信息")
    @PostMapping("/insert")
    public R insert(@RequestBody Student student) {
       studentService.insertstudent(student);
       return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除学生")
    @PostMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        studentService.deleteByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改学生")
    @PostMapping("/update")
    public R update(@RequestBody Student student) {
        studentService.updateById(student);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询学生")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<StudentReturnVo>> getPage(@RequestBody StudentPageSearchDto param) {
        Page<StudentReturnVo> page = studentService.getPage(param);
        PageBaseReturnParam<StudentReturnVo> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation("批量导入学生 Excel")
    @PostMapping("/importStudentData")
    @ResponseBody
    public R<ImportStudentReturnVo> importPopulationData(MultipartFile file){
        String fileName = file.getOriginalFilename();
        if (fileName.matches("^.+\\.(?i)(xls)$")) {
            //03版本excel,xls
            return R.fail("该文件类型已不支持，请使用07版本后缀为.xlsx版本导入");
        } else if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            //07版本,xlsx
            return R.ok(studentService.importStudentData(file));
        }else{
            return R.fail("文件格式不支持，请使用xlsx");
        }
    }
    @ApiOperation("根据用户ID查询学生的用户信息")
    @PostMapping("/getStudentUser")
    public R<SysTbuser> getStudentUser(@RequestBody StudentReturnVo param) {
        SysTbuser sysTbuser = studentService.getStudentUser(param);
        return R.ok(sysTbuser);
    }


    @ApiOperation("根据用户ID查询学生的学生信息")
    @PostMapping("/getStudent")
    public R<List<Student>> getStudentUser(@RequestBody IdParam idParam) {
        List<Student> sysTbuser = studentService.getStudent(idParam);
        return R.ok(sysTbuser);
    }

    @ApiOperation("查询未绑定用户的学生")
    @PostMapping("/getStudentNoUser")
    public R<List<StudentReturnVo>> getStudentNoUser() {
        List<StudentReturnVo> result = studentService.getStudentNoUser();
        return R.ok(result);
    }

    @PostMapping("/generateStudentUsers")
    @ApiOperation("批量生成学生用户并绑定")
    public R<GenerateUserResultVoStudent> generateTeacherUsers(@RequestBody StudentGenerateDto dto) {
        GenerateUserResultVoStudent result = studentService.generateStudentUsers(dto);
        return R.ok(result);
    }

    @ApiOperation("查询学生是否在该机构下")
    @PostMapping("/getStudentIsInInstitution")
    public R<Boolean> getStudentIsInInstitution(@RequestBody GetStudentIsInInstitutionParam param) {
        Boolean result = studentService.getStudentIsInInstitution(param);
        return R.ok(result);
    }


    @ApiOperation("批量修改学生的状态")
    @PostMapping("/updateStudentAccountStatus")
    public R updateStudentAccountStatus(@RequestBody BatchUpdateStatusDto dto) {
        if (dto.getIds() == null || dto.getIds().isEmpty()) {
            return R.fail("ID列表不能为空");
        }
        studentService.batchUpdateStatus(dto.getIds(), dto.getAccountStatus());
        return R.ok();
    }



    @ApiOperation("导出学生信息")
    @PostMapping("/exportStudentData")
    public void exportStudentData(HttpServletResponse response, @RequestBody StudentExportDto param) {
        studentService.exportStudentData(response, param);
    }






}
