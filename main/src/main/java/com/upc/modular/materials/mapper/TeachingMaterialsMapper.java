package com.upc.modular.materials.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.materials.controller.param.dto.TeachingMaterialsPageSearchDto;
import com.upc.modular.materials.controller.param.vo.TeachingMaterialsReturnVo;
import com.upc.modular.materials.entity.TeachingMaterials;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.student.controller.param.dto.StudentPageSearchDto;
import com.upc.modular.student.controller.param.vo.StudentReturnVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    Page<TeachingMaterialsReturnVo> getPage(Page<TeachingMaterialsReturnVo> page, @Param("param") TeachingMaterialsPageSearchDto param);

}
