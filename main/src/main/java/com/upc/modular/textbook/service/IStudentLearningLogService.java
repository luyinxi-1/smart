package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.controller.param.dto.StudentLearningLogSaveParam;
import com.upc.modular.textbook.entity.StudentLearningLog;
import com.upc.modular.textbook.param.StudentLearningLogPageSearchParam;

public interface IStudentLearningLogService extends IService<StudentLearningLog> {

    /**
     * 保存学习日志（学生）
     *
     * @param param 学习日志参数
     * @return R<Void>
     */
    R<Void> saveLog(StudentLearningLogSaveParam param);

    /**
     * 提交学习日志（学生）
     *
     * @param logId 日志ID
     * @return R<Void>
     */
    R<Void> submitLog(Long logId);


    Page<StudentLearningLog> getLogPage(StudentLearningLogPageSearchParam param);

    /**
     * 查询日志详情
     *
     * @param logId 日志ID
     * @return StudentLearningLog
     */
    StudentLearningLog getLogDetail(Long logId);

    /**
     * 删除未提交的学习日志（学生）
     *
     * @param logId 日志ID
     * @return R<Void>
     */
    R<Void> deleteUnsubmittedLog(Long logId);
}