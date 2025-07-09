package com.upc.modular.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.modular.group.controller.param.UserTypeCount;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.group.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.service.IInstitutionService;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import org.apache.poi.hpsf.ClassID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-06-26
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    @Autowired
    private UserClassListServiceImpl userClassListService;

    @Autowired
    private IInstitutionService institutionService;

    @Override
    public Page<Group> selectgetByidPage(pageGroup dictType) {
        Page<Group> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(dictType.getId() != null, Group::getId, dictType.getId())
                .eq(dictType.getGrade() != null, Group::getGrade, dictType.getGrade())
                .eq(dictType.getStatus() != null, Group::getStatus, dictType.getStatus())
                .like(!StringUtils.isEmpty(dictType.getName()), Group::getName, dictType.getName())
                .eq(Group::getStatus, 1);
        queryWrapper.orderByDesc(Group::getAddDatetime);
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateByIdStudents(Group group) {
        if (group == null || group.getId() == null) {
            return false;
        }

        // 1. 获取更新前的班级信息，用于定位对应的组织
        Group oldGroup = this.getById(group.getId());
        if(oldGroup == null || !Integer.valueOf(1).equals(oldGroup.getStatus())) {
            // 如果班级不存在或状态不是1，则不允许更新
            return false;
        }

        // 2. 查找对应的组织
        LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
        // 使用更新前的 名称 和 父组织ID 来定位
        wrapper.eq(Institution::getInstitutionName, oldGroup.getName())
                .eq(Institution::getFatherInstitutionId, oldGroup.getInstitutionId());
        Institution institutionToUpdate = institutionService.getOne(wrapper);

        // 3. 如果找到了对应的组织，则更新它
        if (institutionToUpdate != null) {
            institutionToUpdate.setInstitutionName(group.getName()); // 更新名称
            institutionToUpdate.setFatherInstitutionId(group.getInstitutionId()); // 更新父级ID
            institutionToUpdate.setIntroduction(group.getRemark()); // 更新介绍
            institutionService.updateById(institutionToUpdate);
        }

        // 4. 更新班级表
        return this.updateById(group);
    }


    @Override
    public Group getByIdStudents(Long groupId) {
        if (groupId == null || groupId <= 0) {
            return null;
        }
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Group::getId, groupId)
                .eq(Group::getStatus, 1);
        return this.getOne(queryWrapper);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDelectStudents(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        // 1. 根据ID列表查出所有要删除的班级实体
        List<Group> groupsToDelete = this.listByIds(idList);
        if (CollectionUtils.isEmpty(groupsToDelete)) {
            return true; // 列表为空，认为删除成功
        }

        // 2. 准备查询条件，找到所有对应的组织
        List<Long> institutionIdsToDelete = new ArrayList<>();
        for (Group group : groupsToDelete) {
            LambdaQueryWrapper<Institution> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Institution::getInstitutionName, group.getName())
                    .eq(Institution::getFatherInstitutionId, group.getInstitutionId());
            List<Institution> institutions = institutionService.list(wrapper);
            institutionIdsToDelete.addAll(institutions.stream().map(Institution::getId).collect(Collectors.toList()));
        }

        // 3. 如果找到了要删除的组织，执行删除
        if (!institutionIdsToDelete.isEmpty()) {
            institutionService.removeByIds(institutionIdsToDelete);
        }

        // 4. 删除班级表中的数据
        int deletedRows = baseMapper.deleteBatchIds(idList);
        return deletedRows > 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertstudentlist(List<Group> groupsList) {
        if (CollectionUtils.isEmpty(groupsList)) {
            return false;
        }

        for (Group group : groupsList) {
            // 1. 保存班级信息到 group 表
            this.save(group); // mybatis-plus的save方法会自动将生成的主键回填到group对象中

            // 2. 创建一个新的 Institution 对象
            Institution institution = new Institution();
            institution.setInstitutionName(group.getName()); // 机构名称 = 班级名称
            institution.setFatherInstitutionId(group.getInstitutionId()); // 父级ID = 班级所属组织ID
            institution.setIntroduction(group.getRemark()); // 介绍 = 班级备注
            // 使用UUID生成机构编码
            String token = UUID.randomUUID().toString().replace("-", "_");
            institution.setInstitutionCode(token);

            // 3. 保存组织信息到 institution 表
            institutionService.save(institution);
        }

        return true;
    }

    @Override
    public Map<String, Long> getUserTypeCountByClassId(Long groupId) {
        // --- 修改点：先检查班级是否存在且状态为1 ---
        Group group = this.getByIdStudents(groupId); // 复用带有status检查的查询方法
        if (group == null) {
            // 如果班级无效或不存在，返回空结果
            return Collections.emptyMap();
        }

        MyLambdaQueryWrapper<UserClassList> listMyLambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        listMyLambdaQueryWrapper.eq(UserClassList::getClassId, groupId)
                .select(UserClassList::getType);


        List<UserClassList> userList = userClassListService.list(listMyLambdaQueryWrapper);

        Map<Integer, Long> typeCountMap = userList.stream()
                .collect(Collectors.groupingBy(UserClassList::getType, Collectors.counting()));

        Map<String, Long> result = new HashMap<>();
        result.put("管理员", typeCountMap.getOrDefault(0, 0L));
        result.put("学生", typeCountMap.getOrDefault(1, 0L));
        result.put("老师", typeCountMap.getOrDefault(2, 0L));
        return result;
    }
}
