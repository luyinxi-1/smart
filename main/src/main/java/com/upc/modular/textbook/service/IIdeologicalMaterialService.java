package com.upc.modular.textbook.service;

import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.IdeologicalMaterialSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
public interface IIdeologicalMaterialService extends IService<IdeologicalMaterial> {

    void deleteIdeologicalMaterialByIds(List<Long> ids);

    void insertIdeologicalMaterial(IdeologicalMaterial ideologicalMaterial);

    void updateIdeologicalMaterialById(IdeologicalMaterial ideologicalMaterial);

    List<IdeologicalMaterial> getIdeologicalMaterialByConditions(IdeologicalMaterialSearchParam param);
}
