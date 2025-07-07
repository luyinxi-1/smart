package com.upc.modular.discussion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.auth.param.DiscussionTopicSearchParam;
import com.upc.modular.discussion.entity.DiscussionTopic;
import com.upc.modular.discussion.mapper.DiscussionTopicMapper;
import com.upc.modular.discussion.service.IDiscussionTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@Service
public class DiscussionTopicServiceImpl extends ServiceImpl<DiscussionTopicMapper, DiscussionTopic> implements IDiscussionTopicService {

    @Override
    public void deleteDiscussionTopicByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "ID列表不能为空");
        }
        this.removeBatchByIds(ids);
    }

    @Override
    public void insertDiscussionTopic(DiscussionTopic discussionTopic) {
        if (discussionTopic == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (StringUtils.isBlank(discussionTopic.getTopicTitle()) || StringUtils.isBlank(discussionTopic.getTopicContent())){
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "标题或内容不能为空");
        }
        this.save(discussionTopic);
    }

    @Override
    public void updateDiscussionTopicById(DiscussionTopic discussionTopic) {
        if (discussionTopic == null || discussionTopic.getId() == null) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        if (StringUtils.isBlank(discussionTopic.getTopicTitle()) || StringUtils.isBlank(discussionTopic.getTopicContent())){
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "标题或内容不能为空");
        }
        this.updateById(discussionTopic);
    }

    @Override
    public List<DiscussionTopic> getDiscussionTopicList(DiscussionTopicSearchParam param) {
        LambdaQueryWrapper<DiscussionTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getTopicTitle()), DiscussionTopic::getTopicTitle, param.getTopicTitle());
        queryWrapper.eq(param.getType() != null, DiscussionTopic::getType, param.getType());
        queryWrapper.eq(param.getTextbookId() != null, DiscussionTopic::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, DiscussionTopic::getTextbookCatalogId, param.getTextbookCatalogId());

        List<DiscussionTopic> resultList = this.list(queryWrapper);

        return resultList;
    }


}
