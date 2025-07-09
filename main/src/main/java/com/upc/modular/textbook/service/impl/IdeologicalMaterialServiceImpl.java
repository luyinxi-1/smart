package com.upc.modular.textbook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingActivities.entity.DiscussionTopic;
import com.upc.modular.teachingActivities.param.DiscussionTopicSearchParam;
import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.mapper.IdeologicalMaterialMapper;
import com.upc.modular.textbook.param.IdeologicalMaterialSearchParam;
import com.upc.modular.textbook.service.IIdeologicalMaterialService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Service
public class IdeologicalMaterialServiceImpl extends ServiceImpl<IdeologicalMaterialMapper, IdeologicalMaterial> implements IIdeologicalMaterialService {

    @Autowired
    private ITextbookCatalogService textbookCatalogService;

    @Override
    public void deleteIdeologicalMaterialByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void insertIdeologicalMaterial(IdeologicalMaterial ideologicalMaterial) {
        if (ideologicalMaterial == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.save(ideologicalMaterial);
    }

    @Override
    public void updateIdeologicalMaterialById(IdeologicalMaterial ideologicalMaterial) {
        if (ideologicalMaterial == null || ideologicalMaterial.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        this.updateById(ideologicalMaterial);
    }

    @Override
    public List<IdeologicalMaterial> getIdeologicalMaterialByConditions(IdeologicalMaterialSearchParam param) {
        LambdaQueryWrapper<IdeologicalMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getType()), IdeologicalMaterial::getType, param.getType());
        queryWrapper.like(StringUtils.isNotBlank(param.getName()), IdeologicalMaterial::getName, param.getName());
        queryWrapper.eq(param.getTextbookId() != null, IdeologicalMaterial::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, IdeologicalMaterial::getTextbookCatalogId, param.getTextbookCatalogId());

        List<IdeologicalMaterial> ideologicalMaterialList = this.list(queryWrapper);

        if (ideologicalMaterialList.isEmpty()) {
            return ideologicalMaterialList;
        }

        // 构造临时结构体：包装排序字段
        List<Pair<IdeologicalMaterial, Integer>> wrappedList = new ArrayList<>();

        for (IdeologicalMaterial material : ideologicalMaterialList) {
            Integer sort = 0;
            if (material.getTextbookCatalogId() != null && material.getTextbookCatalogId() != 0L) {
                TextbookCatalog catalog = textbookCatalogService.getById(material.getTextbookCatalogId());
                if (catalog != null && catalog.getSort() != null) {
                    sort = catalog.getSort();
                }
            }
            wrappedList.add(Pair.of(material, sort));
        }

        // 根据 sort 排序
        wrappedList.sort(Comparator.comparingInt(Pair::getRight));

        List<IdeologicalMaterial> resultIdeologicalMaterials = wrappedList.stream().map(Pair::getLeft).collect(Collectors.toList());

        return resultIdeologicalMaterials;
    }
}
