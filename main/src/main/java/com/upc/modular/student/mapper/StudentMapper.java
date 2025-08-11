package com.upc.modular.student.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.student.controller.param.dto.StudentExportDto;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import com.upc.modular.student.entity.Student;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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


    Long getInstitutionIdByStudentId(Long studentId);
    /**
     * 学生列表导出（全量匹配）
     * @param param 查询条件（与分页条件一致）
     * @return 学生导出数据
     */
    List<StudentReturnVo> selectStudentExportList(StudentExportDto param);
}
