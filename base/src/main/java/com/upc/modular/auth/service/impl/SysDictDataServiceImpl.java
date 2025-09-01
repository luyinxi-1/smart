package com.upc.modular.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.wrapper.MyLambdaQueryWrapper;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemPageSearchParam;
import com.upc.modular.auth.controller.param.SysDictItemParam.SysDictItemSearchParam;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.auth.entity.SysDictData;
import com.upc.modular.auth.mapper.SysDictItemMapper;
import com.upc.modular.auth.service.ISysDictDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
public class SysDictDataServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictData> implements ISysDictDataService {

    @Autowired
    private SysDictItemMapper sysDictItemMapper;
    @Override
    public boolean insertDictItem(SysDictData dictItem) {
        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(ObjectUtils.isNotEmpty(dictItem.getDictType()), SysDictData::getDictType, dictItem.getDictType());

        if (ObjectUtils.isNotEmpty(dictItem.getDictKey()) || ObjectUtils.isNotEmpty(dictItem.getName())) {
            lambdaQueryWrapper.and(w -> w
                    .eq(ObjectUtils.isNotEmpty(dictItem.getDictKey()), SysDictData::getDictKey, dictItem.getDictKey())
                    .or()
                    .eq(ObjectUtils.isNotEmpty(dictItem.getName()), SysDictData::getName, dictItem.getName())
            );
        }
        List<SysDictData> sysDictData = sysDictItemMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtils.isNotEmpty(sysDictData)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "新增字典内容'" + dictItem.getDictKey() + "'失败，字典内容value值已存在");
        }
        sysDictItemMapper.insert(dictItem);
        return true;
    }

    @Override
    @Transactional
    public void deleteDictItemByIds(IdParam idParam) {
        List<Long> initialIdList = idParam.getIdList();
        if (CollectionUtils.isEmpty(initialIdList)) {
            return;
        }

        // 1. 获取数据库中的所有字典项，以在内存中构建父子关系。
        List<SysDictData> allNodes = sysDictItemMapper.selectList(null);
        if (CollectionUtils.isEmpty(allNodes)) {
            return;
        }

        Map<Long, List<SysDictData>> childrenMap = allNodes.stream()
                .filter(node -> node.getParentId() != null && node.getParentId() != 0L)
                .collect(Collectors.groupingBy(SysDictData::getParentId));

        // 2. 递归查找所有需要删除的ID（包括所有子孙节点）
        Set<Long> allIdsToDelete = new HashSet<>(initialIdList);
        for (Long id : initialIdList) {
            collectAllDescendantIds(id, childrenMap, allIdsToDelete);
        }

        // 3. 执行一次性的批量删除
        sysDictItemMapper.deleteBatchIds(allIdsToDelete);
    }

    /**
     * Recursively collects all descendant IDs for a given parent node.
     * @param parentId The ID of the parent node to start from.
     * @param childrenMap A map containing parent-child relationships.
     * @param idsToDelete The set to which descendant IDs will be added.
     */
    private void collectAllDescendantIds(Long parentId, Map<Long, List<SysDictData>> childrenMap, Set<Long> idsToDelete) {

        List<SysDictData> children = childrenMap.get(parentId);

        if (CollectionUtils.isEmpty(children)) {
            return;
        }

        for (SysDictData child : children) {
            idsToDelete.add(child.getId());
            collectAllDescendantIds(child.getId(), childrenMap, idsToDelete);
        }
    }

    @Override
    public Page<SysDictData> getPage(SysDictItemPageSearchParam param) {
        Page<SysDictData> page = new Page<>(param.getCurrent(), param.getSize());

        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();

        // --- 修改点在这里 ---
        // 如果前端没有传入parentId，则默认为0，查询顶级字典
        Long parentId = Optional.ofNullable(param.getParentId()).orElse(0L);

        lambdaQueryWrapper
                .eq(SysDictData::getDictType, param.getDictType())
                // 新增核心查询条件：根据 parentId 查询
                .eq(SysDictData::getParentId, parentId)
                .like(ObjectUtils.isNotEmpty(param.getName()), SysDictData::getName, param.getName())
                .eq(ObjectUtils.isNotEmpty(param.getStatus()), SysDictData::getStatus, param.getStatus())
                // 建议加上排序，让同一级的字典项顺序固定
                .orderByAsc(SysDictData::getDictSort);

        return this.page(page, lambdaQueryWrapper);
    }

    @Override
    public List<SysDictData> selectDictDataByDictType(SysDictItemSearchParam param) {
        // 1. 获取字典类型下的所有扁平数据
        MyLambdaQueryWrapper<SysDictData> lambdaQueryWrapper = new MyLambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysDictData::getDictType, param.getDictType())
                .orderByAsc(SysDictData::getDictSort);
        List<SysDictData> allNodes = sysDictItemMapper.selectList(lambdaQueryWrapper);

        if (allNodes == null || allNodes.isEmpty()) {
            return new ArrayList<>();
        }

        // 如果搜索关键字为空，则直接构建并返回完整的树
        if (!StringUtils.hasText(param.getName())) {
            return buildTree(allNodes);
        }

        // ================= 树形筛选核心逻辑 =================

        // 2. 标记阶段：找出所有需要保留的节点ID
        Set<Long> idsToKeep = new HashSet<>();
        Map<Long, SysDictData> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(SysDictData::getId, node -> node));

        // 为了快速查找子节点，创建一个父ID到子节点列表的映射
        Map<Long, List<SysDictData>> childrenMap = allNodes.stream()
                .filter(node -> node.getParentId() != 0L)
                .collect(Collectors.groupingBy(SysDictData::getParentId));

        // 遍历所有节点，找到直接匹配的节点
        for (SysDictData node : allNodes) {
            if (node.getName() != null && node.getName().contains(param.getName())) {
                // 找到了一个匹配节点，保留它、它所有的父级、它所有的子孙
                collectAllChildrenIds(node, childrenMap, idsToKeep);
                collectAllParentIds(node, nodeMap, idsToKeep);
            }
        }

        // 3. 重建阶段：根据保留的ID筛选节点，并重新构建树
        List<SysDictData> filteredNodes = allNodes.stream()
                .filter(node -> idsToKeep.contains(node.getId()))
                .collect(Collectors.toList());

        return buildTree(filteredNodes);
    }

    @Override
    public Boolean updateDictData(SysDictData dict) {
        // 校验1：ID不能为空
        if (dict.getId() == null || dict.getId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "更新时ID不能为空");
        }

        // 校验2：获取原始节点数据，检查父节点ID是否有变化
        SysDictData originalNode = this.getById(dict.getId());
        if (originalNode == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "找不到要更新的字典项");
        }

        Long originalParentId = originalNode.getParentId();
        Long newParentId = dict.getParentId();

        // 如果父节点没有变化，或者新的父节点为空（通常设为0），则直接更新
        if (newParentId == null || Objects.equals(originalParentId, newParentId)) {
            return this.updateById(dict);
        }

        // ================= 核心校验逻辑 =================
        // 父节点发生了变化，需要进行移动校验

        // 校验3：节点不能移动到自己下面
        if (dict.getId().equals(newParentId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不能将节点移动到其自身之下");
        }

        // 校验4：节点不能移动到自己的子孙节点下
        // 获取当前节点的所有子孙节点ID
        List<SysDictData> allNodes = sysDictItemMapper.selectList(null);
        Map<Long, List<SysDictData>> childrenMap = allNodes.stream()
                .filter(node -> node.getParentId() != null && node.getParentId() != 0L)
                .collect(Collectors.groupingBy(SysDictData::getParentId));

        Set<Long> descendantIds = new HashSet<>();
        collectAllDescendantIds(dict.getId(), childrenMap, descendantIds);

        if (descendantIds.contains(newParentId)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "不能将节点移动到其子孙节点之下");
        }

        // 所有校验通过，执行更新
        return this.updateById(dict);
    }

    /**
     * 将扁平的节点列表构建成树形结构
     * @param nodes 节点列表
     * @return 树形结构的顶级节点列表
     */
    private List<SysDictData> buildTree(List<SysDictData> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return new ArrayList<>();
        }

        List<SysDictData> treeList = new ArrayList<>();
        Map<Long, SysDictData> map = nodes.stream()
                .peek(node -> node.setChildren(null)) // 重置children，防止重复构建
                .collect(Collectors.toMap(SysDictData::getId, node -> node));

        for (SysDictData node : nodes) {
            if (node.getParentId() == 0L) {
                treeList.add(node);
            } else {
                SysDictData parentNode = map.get(node.getParentId());
                if (parentNode != null) {
                    if (parentNode.getChildren() == null) {
                        parentNode.setChildren(new ArrayList<>());
                    }
                    parentNode.getChildren().add(node);
                }
            }
        }
        return treeList;
    }

    /**
     * 递归收集一个节点及其所有子孙节点的ID
     * @param startNode 起始节点
     * @param childrenMap 父ID -> 子节点列表的映射
     * @param idsToKeep 用于存储ID的Set
     */
    private void collectAllChildrenIds(SysDictData startNode, Map<Long, List<SysDictData>> childrenMap, Set<Long> idsToKeep) {
        idsToKeep.add(startNode.getId());
        List<SysDictData> children = childrenMap.get(startNode.getId());
        if (children != null && !children.isEmpty()) {
            for (SysDictData child : children) {
                // 递归调用
                collectAllChildrenIds(child, childrenMap, idsToKeep);
            }
        }
    }

    /**
     * 向上追溯收集一个节点所有父节点的ID
     * @param startNode 起始节点
     * @param nodeMap ID -> 节点的映射
     * @param idsToKeep 用于存储ID的Set
     */
    private void collectAllParentIds(SysDictData startNode, Map<Long, SysDictData> nodeMap, Set<Long> idsToKeep) {
        idsToKeep.add(startNode.getId());
        Long parentId = startNode.getParentId();
        while (parentId != 0L) {
            SysDictData parentNode = nodeMap.get(parentId);
            if (parentNode != null) {
                idsToKeep.add(parentNode.getId());
                parentId = parentNode.getParentId();
            } else {
                // 找不到父节点，终止循环
                break;
            }
        }
    }
}
