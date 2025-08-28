package com.upc.modular.textbook.mapper;

import com.upc.modular.textbook.entity.Textbook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    List<TextbookPageReturnParam> selectTextbookPage(@Param("param")TextbookPageSearchParam param, @Param("classificationIds") List<Long> classificationIds, @Param("userType") Integer userType);

    TextbookPageReturnParam getOneTextbookDetails(@Param("textbookId") Long textbookId, @Param("userType") Integer userType);
}
