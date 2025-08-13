package com.upc.modular.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.questionbank.controller.param.TeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.controller.param.TopLevelTeachingQuestionClassificationSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionClassification;
import com.upc.modular.questionbank.mapper.TeachingQuestionClassificationMapper;
import com.upc.modular.questionbank.service.ITeachingQuestionClassificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.TextbookClassification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author la
 * @since 2025-08-12
 */
@Service
public class TeachingQuestionClassificationServiceImpl extends ServiceImpl<TeachingQuestionClassificationMapper, TeachingQuestionClassification> implements ITeachingQuestionClassificationService {

    @Autowired
    private TeachingQuestionClassificationMapper teachingQuestionClassificationMapper;

    @Override
    public void insertTeachingQuestionClassification(TeachingQuestionClassification param) {
        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "前端传参为空");
        }
        if (ObjectUtils.isEmpty(param.getParentId())) {
            param.setParentId(0L);
            param.setClassificationGrade(1);
        } else {
            TeachingQuestionClassification teachingQuestionClassification = teachingQuestionClassificationMapper.selectById(param.getParentId());
            if (ObjectUtils.isNotEmpty(teachingQuestionClassification) && teachingQuestionClassification.getClassificationGrade() != 3) {
                if (teachingQuestionClassification.getClassificationGrade() == 1) {
                    param.setClassificationGrade(2);
                } else {
                    param.setClassificationGrade(3);
                }
            } else {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "，父id不存在或不能在三级分类下再插入子分类");
            }
        }
        Integer sortNumber = teachingQuestionClassificationMapper.selectMaxSortNumber(param);
        if (ObjectUtils.isEmpty(param.getSortNumber())) {
            if (ObjectUtils.isNotEmpty(sortNumber)) {
                sortNumber = sortNumber + 1;
            } else {
                sortNumber = 1;
            }
            param.setSortNumber(sortNumber);
        }
        this.save(param);
    }

    @Override
    public void removeTeachingQuestionClassification(List<Long> idList) {
        for (Long id : idList) {
            // 查询一级分类下的所有二级分类
            MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TeachingQuestionClassification::getParentId, id);
            List<TeachingQuestionClassification> secondLevelList = teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper);

            if (ObjectUtils.isNotEmpty(secondLevelList)) {
                // 遍历二级分类
                for (TeachingQuestionClassification secondLevel : secondLevelList) {
                    // 查询二级分类下的所有三级分类
                    MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper1 = new MyLambdaQueryWrapper<>();
                    lambdaQueryWrapper1.eq(TeachingQuestionClassification::getParentId, secondLevel.getId());
                    List<TeachingQuestionClassification> thirdLevelList = teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper1);
                    if (ObjectUtils.isNotEmpty(thirdLevelList)) {
                        // 删除所有三级分类
                        for (TeachingQuestionClassification thirdLevel : thirdLevelList) {
                            teachingQuestionClassificationMapper.deleteById(thirdLevel.getId());
                        }
                    }
                    // 删除二级分类
                    teachingQuestionClassificationMapper.deleteById(secondLevel.getId());
                }
            }
            // 删除一级分类
            teachingQuestionClassificationMapper.deleteById(id);
        }
    }

    @Override
    public boolean updateTeachingQuestionClassification(TeachingQuestionClassification param) {
        if (ObjectUtils.isEmpty(param.getOperator()) && ObjectUtils.isNotEmpty(UserUtils.get().getUsername())) {
            param.setOperator(UserUtils.get().getId());
        }
        if (ObjectUtils.isEmpty(param.getOperationDatetime())) {
            param.setOperationDatetime(LocalDateTime.now());
        }
        return teachingQuestionClassificationMapper.updateTeachingQuestionClassification(param);
    }

    @Override
    public List<TeachingQuestionClassification> selectTeachingQuestionClassificationParentIdList(Integer classificationGrade) {
        if (ObjectUtils.isNotEmpty(classificationGrade)) {
            MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            // 假如为3级分类，则获取了所有的一二级分类
            lambdaQueryWrapper.lt(TeachingQuestionClassification::getClassificationGrade, classificationGrade); //.lt 代表 "less than"（小于）
            List<TeachingQuestionClassification> list = teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper);
            if (ObjectUtils.isNotEmpty(list)) {
                return list;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<TeachingQuestionClassification> selectTeachingQuestionClassificationDownList(Long id) {
        if (ObjectUtils.isNotEmpty(id)) {
            MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TeachingQuestionClassification::getParentId, id);
            List<TeachingQuestionClassification> list = teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper);
            if (ObjectUtils.isNotEmpty(list)) {
                return list;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<TeachingQuestionClassification> selectTeachingQuestionClassificationList(TeachingQuestionClassificationSearchParam param) {
        MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.like(ObjectUtils.isNotEmpty(param.getTeachingQuestionClassificationName()), TeachingQuestionClassification::getTeachingQuestionClassificationName, param.getTeachingQuestionClassificationName());
        lambdaQueryWrapper.orderBy(true, Objects.equals(param.getIsAsc(), 1), TeachingQuestionClassification::getSortNumber);
        return teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<TeachingQuestionClassification> buildDictTree(List<TeachingQuestionClassification> list) {
        // 将分类按照父ID分组
        Map<Long, List<TeachingQuestionClassification>> map = list.stream()
                .collect(Collectors.groupingBy(TeachingQuestionClassification::getParentId));

        // 为每个分类设置子分类
        list.forEach(item -> item.setChildren(map.get(item.getId())));

        // 找到所有根节点（假设根节点的父ID为0）
        List<TeachingQuestionClassification> roots = list.stream()
                .filter(item -> item.getParentId() == 0)
                .collect(Collectors.toList()); // 将所有根节点收集到一个列表中
        return roots;
    }

    @Override
    public boolean updateTeachingQuestionClassificationSortName(Long id, Integer param) {
        TeachingQuestionClassification currentTag = teachingQuestionClassificationMapper.selectById(id);
        // 根据param决定是向上还是向下调整
        boolean isNext = param != 0;
        TeachingQuestionClassification adjacentTag = getAdjacentClassifiaction(currentTag, isNext);
        if (adjacentTag == null) {
            return false;
        }
        // 交换sortNumber
        Integer tempSortNumber = currentTag.getSortNumber();
        currentTag.setSortNumber(adjacentTag.getSortNumber());
        adjacentTag.setSortNumber(tempSortNumber);
        // 更新数据库
        teachingQuestionClassificationMapper.updateById(currentTag);
        teachingQuestionClassificationMapper.updateById(adjacentTag);
        return true;
    }

    @Override
    public List<TeachingQuestionClassification> selectTopLevelTeachingQuestionClassification() {
        MyLambdaQueryWrapper<TeachingQuestionClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        // 查询条件：分类等级为 1 (顶级)
        lambdaQueryWrapper.eq(TeachingQuestionClassification::getClassificationGrade, 1);
        // 加上排序，保证每次返回的顺序一致
        lambdaQueryWrapper.orderByAsc(TeachingQuestionClassification::getSortNumber);
        return teachingQuestionClassificationMapper.selectList(lambdaQueryWrapper);
    }

    public TeachingQuestionClassification getAdjacentClassifiaction(TeachingQuestionClassification currentTag, boolean isNext) {
        LambdaQueryWrapper<TeachingQuestionClassification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachingQuestionClassification::getParentId, currentTag.getParentId());
        // 根据 isNext 决定查询前一条还是后一条记录
        if (isNext) {
            queryWrapper.gt(TeachingQuestionClassification::getSortNumber, currentTag.getSortNumber())
                    .orderByAsc(TeachingQuestionClassification::getSortNumber);
        } else {
            queryWrapper.lt(TeachingQuestionClassification::getSortNumber, currentTag.getSortNumber())
                    .orderByDesc(TeachingQuestionClassification::getSortNumber);
        }

        // 使用 Page 对象限制查询结果为 1 条
        Page<TeachingQuestionClassification> page = new Page<>(1, 1);
        page = teachingQuestionClassificationMapper.selectPage(page, queryWrapper);

        // 获取查询结果
        List<TeachingQuestionClassification> records = page.getRecords();
        if (records.isEmpty()) {
            return null; // 如果没有找到相邻记录，则返回 null
        }
        return records.get(0); // 返回查询到的相邻记录
    }
}
