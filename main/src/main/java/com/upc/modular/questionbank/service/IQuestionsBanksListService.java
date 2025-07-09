package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
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

    void deleteQuestionsBanksListByIds(IdParam idParam);

    void updateQuestionsBanksList(QuestionsBanksList param);

    Page<QuestionsBanksList> selectQuestionPageList(QuestionsBanksListPageSearchParam param);
}
