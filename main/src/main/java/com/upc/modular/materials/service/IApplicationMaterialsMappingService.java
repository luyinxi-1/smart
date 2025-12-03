package com.upc.modular.materials.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.materials.entity.ApplicationMaterialsMapping;

import java.util.List;

public interface IApplicationMaterialsMappingService
        extends IService<ApplicationMaterialsMapping> {

    /**
     * 【客户端】根据教材ID查询该教材下所有应用素材-教学素材关联记录
     */
    List<ApplicationMaterialsMapping> listByTextbookId(Long textbookId);
}

