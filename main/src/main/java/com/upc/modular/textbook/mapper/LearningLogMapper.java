package com.upc.modular.textbook.mapper;

import com.upc.modular.textbook.entity.LearningLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.textbook.param.RecentStudyReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}
