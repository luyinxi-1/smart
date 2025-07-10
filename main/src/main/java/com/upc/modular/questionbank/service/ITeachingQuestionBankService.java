package com.upc.modular.questionbank.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageReturnParam;
import com.upc.modular.questionbank.controller.param.TeachingQuestionBankPageSearchParam;
import com.upc.modular.questionbank.entity.TeachingQuestionBank;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-04
 */
public interface ITeachingQuestionBankService extends IService<TeachingQuestionBank> {

    Void deleteQuestionBankByIds(IdParam idParam);

    Page<TeachingQuestionBankPageReturnParam> selectQuestionPage(TeachingQuestionBankPageSearchParam param);

    void inserQuestionBank(TeachingQuestionBank teachingQuestionbank);

    void updateQuestionBank(TeachingQuestionBank teachingQuestionbank);
}
