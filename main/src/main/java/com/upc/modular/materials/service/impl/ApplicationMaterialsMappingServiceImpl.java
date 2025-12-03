package com.upc.modular.materials.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.materials.entity.ApplicationMaterialsMapping;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import com.upc.modular.materials.mapper.ApplicationMaterialsMappingMapper;
import com.upc.modular.materials.mapper.ApplicationMaterialsTextbookMappingMapper;
import com.upc.modular.materials.service.IApplicationMaterialsMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ApplicationMaterialsMappingServiceImpl
        extends ServiceImpl<ApplicationMaterialsMappingMapper, ApplicationMaterialsMapping>
        implements IApplicationMaterialsMappingService {


    @Autowired
    private ApplicationMaterialsTextbookMappingMapper applicationMaterialsTextbookMappingMapper;


    @Override
    public List<ApplicationMaterialsMapping> listByTextbookId(Long textbookId) {
        if (textbookId == null) {
            return Collections.emptyList();
        }

        // 1. 通过 textbookId 查出所有应用素材ID
        List<ApplicationMaterialsTextbookMapping> tbMappings =
                applicationMaterialsTextbookMappingMapper.selectList(
                        new LambdaQueryWrapper<ApplicationMaterialsTextbookMapping>()
                                .eq(ApplicationMaterialsTextbookMapping::getTextbookId, textbookId)
                );
        if (CollectionUtils.isEmpty(tbMappings)) {
            return Collections.emptyList();
        }

        List<Long> appIds = tbMappings.stream()
                .map(ApplicationMaterialsTextbookMapping::getApplicationMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (appIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 用这些应用素材ID，在 mapping 表中查所有关联记录
        return this.list(new LambdaQueryWrapper<ApplicationMaterialsMapping>()
                .in(ApplicationMaterialsMapping::getApplicationMaterialId, appIds));
    }
}
