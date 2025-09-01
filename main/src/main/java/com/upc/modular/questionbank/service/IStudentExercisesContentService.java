package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesContentPageSearchParam;
import com.upc.modular.questionbank.entity.StudentExercisesContent;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface IStudentExercisesContentService extends IService<StudentExercisesContent> {
    void inserStudentExercisesContent(StudentExercisesContent param);

    Void deleteStudentExercisesContentByIds(IdParam idParam);

    void updateStudentExercisesContent(StudentExercisesContent param);

    Page<StudentExercisesContent> selectStudentExercisesContentPage(StudentExercisesContentPageSearchParam param);
}
