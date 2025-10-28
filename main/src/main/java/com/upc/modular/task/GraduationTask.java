package com.upc.modular.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.upc.modular.auth.entity.SysTbuser;
import com.upc.modular.auth.service.ISysUserService;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.service.IGroupService;
import com.upc.modular.group.service.impl.UserClassListServiceImpl;
import com.upc.modular.student.entity.Student;
import com.upc.modular.student.service.impl.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GraduationTask {
    @Autowired
    private IGroupService groupService;
    @Autowired
    private UserClassListServiceImpl userClassListService;
    @Autowired
    private StudentServiceImpl studentService;

    @Scheduled(cron = "0 0 0 1 9 ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateGraduationStatus() {
        //查找所有毕业日期在今天之前状态不为2的班级
        List<Group> groupsToGraduate = groupService.lambdaQuery()
                .lt(Group::getGraduationDate, LocalDate.now())
                .ne(Group::getClassStatus,2)
                .list();
        if (CollectionUtils.isEmpty(groupsToGraduate)) {
            return; // 列表为空，不需要处理
        }

        List<Long> groupIds = groupsToGraduate.stream().map(Group::getId).collect(Collectors.toList());

        //批量更新班级状态
        LambdaUpdateWrapper<Group> groupUpdateWrapper = new LambdaUpdateWrapper<>();
        groupUpdateWrapper.in(Group::getId,groupIds).set(Group::getClassStatus,2);
        groupService.update(groupUpdateWrapper);

        //查找该班级下的所有学生
        List<UserClassList> students = userClassListService.lambdaQuery()
                .in(UserClassList::getClassId,groupIds)
                .list();
        if (CollectionUtils.isEmpty(students)) {
            return;
        }
        List<Long> studentIds = students.stream().map(UserClassList::getUserId).collect(Collectors.toList());

        //批量更新学生状态
        LambdaUpdateWrapper<Student> studentUpdateWrapper = new LambdaUpdateWrapper<>();
        studentUpdateWrapper.in(Student::getId,studentIds).set(Student::getAccountStatus,0);
        studentService.update(studentUpdateWrapper);

    }


}
