package com.upc.modular.student.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {


    Page<StudentReturnVo> selectStudentWithDetails(Page<StudentReturnVo> page, StudentPageSearchDto param);

    Long getInstitutionIdByStudentId(@Param("studentId") Long studentId);
}
