package com.upc.modular.textbook.mapper;

import com.upc.modular.textbook.entity.LearningLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-14
 */
@Mapper
public interface LearningLogMapper extends BaseMapper<LearningLog> {

    List<RecentStudyReturnParam> recentStudy(@Param("userId") Long userId,
                                             @Param("limit") Integer limit);

    /**
     * 获取教材的总章节数
     */
    List<Map<String, Object>> getTextbookTotalChapters(@Param("textbookIds") List<Long> textbookIds);

    /**
     * 获取学生已读章节数
     */
    List<Map<String, Object>> getStudentReadChapters(@Param("userId") Long userId, @Param("textbookIds") List<Long> textbookIds);
}
