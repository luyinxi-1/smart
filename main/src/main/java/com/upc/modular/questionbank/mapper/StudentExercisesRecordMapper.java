package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.StudentExercisesRecordPageSearchParam;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
@Mapper
public interface StudentExercisesRecordMapper extends BaseMapper<StudentExercisesRecord> {

    Page<StudentExercisesRecord> selectStudentExercisesRecordPage(
            Page<StudentExercisesRecord> page,
            @Param("param") StudentExercisesRecordPageSearchParam param
    );
}
