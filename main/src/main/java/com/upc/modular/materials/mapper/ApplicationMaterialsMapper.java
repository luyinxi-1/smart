package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.ApplicationMaterialsPageParam;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsVO;
import com.upc.modular.materials.entity.ApplicationMaterials;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 应用素材Mapper接口
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Mapper
public interface ApplicationMaterialsMapper extends BaseMapper<ApplicationMaterials> {

    /**
     * 分页查询应用素材列表
     *
     * @param page 分页对象
     * @param param 查询参数
     * @return 分页结果
     */
    Page<ApplicationMaterialsVO> selectApplicationMaterialsPage(Page<ApplicationMaterialsVO> page, @Param("param") ApplicationMaterialsPageParam param);
    
    /**
     * 根据ID查询应用素材详情（包括创建者、操作者名称等信息）
     *
     * @param id 应用素材ID
     * @return 应用素材详情
     */
    ApplicationMaterialsVO selectApplicationMaterialsById(@Param("id") Long id);


    boolean updateByApplicationMaterialId(ApplicationMaterials applicationMaterials);
}
