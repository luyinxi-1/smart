package com.upc.modular.teachingactivities.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.modular.teachingactivities.param.*;
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

    Long insertDiscussionTopic(DiscussionTopic discussionTopic);

    void updateDiscussionTopicById(DiscussionTopic discussionTopic);

    Page<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param);

    List<MyJoinDiscussionTopicDiscussionTopicReturnParam> selectMyJoinDiscussionTopic(MyJoinDiscussionTopicSearchParam param);

    DiscussionTopicSecondReturnParam getSecondTextbookById(Long id);
    
    /**
     * 批量更新教学活动的章节ID
     * @param param 批量更新参数
     */
    void batchUpdateCatalog(List<DiscussionTopicBatchUpdateCatalogParam> param);
}