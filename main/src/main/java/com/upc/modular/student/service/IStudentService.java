package com.upc.modular.student.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface IStudentService extends IService<Student> {

    boolean insertstudentlist(List<Student> studentList);

    boolean batchDelectStudents(List<Long> idList);

    Student getByIdStudents(Long studentId);

    boolean updateByIdStudents(Student student);

    Page<Student> selectgetByidPage(pageStudent dictType);
}
