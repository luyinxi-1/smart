package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.modular.textbook.entity.MaterialPush;
import com.upc.modular.textbook.param.MaterialPushPageSearchParam;
import com.upc.modular.textbook.param.PushMaterialBatchUpdateCatalogParam;
import com.upc.modular.textbook.param.PushMaterialInsertAndUpdateParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author MJ
 * @since 2025-10-29
 */
public interface IMaterialPushService extends IService<MaterialPush> {

    Long insertPushMaterial(PushMaterialInsertAndUpdateParam param);

    void deleteIdeologicalMaterialByIds(List<Long> ids);

    PushMaterialInsertAndUpdateParam getMaterialById(Long id);

    PageBaseReturnParam<MaterialPush> getPushMaterialByTextbookIdPage(MaterialPushPageSearchParam param);

    void updatePushMaterialById(PushMaterialInsertAndUpdateParam param);

    void batchUpdateCatalog(List<PushMaterialBatchUpdateCatalogParam> params);
}