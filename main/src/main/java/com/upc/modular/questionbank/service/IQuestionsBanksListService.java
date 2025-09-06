package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListBatchParam;
import com.upc.modular.questionbank.controller.param.QuestionsBanksListPageSearchParam;
import com.upc.modular.questionbank.entity.QuestionsBanksList;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface IQuestionsBanksListService extends IService<QuestionsBanksList> {

    Void inserQuestionBankList(QuestionsBanksList param);
    /**
     * 批量新增题目题库关联
     * @param param 批量关联参数
     */
    void batchInsertQuestionBankList(QuestionsBanksListBatchParam param);

    void deleteQuestionsBanksListByIds(IdParam idParam);
    /**
     * 批量更新题目题库关联
     * @param param 批量更新参数
     */
    void batchUpdateQuestionsBanksList(QuestionsBanksListBatchParam param);
    void updateQuestionsBanksList(QuestionsBanksList param);

    //Page<QuestionsBanksListWithCreatorDto> selectQuestionPageList(QuestionsBanksListPageSearchParam param);

    Page<QuestionsBanksList> selectQuestionPageList(QuestionsBanksListPageSearchParam param);
}
