package com.upc.modular.teachingActivities.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teachingActivities.entity.DiscussionTopicReply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.upc.modular.teachingActivities.param.*;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
public interface IDiscussionTopicReplyService extends IService<DiscussionTopicReply> {
    void insert(DiscussionTopicReply reply);
    void deleteDictItemByIds(IdParam idParam);

    Page<DiscussionTopicMyPageReturnParam> getMyReply(DiscussionTopicMyPageSearchParam param);

    Page<DiscussionTopicReplyPageReturnParam> getReply(DiscussionTopicReplyPageSearchParam param);

    Page<DiscussionTopicSecondReplyPageReturnParam> getSecondReply(DiscussionTopicSecondReplyPageSearchParam param);

    R<DiscussionTopicMyReturnParam> getMyReplyContent(DiscussionTopicMySearchParam param);
}
