package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.StudentExercisesContentPageSearchParam;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
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
public interface StudentExercisesContentMapper extends BaseMapper<StudentExercisesContent> {

    Page<StudentExercisesContent> selectStudentExercisesContentPage(
            Page<StudentExercisesContent> page,
            @Param("param") StudentExercisesContentPageSearchParam param
    );
}
