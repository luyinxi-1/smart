package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsTextbookMappingPageSearchParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsTextbookMappingReturnParam;
import com.upc.modular.materials.entity.ApplicationMaterialsTextbookMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 应用素材与教材关联Mapper接口
 * </p>
 *
 * @author system
 * @since 2025-10-31
 */
@Mapper
public interface ApplicationMaterialsTextbookMappingMapper extends BaseMapper<ApplicationMaterialsTextbookMapping> {

    /**
     * 分页查询应用素材教材绑定
     *
     * @param param 查询参数
     * @param page  分页参数
     * @return 分页结果
     */
    Page<ApplicationMaterialsTextbookMappingReturnParam> getPage(
            @Param("param") ApplicationMaterialsTextbookMappingPageSearchParam param,
            Page<ApplicationMaterialsTextbookMappingReturnParam> page);
}

