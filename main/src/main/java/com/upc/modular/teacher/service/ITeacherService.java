package com.upc.modular.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.teacher.dto.TeacherGenerateDto;
import com.upc.modular.teacher.dto.TeacherInsertDto;
import com.upc.modular.teacher.vo.GenerateUserResultVo;
import com.upc.modular.teacher.vo.ImportTeacherReturnVo;
import com.upc.modular.teacher.dto.TeacherPageSearchDto;
import com.upc.modular.teacher.entity.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
public interface ITeacherService extends IService<Teacher> {

    void insert(TeacherInsertDto teacher);

    void deleteDictItemByIds(IdParam idParam);

    Page<TeacherReturnVo> getPage(TeacherPageSearchDto param);

    ImportTeacherReturnVo importTeacherData(MultipartFile file);

    SysTbuser getTeacherUser(TeacherReturnVo param);

    List<TeacherReturnVo> getTeacherNoUser();

    GenerateUserResultVo generateTeacherUsers(TeacherGenerateDto dto);
}
