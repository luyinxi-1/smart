package com.upc.modular.textbook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.textbook.entity.LearningNotes;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;
import com.upc.modular.textbook.param.UuidParam;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
public interface ILearningNotesService extends IService<LearningNotes> {

    Boolean insert(LearningNotes learningNotes);

    Boolean batchDelete(IdParam idParam);

    Boolean batchDeleteByUuid(UuidParam uuidParam);

    Boolean updateNotes(LearningNotes param);

    Boolean updateNotesbyClientUuid(LearningNotes param);

    Page<LearningNotesPageReturnParam> getAllPage(LearningNotesPageSearchParam param);

    LearningNotes getOneNote(Long id);

    Page<LearningNotesPageReturnParam> getMyPage(LearningNotesPageSearchParam param);

    Page<LearningNotesPageReturnParam> getMyNotesTextbookCenter(LearningNotesPageSearchParam param);

    /**
     * 根据clientUuid获取学习笔记
     * @param clientUuid 客户端UUID
     * @return 学习笔记对象
     */
    LearningNotes getOneNoteByClientUuid(String clientUuid);

    /**
     * 【新】获取所有未同步的学习笔记ID
     * @return ID列表
     */
    List<Long> getNewNoteIdsForClient();

    /**
     * 【新】根据ID列表获取学习笔记实体
     * @param ids ID列表
     * @return 实体列表
     */
    List<LearningNotes> getNotesByIds(List<Long> ids);

    /**
     * 【新】根据ID列表确认同步状态
     * @param ids ID列表
     * @return 是否成功
     */
    boolean confirmNotesSync(List<Long> ids);
}