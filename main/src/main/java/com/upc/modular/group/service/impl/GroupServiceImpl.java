package com.upc.modular.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.group.controller.param.pageGroup;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.mapper.GroupMapper;
import com.upc.modular.group.service.IGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.student.controller.param.pageStudent;
import com.upc.modular.student.entity.Student;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public Page<Group> selectgetByidPage(pageGroup dictType) {
        // 从传入的 dictType 参数中获取分页信息 (current, size)
        Page<Group> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        // 2. 创建查询条件构造器
        LambdaQueryWrapper<Group> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 根据传入的参数动态构建查询条件
        queryWrapper
                // 精确匹配查询
                .eq(dictType.getId() != null, Group::getId, dictType.getId())
                .eq(dictType.getGrade() != null, Group::getGrade, dictType.getGrade())
                .eq(dictType.getMajor() != null, Group::getMajor, dictType.getMajor())
                .eq(dictType.getSemester() != null, Group::getSemester, dictType.getSemester())
                .eq(dictType.getStatus() != null, Group::getStatus, dictType.getStatus())
                // 模糊匹配查询 (like)
                .like(!StringUtils.isEmpty(dictType.getName()), Group::getName, dictType.getName());
        // 4. 添加默认排序规则，例如按创建时间降序
        queryWrapper.orderByDesc(Group::getAddDatetime);
        // 5. 执行分页查询并返回结果
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public boolean updateByIdStudents(Group group) {
        if (group == null || group.getId() == null) {
            return false;
        }
        // 手动填充操作者和操作时间字段
        // 同样，我们使用一个固定的ID（-1L）代表“系统”或“未知操作者”
        Long systemOperatorId = -1L;
        group.setOperator(systemOperatorId);
        group.setOperationDatetime(LocalDateTime.now());
        return this.updateById(group);
    }

    @Override
    public Group getByIdStudents(Long groupId) {
        if (groupId == null || groupId <= 0) {
            return null;
        }
        return this.getById(groupId);
    }

    @Override
    public boolean batchDelectStudents(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return false;
        }
        int deletedRows = baseMapper.deleteBatchIds(idList);
        return deletedRows > 0;
    }

    @Override
    public boolean insertstudentlist(List<Group> groupsList) {
        // 1. 参数校验：检查列表是否为空
        if (CollectionUtils.isEmpty(groupsList)) {
            return false;
        }
        // 2. 填充公共字段
        Long systemOperatorId = -1L;
        LocalDateTime now = LocalDateTime.now();

        // 遍历列表，为每个对象手动设置创建者和创建时间
        for (Group group : groupsList) {
            group.setCreator(systemOperatorId);
            group.setAddDatetime(now);
        }
        return this.saveBatch(groupsList);
    }
}
