package com.upc.modular.group.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.group.controller.param.pageUserClassList;
import com.upc.modular.group.entity.UserClassList;
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
public interface IUserClassListService extends IService<UserClassList> {

    boolean insertstudentlist(List<UserClassList> userClassLists);

    boolean batchDelectStudents(List<Long> idList);

    UserClassList getByIdStudents(Long groupId);

    boolean updateByIdStudents(UserClassList userClassList);

    Page<UserClassList> selectgetByidPage(pageUserClassList dictType);
}
