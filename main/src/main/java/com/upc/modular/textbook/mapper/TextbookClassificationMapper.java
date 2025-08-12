package com.upc.modular.textbook.mapper;

import com.upc.modular.textbook.entity.TextbookClassification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-08-12
 */
@Mapper
public interface TextbookClassificationMapper extends BaseMapper<TextbookClassification> {

    Integer selectMaxSortNumber(TextbookClassification param);

    boolean updateProductClassification(TextbookClassification param);
}
