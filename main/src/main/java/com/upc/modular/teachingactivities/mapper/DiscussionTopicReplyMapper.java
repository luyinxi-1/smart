package com.upc.modular.teachingactivities.mapper;

import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
