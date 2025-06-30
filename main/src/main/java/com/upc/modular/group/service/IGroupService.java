package com.upc.modular.group.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.entity.Group;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
public interface IGroupService extends IService<Group> {

    Page<Group> selectgetByidPage(pageGroup dictType);

    boolean updateByIdStudents(Group group);

    Group getByIdStudents(Long groupId);

    boolean batchDelectStudents(List<Long> idList);

    boolean insertstudentlist(List<Group> groupsList);
}
