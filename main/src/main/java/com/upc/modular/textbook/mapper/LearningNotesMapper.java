package com.upc.modular.textbook.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.auth.param.UserRoleListPageReturnParam;
import com.upc.modular.textbook.entity.LearningNotes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.LearningNotesPageReturnParam;
import com.upc.modular.textbook.param.LearningNotesPageSearchParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Mapper
public interface LearningNotesMapper extends BaseMapper<LearningNotes> {
    Page<LearningNotesPageReturnParam> getPage(@Param("page") Page<LearningNotesPageReturnParam> page, @Param("param") LearningNotesPageSearchParam param);

    Page<LearningNotesPageReturnParam> getMyPage(@Param("page") Page<LearningNotesPageReturnParam> page, @Param("param") LearningNotesPageSearchParam param, @Param("id") Long id);

    Page<LearningNotesPageReturnParam> getMyNotesTextbookCenter(@Param("page") Page<LearningNotesPageReturnParam> page, @Param("param") LearningNotesPageSearchParam param, @Param("id") Long id);
}
