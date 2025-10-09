package com.upc.modular.teacher.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.teacher.dto.TeacherPageSearchDto;
import com.upc.modular.teacher.entity.Teacher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.teacher.vo.TeacherReturnVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    Page<TeacherReturnVo> selectTeacherWithInstitution(@Param("page") Page<TeacherReturnVo> page, @Param("param") TeacherPageSearchDto param);

    Long getInstitutionIdByTeacherId(@Param("teacherId") Long teacherId);


    TeacherReturnVo getInformationByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据用户ID获取教师ID
     * @param userId 用户ID
     * @return 教师ID
     */
    Long getTeacherIdByUserId(@Param("userId") Long userId);
}
