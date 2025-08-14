package com.upc.modular.questionbank.mapper;

import com.upc.modular.questionbank.entity.TeachingQuestionClassification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author la
 * @since 2025-08-12
 */
@Mapper
public interface TeachingQuestionClassificationMapper extends BaseMapper<TeachingQuestionClassification> {

    Integer selectMaxSortNumber(TeachingQuestionClassification param);

    boolean updateTeachingQuestionClassification(TeachingQuestionClassification param);
}
