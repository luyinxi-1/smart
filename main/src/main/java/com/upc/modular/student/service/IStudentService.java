package com.upc.modular.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.student.controller.param.GetStudentIsInInstitutionParam;
import com.upc.modular.student.controller.param.dto.StudentExportDto;
import com.upc.modular.student.controller.param.vo.GenerateUserResultVoStudent;
import com.upc.modular.student.controller.param.vo.ImportStudentReturnVo;
import com.upc.modular.student.controller.param.dto.StudentGenerateDto;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.StudentExcelVo;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface IStudentService extends IService<Student> {

    void insertstudent(Student student);


    void deleteByIds(IdParam idParam);

    Page<StudentReturnVo> getPage(StudentPageSearchDto param);

    ImportStudentReturnVo importStudentData(MultipartFile file);

    SysTbuser getStudentUser(StudentReturnVo param);

    List<StudentReturnVo> getStudentNoUser();

    GenerateUserResultVoStudent generateStudentUsers(StudentGenerateDto dto);

    Boolean getStudentIsInInstitution(GetStudentIsInInstitutionParam param);


    void batchUpdateStatus(List<Long> ids, Integer accountStatus);







    void exportStudentData(HttpServletResponse response, StudentExportDto param);

    List<Student> getStudent(IdParam idParam);
}
