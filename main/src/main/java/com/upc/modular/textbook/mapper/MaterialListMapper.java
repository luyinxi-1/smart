package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.entity.MaterialList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author JM
 * @since 2025-10-29
 */
@Mapper
public interface MaterialListMapper extends BaseMapper<MaterialList> {


        List<MaterialList> selectByTextbookId(@Param("textbookId") Long textbookId);

}
