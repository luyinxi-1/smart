package com.upc.modular.institution.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.param.InstitutionDto;
import com.upc.modular.institution.param.InstitutionSearchParam;
import com.upc.modular.institution.entity.Institution;
import com.upc.modular.institution.mapper.InstitutionMapper;
import com.upc.modular.institution.service.IInstitutionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-01
 */
@Service
public class InstitutionServiceImpl extends ServiceImpl<InstitutionMapper, Institution> implements IInstitutionService {

    @Override
    public void deleteInstitutionByIds(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, " ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void updateInstitutionById(Institution institution) {
        if (institution == null || institution.getId() == null || institution.getId() == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.updateById(institution);
    }

    @Override
    public R<List<InstitutionDto>> getInstitutionByConditions(InstitutionSearchParam param) {
        LambdaQueryWrapper<Institution> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getInstitutionName()), Institution::getInstitutionName, param.getInstitutionName());
        queryWrapper.eq(param.getInstitutionGrade() != null, Institution::getInstitutionGrade, param.getInstitutionGrade());
        queryWrapper.eq(param.getStatus() != null, Institution::getStatus, param.getStatus());
        queryWrapper.in(Institution::getInstitutionGrade, 0, 1, 2, 3, 4, 6);
        queryWrapper.orderBy(true, true, Institution::getSort);
        List<Institution> institutionList = this.list(queryWrapper);

        List<InstitutionDto> institutionDtos = institutionList.stream().map(item -> {
            InstitutionDto dto = new InstitutionDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());

        Map<Long, InstitutionDto> institutionMap = new HashMap<>();
        List<InstitutionDto> rootList = new ArrayList<>();

        Map<Long, Long> parentMap = institutionList.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(
                        Institution::getId,
                        item -> item.getFatherInstitutionId() == null ? 0L : item.getFatherInstitutionId()
                ));

        // 0.检测是否存在循环嵌套
        for (Institution inst : institutionList) {
            if (hasCycle(inst.getId(), parentMap, new HashSet<>())) {
                return R.fail("存在循环嵌套，构建失败，请检查机构父子关系！");
            }
        }

        // 1.先将所有机构按 id 放入 map 中，便于后续查找父节点
        for (InstitutionDto institution : institutionDtos) {
            institution.setChildren(new ArrayList<>()); // 确保 children 不为 null
            institutionMap.put(institution.getId(), institution);
        }

        // 2.构建树结构
        for (InstitutionDto institution : institutionDtos) {
            Long parentId = institution.getFatherInstitutionId();
            if (parentId == null || parentId == 0) {
                // 如果没有父级，则为根节点（顶级机构）
                rootList.add(institution);
            } else {
                InstitutionDto parent = institutionMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(institution);
                } else {
                    // 如果找不到父节点，也作为根节点放入（视项目需求而定）
                    rootList.add(institution);
                }
            }
        }

        return R.ok(rootList);
    }

    private boolean hasCycle(Long nodeId, Map<Long, Long> parentMap, Set<Long> visited) {
        Set<Long> path = new HashSet<>();
        while (nodeId != null && nodeId != 0) {
            if (path.contains(nodeId)) {
                return true; // 检测到环
            }
            path.add(nodeId);
            nodeId = parentMap.get(nodeId);
        }
        return false;
    }

    /**
     * 如果institutionId这个机构是targetInstitutionId这个机构的同级或子类机构则返回true，反之返回false
     * @param institutionId
     * @param targetInstitutionId
     * @return
     */
    public boolean judgeInclusion(Long institutionId, Long targetInstitutionId) {
        // 1. 参数校验
        if (institutionId == null || targetInstitutionId == null) {
            return false;
        }
        // 2. 如果两个ID相同，则直接返回true
        if (institutionId.equals(targetInstitutionId)) {
            return true;
        }
        // 3. 循环向上追溯父节点
        Long currentId = institutionId;
        // 使用一个集合来防止因数据问题导致的无限循环（虽然您已有全局循环检测，但单个方法也应健壮）
        Set<Long> visitedIds = new HashSet<>();
        while (currentId != null && currentId != 0L) {
            // 防止死循环
            if (!visitedIds.add(currentId)) {
                return false;
            }
            Institution currentInstitution = this.getById(currentId);
            if (currentInstitution == null) {
                // 如果在向上查找的过程中找不到某个机构实体，说明链路断裂
                return false;
            }
            Long parentId = currentInstitution.getFatherInstitutionId();
            // 4. 检查父ID是否是目标ID
            if (targetInstitutionId.equals(parentId)) {
                return true;
            }
            // 5. 更新当前ID为父ID，继续向上查找
            currentId = parentId;
        }
        // 6. 如果循环结束仍未找到，说明不包含
        return false;
    }
}
