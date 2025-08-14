package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.utils.UserUtils;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.textbook.entity.TextbookClassification;
import com.upc.modular.textbook.mapper.TextbookClassificationMapper;
import com.upc.modular.textbook.param.TextbookClassificationSearchParam;
import com.upc.modular.textbook.param.TopLevelTextbookClassificationSearchParam;
import com.upc.modular.textbook.service.ITextbookClassificationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-08-12
 */
@Service
public class TextbookClassificationServiceImpl extends ServiceImpl<TextbookClassificationMapper, TextbookClassification> implements ITextbookClassificationService {

    @Autowired
    private TextbookClassificationMapper textbookClassificationMapper;
    @Override
    public void insertTextbookClassification(TextbookClassification param) {

        if (ObjectUtils.isEmpty(param)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "前端传参为空");
        }

        if (ObjectUtils.isEmpty(param.getParentId())) {
            param.setParentId(0L);
            param.setClassificationGrade(1);
        } else {
            TextbookClassification TextbookClassification = textbookClassificationMapper.selectById(param.getParentId());
            if (ObjectUtils.isNotEmpty(TextbookClassification) && TextbookClassification.getClassificationGrade() != 3) {
                if (TextbookClassification.getClassificationGrade() == 1) {
                    param.setClassificationGrade(2);
                } else {
                    param.setClassificationGrade(3);
                }
            } else {
                throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "父id不存在或不能在三级分类下再插入子分类");
            }
        }
        Integer sortNumber = textbookClassificationMapper.selectMaxSortNumber(param);
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
    public void removeTextbookClassification(List<Long> idList) {
        for (Long id : idList) {
            // 查询一级分类下的所有二级分类
            MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TextbookClassification::getParentId, id);
            List<TextbookClassification> secondLevelList = textbookClassificationMapper.selectList(lambdaQueryWrapper);

            if (ObjectUtils.isNotEmpty(secondLevelList)) {
                // 遍历二级分类
                for (TextbookClassification secondLevel : secondLevelList) {
                    // 查询二级分类下的所有三级分类
                    MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper1 = new MyLambdaQueryWrapper<>();
                    lambdaQueryWrapper1.eq(TextbookClassification::getParentId, secondLevel.getId());
                    List<TextbookClassification> thirdLevelList = textbookClassificationMapper.selectList(lambdaQueryWrapper1);
                    if (ObjectUtils.isNotEmpty(thirdLevelList)) {
                        // 删除所有三级分类
                        for (TextbookClassification thirdLevel : thirdLevelList) {
                            textbookClassificationMapper.deleteById(thirdLevel.getId());
                        }
                    }
                    // 删除二级分类
                    textbookClassificationMapper.deleteById(secondLevel.getId());
                }
            }
            // 删除一级分类
            textbookClassificationMapper.deleteById(id);
        }
    }

    @Override
    public boolean updateTextbookClassification(TextbookClassification param) {
        if (ObjectUtils.isEmpty(param.getOperator()) && ObjectUtils.isNotEmpty(UserUtils.get().getId())) {
            param.setOperator(UserUtils.get().getId());
        }
        if (ObjectUtils.isEmpty(param.getOperationDatetime())) {
            param.setOperationDatetime(LocalDateTime.now());
        }
        return textbookClassificationMapper.updateProductClassification(param);
    }

    @Override
    public List<TextbookClassification> selectTextbookClassificationParentIdList(Integer classificationGrade) {
        if (ObjectUtils.isNotEmpty(classificationGrade)) {
            MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            // 假如为3级分类，则获取了所有的一二级分类
            lambdaQueryWrapper.lt(TextbookClassification::getClassificationGrade, classificationGrade);
            List<TextbookClassification> list = textbookClassificationMapper.selectList(lambdaQueryWrapper);
            if (ObjectUtils.isNotEmpty(list)) {
                return list;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<TextbookClassification> selectTextbookClassificationDownList(Long id) {
        if (ObjectUtils.isNotEmpty(id)) {
            MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TextbookClassification::getParentId, id);
            List<TextbookClassification> list = textbookClassificationMapper.selectList(lambdaQueryWrapper);
            if (ObjectUtils.isNotEmpty(list)) {
                return list;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<TextbookClassification> selectTextbookClassificationList(TextbookClassificationSearchParam param) {

        MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();

        lambdaQueryWrapper.like(ObjectUtils.isNotEmpty(param.getClassificationName()), TextbookClassification::getClassificationName, param.getClassificationName());
        lambdaQueryWrapper.orderBy(true, Objects.equals(param.getIsAsc(), 1), TextbookClassification::getSortNumber);

        return textbookClassificationMapper.selectList(lambdaQueryWrapper);

    }

    @Override
    public List<TextbookClassification> buildDictTree(List<TextbookClassification> list) {
        // 将分类按照父ID分组
        Map<Long, List<TextbookClassification>> map = list.stream()
                .collect(Collectors.groupingBy(TextbookClassification::getParentId));

        // 为每个分类设置子分类
        list.forEach(item -> item.setChildren(map.get(item.getId())));

        // 找到所有根节点（假设根节点的父ID为0）
        List<TextbookClassification> roots = list.stream()
                .filter(item -> item.getParentId() == 0)
                .collect(Collectors.toList()); // 将所有根节点收集到一个列表中

        return roots;
    }

    @Override
    public boolean updateTextbookClassificationSortName(Long id, Integer param) {
        TextbookClassification currentTag = textbookClassificationMapper.selectById(id);
        // 根据param决定是向上还是向下调整
        boolean isNext = param != 0;
        TextbookClassification adjacentTag = getAdjacentClassifiaction(currentTag, isNext);
        if (adjacentTag == null) {
            return false;
        }
        // 交换sortNumber
        Integer tempSortNumber = currentTag.getSortNumber();
        currentTag.setSortNumber(adjacentTag.getSortNumber());
        adjacentTag.setSortNumber(tempSortNumber);
        // 更新数据库
        textbookClassificationMapper.updateById(currentTag);
        textbookClassificationMapper.updateById(adjacentTag);
        return true;
    }

    @Override
    public List<TextbookClassification> selectTopLevelTextbookClassification(TopLevelTextbookClassificationSearchParam param) {
        MyLambdaQueryWrapper<TextbookClassification> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TextbookClassification::getClassificationGrade, 1);
        return textbookClassificationMapper.selectList(lambdaQueryWrapper);
    }

    public TextbookClassification getAdjacentClassifiaction(TextbookClassification currentTag, boolean isNext) {
        LambdaQueryWrapper<TextbookClassification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TextbookClassification::getParentId, currentTag.getParentId());
        // 根据 isNext 决定查询前一条还是后一条记录
        if (isNext) {
            queryWrapper.gt(TextbookClassification::getSortNumber, currentTag.getSortNumber())
                    .orderByAsc(TextbookClassification::getSortNumber);
        } else {
            queryWrapper.lt(TextbookClassification::getSortNumber, currentTag.getSortNumber())
                    .orderByDesc(TextbookClassification::getSortNumber);
        }

        // 使用 Page 对象限制查询结果为 1 条
        Page<TextbookClassification> page = new Page<>(1, 1);
        page = textbookClassificationMapper.selectPage(page, queryWrapper);

        // 获取查询结果
        List<TextbookClassification> records = page.getRecords();
        if (records.isEmpty()) {
            return null; // 如果没有找到相邻记录，则返回 null
        }
        return records.get(0); // 返回查询到的相邻记录
    }


    public List<Long> selectTextbookClassificationSubtreeIdList(Long rootId) {
        if (rootId == null) return Collections.emptyList();

        // 只查必要字段，减少反序列化 & 传输
        List<TextbookClassification> all = textbookClassificationMapper.selectList(
                new MyLambdaQueryWrapper<TextbookClassification>()
                        .select(TextbookClassification::getId, TextbookClassification::getParentId)
        );

        // parent_id -> [child_id...]
        Map<Long, List<Long>> childrenMap = all.stream()
                .collect(Collectors.groupingBy(
                        TextbookClassification::getParentId,
                        Collectors.mapping(TextbookClassification::getId, Collectors.toList())
                ));

        List<Long> result = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();

        // 根也要
        result.add(rootId);
        seen.add(rootId);
        stack.push(rootId);

        // DFS（你喜欢BFS也行，换成队列即可）
        while (!stack.isEmpty()) {
            Long cur = stack.pop();
            List<Long> kids = childrenMap.getOrDefault(cur, Collections.emptyList());
            for (Long kid : kids) {
                if (kid != null && seen.add(kid)) {
                    result.add(kid);
                    stack.push(kid);
                }
            }
        }
        return result;  // [rootId, ...所有子孙...]
    }


}
