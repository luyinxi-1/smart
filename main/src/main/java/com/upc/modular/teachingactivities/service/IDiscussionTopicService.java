package com.upc.modular.teachingactivities.service;

import com.upc.modular.teachingactivities.param.DiscussionTopicReturnParam;
import com.upc.modular.teachingactivities.param.DiscussionTopicSearchParam;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.teachingactivities.param.MyJoinDiscussionTopicDiscussionTopicReturnParam;
import com.upc.modular.teachingactivities.param.MyJoinDiscussionTopicSearchParam;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
public interface IDiscussionTopicService extends IService<DiscussionTopic> {

    void deleteDiscussionTopicByIds(List<Long> ids);

    void insertDiscussionTopic(DiscussionTopic discussionTopic);

    void updateDiscussionTopicById(DiscussionTopic discussionTopic);

    List<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param);

    List<MyJoinDiscussionTopicDiscussionTopicReturnParam> selectMyJoinDiscussionTopic(MyJoinDiscussionTopicSearchParam param);
}
