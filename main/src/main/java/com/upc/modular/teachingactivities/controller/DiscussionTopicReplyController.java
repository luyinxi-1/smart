package com.upc.modular.teachingactivities.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.common.responseparam.R;
import com.upc.modular.auth.controller.param.SysDictTypeParam.IdParam;
import com.upc.modular.teachingactivities.entity.DiscussionTopicReply;
import com.upc.modular.teachingactivities.param.*;
import com.upc.modular.teachingactivities.service.IDiscussionTopicReplyService;
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
    public R<Boolean> insert(@RequestBody DiscussionTopicReply reply) {
        return R.ok(discussionTopicReplyService.insert(reply));
    }

    @ApiOperation(value = "删除回复")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody IdParam idParam) {
        return R.ok(discussionTopicReplyService.deleteDictItemByIds(idParam));
    }

    @ApiOperation(value = "修改回复")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody DiscussionTopicReply reply) {
        return R.ok(discussionTopicReplyService.updateReply(reply));
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
