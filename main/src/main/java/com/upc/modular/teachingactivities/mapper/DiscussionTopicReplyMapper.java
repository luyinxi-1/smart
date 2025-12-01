package com.upc.modular.teachingactivities.mapper;

import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.teachingactivities.param.DiscussionTopicMyPageReturnParam;
import com.upc.modular.teachingactivities.param.DiscussionTopicReplyPageReturnParam;
import com.upc.modular.teachingactivities.param.DiscussionTopicSecondReplyPageReturnParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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
            @Param("offset") Long offset,
            @Param("userType") Integer userType // 用户类型：0-管理员，1-学生，2-教师
    );

    List<DiscussionTopicMyPageReturnParam> selectMyReplyPage(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("textbookName") String textbookName,
            @Param("isAsc") Integer isAsc,   // 1=升序，其他=降序
            @Param("size") long size,
            @Param("offset") long offset
    );

    // 过滤后的总数（用于分页 total）
    Long countMyReply(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("textbookName") String textbookName
    );
}
