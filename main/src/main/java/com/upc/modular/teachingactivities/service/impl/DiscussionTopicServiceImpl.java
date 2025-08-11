package com.upc.modular.teachingactivities.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingactivities.param.DiscussionTopicReturnParam;
import com.upc.modular.teachingactivities.param.DiscussionTopicSearchParam;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.mapper.DiscussionTopicMapper;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookCatalog;
import com.upc.modular.textbook.service.ITextbookCatalogService;
import com.upc.modular.textbook.service.ITextbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
//        if (StringUtils.isBlank(discussionTopic.getTopicTitle()) || StringUtils.isBlank(discussionTopic.getTopicContent())){
//            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR, "标题或内容不能为空");
//        }
        this.updateById(discussionTopic);
    }

    @Autowired
    private ITextbookService textbookService;
    @Autowired
    private ITextbookCatalogService textbookCatalogService;
    @Autowired
    private DiscussionTopicReplyServiceImpl discussionTopicReplyService;

    @Override
    public List<DiscussionTopicReturnParam> getDiscussionTopicList(DiscussionTopicSearchParam param) {
        LambdaQueryWrapper<DiscussionTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(param.getTopicTitle()), DiscussionTopic::getTopicTitle, param.getTopicTitle());
        queryWrapper.eq(param.getType() != null, DiscussionTopic::getType, param.getType());
        queryWrapper.eq(param.getTextbookId() != null, DiscussionTopic::getTextbookId, param.getTextbookId());
        queryWrapper.eq(param.getTextbookCatalogId() != null, DiscussionTopic::getTextbookCatalogId, param.getTextbookCatalogId());

        List<DiscussionTopic> topicList = this.list(queryWrapper);
        if (topicList.isEmpty()) {
            return new ArrayList<>();
        }

        List<DiscussionTopicReturnParam> returnList = new ArrayList<>();
        for (DiscussionTopic discussionTopic : topicList) {
            DiscussionTopicReturnParam discussionTopicReturnParam = new DiscussionTopicReturnParam();

            if (discussionTopic.getTextbookId() != null) {
                Textbook textbook = textbookService.getById(discussionTopic.getTextbookId());
                discussionTopicReturnParam.setTextbookName(textbook.getTextbookName());
            }
            if (discussionTopic.getTextbookCatalogId() != null) {
                TextbookCatalog textbookCatalog = textbookCatalogService.getById(discussionTopic.getTextbookCatalogId());
                discussionTopicReturnParam.setTextbookCatalogName(textbookCatalog.getCatalogName());
            }

            Integer topicReplyCount = discussionTopicReplyService.getTopicReplyCount(discussionTopic.getId());
            if (topicReplyCount != null) {
                discussionTopicReturnParam.setReplyCount(topicReplyCount);
            }

            returnList.add(discussionTopicReturnParam);
        }


        return returnList;
    }


}
