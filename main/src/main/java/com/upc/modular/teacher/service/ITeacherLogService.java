package com.upc.modular.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.teacher.dto.TeacherLogPageSearchParam;
import com.upc.modular.teacher.entity.TeacherLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-08-21
 */
public interface ITeacherLogService extends IService<TeacherLog> {

    void inserTeacherLog(TeacherLog param);

    Page<TeacherLog> selectTeacherLogPage(TeacherLogPageSearchParam param);
}
