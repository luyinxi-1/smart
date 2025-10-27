package com.upc.modular.questionbank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialsPageParam;
import com.upc.modular.questionbank.entity.QuestionBankMaterialsMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 题库素材关联Mapper接口
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
@Mapper
public interface QuestionBankMaterialsMappingMapper extends BaseMapper<QuestionBankMaterialsMapping> {

    /**
     * 根据题库ID查询关联的素材列表
     *
     * @param questionBankId 题库ID
     * @return 素材列表
     */
    List<QuestionBankMaterialsMapping> selectMaterialsByQuestionBankId(@Param("questionBankId") Long questionBankId);

    /**
     * 批量插入题库素材关联
     *
     * @param mappings 关联列表
     * @return 插入条数
     */
    int batchInsert(@Param("mappings") List<QuestionBankMaterialsMapping> mappings);

    /**
     * 根据题库ID删除所有关联
     *
     * @param questionBankId 题库ID
     * @return 删除条数
     */
    int deleteByQuestionBankId(@Param("questionBankId") Long questionBankId);
    
    /**
     * 分页查询题库素材关联列表
     *
     * @param page 分页对象
     * @param param 查询参数
     * @return 分页结果
     */
    Page<QuestionBankMaterialsMapping> selectMaterialsPageList(Page<QuestionBankMaterialsMapping> page, @Param("param") QuestionBankMaterialsPageParam param);
}

