package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.param.TextbookReviewReturnParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fwx
 * @since 2025-08-12
 */
@Mapper
public interface TextbookReviewMapper extends BaseMapper<TextbookReview> {
    Page<TextbookReviewReturnParam> selectPageWithUserNames(Page<TextbookReviewReturnParam> page, @Param("textbookId") Long textbookId);

    TextbookReviewReturnParam selectByIdWithUserNames(@Param("id") Long id);
}
