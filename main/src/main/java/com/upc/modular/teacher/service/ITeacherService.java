package com.upc.modular.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.teacher.vo.GenerateUserResultVo;
import com.upc.modular.teacher.vo.ImportTeacherReturnVo;
import com.upc.modular.teacher.dto.TeacherPageSearchDto;
import com.upc.modular.teacher.entity.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
public interface ITeacherService extends IService<Teacher> {

    void insert(Teacher teacher);

    void deleteDictItemByIds(IdParam idParam);

    Page<Teacher> getPage(TeacherPageSearchDto param);

    ImportTeacherReturnVo importTeacherData(MultipartFile file);

    SysTbuser getTeacherUser(Teacher param);

    GenerateUserResultVo generateUsersForTeachers();
}
