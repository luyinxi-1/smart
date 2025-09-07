package com.upc.modular.materials.mapper;

import com.upc.modular.materials.controller.param.vo.MaterialsTextbookNameMappingReturnParam;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author mjh
 * @since 2025-07-17
 */
@Mapper
public interface TeachingMaterialsMapper extends BaseMapper<TeachingMaterials> {

    MaterialsTextbookNameMappingReturnParam getMaterialIdToTextbookNameMap(@Param("ids") List<Long> ids);

}
