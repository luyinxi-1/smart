package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.entity.Textbook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-08
 */
@Mapper
public interface TextbookMapper extends BaseMapper<Textbook> {

    Page<TextbookPageReturnParam> selectTextbookPage(@Param("page") Page<TextbookPageReturnParam> page, @Param("param")TextbookPageSearchParam param);
}
