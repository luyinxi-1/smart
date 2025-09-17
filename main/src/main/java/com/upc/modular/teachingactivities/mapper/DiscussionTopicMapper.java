package com.upc.modular.teachingactivities.mapper;

import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.upc.modular.teachingactivities.param.MyJoinDiscussionTopicDiscussionTopicReturnParam;
import org.apache.ibatis.annotations.Mapper;

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
public interface DiscussionTopicMapper extends BaseMapper<DiscussionTopic> {

    List<MyJoinDiscussionTopicDiscussionTopicReturnParam> selectWithDetailsByTextbookIds(List<Long> textbookIdList,Integer identityId);
}
