package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.StudentExercisesRecordPageSearchParam;
import com.upc.modular.questionbank.controller.param.SubmitAnswerRequest;
import com.upc.modular.questionbank.entity.StudentExercisesRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface IStudentExercisesRecordService extends IService<StudentExercisesRecord> {

    Long submitAnswers(Long userId, SubmitAnswerRequest request);

    void calculateAndUpdateFinalGrade(Long recordId);

    Void deleteStudentExercisesRecordByIds(IdParam idParam);

    void updateStudentExercisesRecord(StudentExercisesRecord param);

    Page<StudentExercisesRecord> selectStudentExercisesRecordPage(StudentExercisesRecordPageSearchParam param);

    void inserStudentExercisesRecord(StudentExercisesRecord param);
}
