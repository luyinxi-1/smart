package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningNotes;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
public interface ILearningNotesService extends IService<LearningNotes> {

    Boolean insert(LearningNotes learningNotes);

    Boolean batchDelete(IdParam idParam);

    Boolean updateNotes(LearningNotes param);

    Page<LearningNotesPageReturnParam> getAllPage(LearningNotesPageSearchParam param);

    LearningNotes getOneNote(Long id);

    Page<LearningNotesPageReturnParam> getMyPage(LearningNotesPageSearchParam param);
}
