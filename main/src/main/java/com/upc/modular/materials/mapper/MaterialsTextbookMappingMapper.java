package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.MaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.MaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.entity.MaterialsTextbookMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mjh
 * @since 2025-08-23
 */
@Mapper
public interface MaterialsTextbookMappingMapper extends BaseMapper<MaterialsTextbookMapping> {

    Page<MaterialsTextbookMappingReturnParam> getPage(@Param("param") MaterialsTextbookMappingPageSearchParam param, Page<MaterialsTextbookMappingReturnParam> page);

}
