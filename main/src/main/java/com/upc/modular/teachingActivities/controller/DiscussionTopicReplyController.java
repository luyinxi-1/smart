package com.upc.modular.teachingActivities.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teachingActivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingActivities.param.*;
import com.upc.modular.teachingActivities.service.IDiscussionTopicReplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author byh
 * @since 2025-07-07
 */
@RestController
@RequestMapping("/discussion-topic-reply")
@Api(tags = "教学活动回复")
public class DiscussionTopicReplyController {
    @Autowired
    private IDiscussionTopicReplyService discussionTopicReplyService;

    @ApiOperation(value = "新增回复")
    @PostMapping("/insert")
    public R insert(@RequestBody DiscussionTopicReply reply) {
        discussionTopicReplyService.insert(reply);
        return R.commonReturn(200, "回复成功", "");
    }

    @ApiOperation(value = "删除回复")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody IdParam idParam) {
        discussionTopicReplyService.deleteDictItemByIds(idParam);
        return R.commonReturn(200, "删除成功", "");
    }

    @ApiOperation(value = "修改回复")
    @PutMapping("/update")
    public R update(@RequestBody DiscussionTopicReply reply) {
        discussionTopicReplyService.updateById(reply);
        return R.commonReturn(200, "修改成功", "");
    }

    @ApiOperation(value = "分页查询自身回复（个人中心）")
    @PostMapping("/getMyReply")
    public R<PageBaseReturnParam<DiscussionTopicMyPageReturnParam>> getMyReply(@RequestBody DiscussionTopicMyPageSearchParam param) {
        Page<DiscussionTopicMyPageReturnParam> page = discussionTopicReplyService.getMyReply(param);
        PageBaseReturnParam<DiscussionTopicMyPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "查看自身回复具体内容（个人中心）")
    @PostMapping("/getMyReplyContent")
    public R<DiscussionTopicMyReturnParam> getMyReplyContent(@RequestBody DiscussionTopicMySearchParam param) {
        return discussionTopicReplyService.getMyReplyContent(param);
    }

    @ApiOperation(value = "分页查询回复")
    @PostMapping("/getReply")
    public R<PageBaseReturnParam<DiscussionTopicReplyPageReturnParam>> getReply(@RequestBody DiscussionTopicReplyPageSearchParam param) {
        Page<DiscussionTopicReplyPageReturnParam> page = discussionTopicReplyService.getReply(param);
        PageBaseReturnParam<DiscussionTopicReplyPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }

    @ApiOperation(value = "分页查询二级回复")
    @PostMapping("/getSecondReply")
    public R<PageBaseReturnParam<DiscussionTopicSecondReplyPageReturnParam>> getSecondReply(@RequestBody DiscussionTopicSecondReplyPageSearchParam param) {
        Page<DiscussionTopicSecondReplyPageReturnParam> page = discussionTopicReplyService.getSecondReply(param);
        PageBaseReturnParam<DiscussionTopicSecondReplyPageReturnParam> result = PageBaseReturnParam.ok(page);
        return R.page(result);
    }
}
