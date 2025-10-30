package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.materials.controller.param.vo.ApplicationMaterialsDetailVO;
import com.upc.modular.materials.entity.ApplicationMaterialsMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 应用素材与教学素材关联Mapper接口
 * </p>
 *
 * @author system
 * @since 2025-10-29
 */
@Mapper
public interface ApplicationMaterialsMappingMapper extends BaseMapper<ApplicationMaterialsMapping> {

    /**
     * 批量插入应用素材与教学素材关联
     *
     * @param mappings 关联列表
     * @return 插入条数
     */
    int batchInsert(@Param("mappings") List<ApplicationMaterialsMapping> mappings);

    /**
     * 根据应用素材ID删除所有关联
     *
     * @param applicationMaterialId 应用素材ID
     * @return 删除条数
     */
    int deleteByApplicationMaterialId(@Param("applicationMaterialId") Long applicationMaterialId);
    
    /**
     * 获取应用素材关联的教学素材详情列表
     *
     * @param applicationMaterialId 应用素材ID
     * @return 教学素材详情列表
     */
    List<ApplicationMaterialsDetailVO> getTeachingMaterialsByApplicationId(@Param("applicationMaterialId") Long applicationMaterialId);
}
