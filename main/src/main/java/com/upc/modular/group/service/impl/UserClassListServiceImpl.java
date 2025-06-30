package com.upc.modular.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.group.controller.param.pageUserClassList;
import com.upc.modular.group.entity.Group;
import com.upc.modular.group.entity.UserClassList;
import com.upc.modular.group.mapper.UserClassListMapper;
import com.upc.modular.group.service.IUserClassListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class UserClassListServiceImpl extends ServiceImpl<UserClassListMapper, UserClassList> implements IUserClassListService {

    @Override
    public boolean insertstudentlist(List<UserClassList> userClassLists) {
        // 1. 参数校验：检查列表是否为空
        if (CollectionUtils.isEmpty(userClassLists)) {
            return false;
        }
        // 2. 填充公共字段
        Long systemOperatorId = -1L;
        LocalDateTime now = LocalDateTime.now();

        // 遍历列表，为每个对象手动设置创建者和创建时间
        for (UserClassList userClassList : userClassLists) {
            userClassList.setCreator(systemOperatorId);
            userClassList.setAddDatetime(now);
        }
        return this.saveBatch(userClassLists);
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
    public UserClassList getByIdStudents(Long groupId) {
        if (groupId == null || groupId <= 0) {
            return null;
        }
        return this.getById(groupId);
    }

    @Override
    public boolean updateByIdStudents(UserClassList userClassList) {
        if (userClassList == null || userClassList.getId() == null) {
            return false;
        }
        // 手动填充操作者和操作时间字段
        // 同样，我们使用一个固定的ID（-1L）代表“系统”或“未知操作者”
        Long systemOperatorId = -1L;
        userClassList.setOperator(systemOperatorId);
        userClassList.setOperationTime(LocalDateTime.now());
        return this.updateById(userClassList);
    }

    @Override
    public Page<UserClassList> selectgetByidPage(pageUserClassList dictType) {
        // 从传入的 dictType 参数中获取分页信息 (current, size)
        Page<UserClassList> page = new Page<>(dictType.getCurrent(), dictType.getSize());
        // 2. 创建查询条件构造器
        LambdaQueryWrapper<UserClassList> queryWrapper = new LambdaQueryWrapper<>();
        // 3. 根据传入的参数动态构建查询条件
        queryWrapper
                // 精确匹配查询
                .eq(dictType.getId() != null, UserClassList::getId, dictType.getId())
                .eq(dictType.getClassId() != null, UserClassList::getClassId, dictType.getClassId())
                .eq(dictType.getUserId() != null, UserClassList::getUserId, dictType.getUserId())
                .eq(dictType.getType() != null, UserClassList::getType, dictType.getType());

        // 4. 添加默认排序规则，例如按创建时间降序
        queryWrapper.orderByDesc(UserClassList::getAddDatetime);
        // 5. 执行分页查询并返回结果
        return baseMapper.selectPage(page, queryWrapper);
    }
}
