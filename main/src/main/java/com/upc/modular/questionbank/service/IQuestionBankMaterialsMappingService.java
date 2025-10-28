package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.questionbank.controller.param.QuestionBankMaterialsPageParam;
import com.upc.modular.questionbank.entity.QuestionBankMaterialsMapping;

import java.util.List;

/**
 * <p>
 * 题库素材关联服务类
 * </p>
 *
 * @author cyy
 * @since 2025-10-27
 */
public interface IQuestionBankMaterialsMappingService extends IService<QuestionBankMaterialsMapping> {

    /**
     * 根据题库ID查询关联的素材列表
     *
     * @param questionBankId 题库ID
     * @return 素材列表
     */
    List<QuestionBankMaterialsMapping> getMaterialsByQuestionBankId(Long questionBankId);

    /**
     * 批量添加题库素材关联
     *
     * @param questionBankId 题库ID
     * @param materialIds 素材ID列表
     * @return 是否成功
     */
    boolean batchAddMaterials(Long questionBankId, List<Long> materialIds);

    /**
     * 批量删除题库素材关联
     *
     * @param questionBankId 题库ID
     * @param materialIds 素材ID列表
     * @return 是否成功
     */
    boolean batchRemoveMaterials(Long questionBankId, List<Long> materialIds);

    /**
     * 更新题库素材关联（先删除旧的，再添加新的）
     *
     * @param questionBankId 题库ID
     * @param materialIds 素材ID列表
     * @return 是否成功
     */
    boolean updateMaterials(Long questionBankId, List<Long> materialIds);
    
    /**
     * 分页查询题库素材关联列表
     *
     * @param param 查询参数
     * @return 分页结果
     */
    Page<QuestionBankMaterialsMapping> selectMaterialsPageList(QuestionBankMaterialsPageParam param);
}

