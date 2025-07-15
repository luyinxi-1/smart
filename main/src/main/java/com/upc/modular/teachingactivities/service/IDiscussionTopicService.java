package com.upc.modular.teachingactivities.service;

import com.upc.modular.teachingactivities.param.DiscussionTopicSearchParam;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.baomidou.mybatisplus.extension.service.IService;

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

    List<DiscussionTopic> getDiscussionTopicList(DiscussionTopicSearchParam param);
}
