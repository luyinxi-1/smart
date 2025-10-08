package com.upc.modular.teachingactivities.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.exception.BusinessErrorEnum;
import com.upc.exception.BusinessException;
import com.upc.modular.teachingactivities.param.*;
import com.upc.modular.teachingactivities.entity.DiscussionTopic;
import com.upc.modular.teachingactivities.service.IDiscussionTopicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/discussion-topic")
@Api(tags = "教学活动讨论话题")
public class DiscussionTopicController {

    @Autowired
    private IDiscussionTopicService discussionTopicService;

    @ApiOperation(value = "删除教学活动讨论话题")
    @PostMapping("/deleteDiscussionTopicByIds")
    public R deleteDiscussionTopicByIds(@RequestBody List<Long> ids) {
        discussionTopicService.deleteDiscussionTopicByIds(ids);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "新增教学活动讨论话题")
    @PostMapping("/insertDiscussionTopic")
    public R<Long> insertDiscussionTopic(@RequestBody DiscussionTopic discussionTopic) {
        Long id = discussionTopicService.insertDiscussionTopic(discussionTopic);
        return R.ok(id);
    }

    @ApiOperation(value = "查询教学活动讨论话题信息")
    @PostMapping("/getDiscussionTopicById")
    public R getDiscussionTopicById(@RequestParam Long id) {
        if (id == null || id == 0L) {
            throw new BusinessException(BusinessErrorEnum.PARAMETER_VALIDATION_ERROR);
        }
        DiscussionTopicSecondReturnParam discussionTopicReturnParam = discussionTopicService.getSecondTextbookById(id);
        return R.commonReturn(200, "查询成功", discussionTopicReturnParam);
    }

    @ApiOperation(value = "修改教学活动讨论话题")
    @PostMapping("/updateDiscussionTopicById")
    public R updateDiscussionTopicById(@RequestBody DiscussionTopic discussionTopic) {
        discussionTopicService.updateDiscussionTopicById(discussionTopic);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "按条件查询教学活动讨论话题")
    @PostMapping("/getDiscussionTopicList")
    public R<Page<DiscussionTopicReturnParam>> getDiscussionTopicList(@RequestBody DiscussionTopicSearchParam param) {
        Page<DiscussionTopicReturnParam> discussionTopicList = discussionTopicService.getDiscussionTopicList(param);
        return R.ok(discussionTopicList);
    }

    @ApiOperation(value = "查看我能参与的教学活动")
    @PostMapping("/selectMyJoinDiscussionTopic")
    public R<List<MyJoinDiscussionTopicDiscussionTopicReturnParam>> selectMyJoinDiscussionTopic(@RequestBody MyJoinDiscussionTopicSearchParam param) {
        List<MyJoinDiscussionTopicDiscussionTopicReturnParam> resultList = discussionTopicService.selectMyJoinDiscussionTopic(param);
        return R.ok(resultList);
    }
    
    @ApiOperation(value = "批量更新教学活动章节ID")
    @PostMapping("/batchUpdateCatalog")
    public R batchUpdateDiscussionTopicCatalog(@RequestBody DiscussionTopicBatchUpdateCatalogParam param) {
        discussionTopicService.batchUpdateCatalog(param);
        return R.commonReturn(200, "更新成功", "");
    }
}