package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.entity.Textbook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.TextbookPageReturnParam;
import com.upc.modular.textbook.param.TextbookPageSearchParam;
import com.upc.modular.textbook.param.TextbookQueryReq;
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
    /**
     * 根据动态条件查询教材列表
     * @param req 包含所有查询条件的对象
     * @return 教材VO列表
     */
   //List<Textbook> queryByConditions(Page<Textbook> page, @Param("req") TextbookQueryReq req);
    Page<Textbook> queryByConditions(Page<Textbook> page, @Param("req") TextbookQueryReq req);

}
