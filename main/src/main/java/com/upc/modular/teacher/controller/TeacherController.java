package com.upc.modular.teacher.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.teacher.vo.GenerateUserResultVo;
import com.upc.modular.teacher.vo.ImportTeacherReturnVo;
import com.upc.modular.teacher.dto.TeacherPageSearchDto;
import com.upc.modular.teacher.entity.Teacher;
import com.upc.modular.teacher.service.ITeacherService;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@RestController
@RequestMapping("/teacher")
@Api(tags = "教师管理")
public class TeacherController {

    @Autowired
    private ITeacherService teacherService;

    @ApiOperation(value = "新增教师")
    @PostMapping("/insert")
    public R insert(@RequestBody Teacher teacher) {
        teacherService.insert(teacher);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "删除教师")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        teacherService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改教师")
    @PutMapping("/update")
    public R update(@RequestBody Teacher teacher) {
        teacherService.updateById(teacher);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询教师")
    @PostMapping("/getPage")
    public R<PageBaseReturnParam<TeacherReturnVo>> getPage(@RequestBody TeacherPageSearchDto param) {
        Page<TeacherReturnVo> page = teacherService.getPage(param);
        PageBaseReturnParam<TeacherReturnVo> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation("批量导入教师 Excel")
    @PostMapping("/importTeacherData")
    @ResponseBody
    public R<ImportTeacherReturnVo> importPopulationData(MultipartFile file){
        String fileName = file.getOriginalFilename();
        if (fileName.matches("^.+\\.(?i)(xls)$")) {
            //03版本excel,xls
            return R.fail("该文件类型已不支持，请使用07版本后缀为.xlsx版本导入");
        } else if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            //07版本,xlsx
            return R.ok(teacherService.importTeacherData(file));
        }else{
            return R.fail("文件格式不支持，请使用xlsx");
        }
    }

    @ApiOperation("查询教师的用户信息")
    @PostMapping("/getTeacherUser")
    public R<SysTbuser> getTeacherUser(@RequestBody TeacherReturnVo param) {
        SysTbuser sysTbuser = teacherService.getTeacherUser(param);
        return R.ok(sysTbuser);
    }

    @ApiOperation("查询未绑定用户的教师")
    @PostMapping("/getTeacherNoUser")
    public R<List<TeacherReturnVo>> getTeacherNoUser() {
        List<TeacherReturnVo> result = teacherService.getTeacherNoUser();
        return R.ok(result);
    }

    @PostMapping("/generateTeacherUsers")
    @ApiOperation("批量生成教师用户并绑定")
    public R<GenerateUserResultVo> generateTeacherUsers() {
        GenerateUserResultVo result = teacherService.generateUsersForTeachers();
        return R.ok(result);
    }




}
