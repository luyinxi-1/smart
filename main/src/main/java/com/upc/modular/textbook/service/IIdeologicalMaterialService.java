package com.upc.modular.textbook.service;

import com.upc.modular.textbook.entity.IdeologicalMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.IdeologicalMaterialBatchUpdateCatalogParam;
import com.upc.modular.textbook.param.IdeologicalMaterialInsertAndUpdateParam;
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

    Long insertIdeologicalMaterial(IdeologicalMaterialInsertAndUpdateParam param);

    void updateIdeologicalMaterialById(IdeologicalMaterialInsertAndUpdateParam param);

    List<IdeologicalMaterial> getIdeologicalMaterialByConditions(IdeologicalMaterialSearchParam param);
    
    /**
     * 批量更新教学思政的章节ID
     * @param param 批量更新参数
     */
    void batchUpdateCatalog(IdeologicalMaterialBatchUpdateCatalogParam param);
}