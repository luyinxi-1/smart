package com.upc.modular.teacher.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.teacher.dto.TeacherLogPageSearchParam;
import com.upc.modular.teacher.entity.TeacherLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-08-21
 */
@Mapper
public interface TeacherLogMapper extends BaseMapper<TeacherLog> {

    Page<TeacherLog> selectTeacherLogPage(
            Page<TeacherLog> page,
            @Param("param") TeacherLogPageSearchParam param);
}
