package com.upc.modular.teachingactivities.mapper;

import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.teachingactivities.param.DiscussionTopicReplyPageReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Mapper
public interface DiscussionTopicReplyMapper extends BaseMapper<DiscussionTopicReply> {

    Long countTopicWithReplies(Long topicId);

    Long countRootReplies(@Param("topicId") Long topicId);

    List<DiscussionTopicReplyPageReturnParam> selectReplyPageWithDescCount(
            @Param("topicId") Long topicId,
            @Param("loginUserId") Long loginUserId,
            @Param("order") Integer order,   // 1=按点赞数倒序，否则按时间倒序
            @Param("limit") Long limit,
            @Param("offset") Long offset
    );
}
