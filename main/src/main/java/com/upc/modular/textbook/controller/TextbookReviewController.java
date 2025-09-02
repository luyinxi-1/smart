package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.param.TextbookReviewPageParam;
import com.upc.modular.textbook.service.ITextbookReviewService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author fwx
 * @since 2025-08-12
 */
@RestController
@RequestMapping("/textbook-review")
@Api(tags = "教材发布管理")
public class TextbookReviewController {

    @Autowired
    private ITextbookReviewService textbookReviewService;

    @ApiOperation(value = "提交审核")
    @PostMapping("/insertTextbookReview")
    public R insertTextbookReview(@RequestBody TextbookReview textbookReview) {
        textbookReviewService.insertTextbookReview(textbookReview);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "根据教材ID分页查询审核记录")
    @PostMapping("/getPageByTextbookId")
    public R getPageByTextbookId(@RequestBody TextbookReviewPageParam param) {
        return R.ok(textbookReviewService.getPageByTextbookId(param));
    }

    @ApiOperation(value = "根据审核记录ID查询详情")
    @GetMapping("/getReviewDetails/{reviewId}")
    public R getReviewDetails(@PathVariable Long reviewId) {
        return R.ok(textbookReviewService.getById(reviewId));
    }

    @ApiOperation(value = "处理审核结果")
    @PostMapping("/processReviewResult")
    public R processReviewResult(@RequestParam Long reviewId,
                                 @RequestParam Integer auditResult,
                                 @RequestParam String description) {
        textbookReviewService.processReviewResult(reviewId, auditResult, description);
        return R.commonReturn(200, "处理成功", "");
    }

    @ApiOperation(value = "修改审核记录")
    @PostMapping("/updateReview")
    public R updateReview(@RequestBody TextbookReview textbookReview) {
        textbookReviewService.updateReview(textbookReview);
        return R.commonReturn(200, "修改成功", "");
    }
}
