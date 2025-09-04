package com.upc.modular.textbook.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.param.TextbookReviewPageParam;
import com.upc.modular.textbook.param.TextbookReviewReturnParam;
import com.upc.modular.textbook.mapper.TextbookReviewMapper;
import com.upc.modular.textbook.service.ITextbookReviewService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private TextbookReviewMapper textbookReviewMapper;

    @ApiOperation(value = "提交审核")
    @PostMapping("/insertTextbookReview")
    public R insertTextbookReview(@RequestBody TextbookReview textbookReview) {
        textbookReviewService.insertTextbookReview(textbookReview);
        return R.commonReturn(200, "新增成功", "");
    }

    @ApiOperation(value = "根据教材ID分页查询审核记录（包含创建人和操作人姓名）")
    @PostMapping("/getPageByTextbookId")
    public R getPageByTextbookId(@RequestBody TextbookReviewPageParam param) {
        Page<TextbookReviewReturnParam> page = new Page<>(param.getCurrent(), param.getSize());
        Page<TextbookReviewReturnParam> result = textbookReviewMapper.selectPageWithUserNames(page, param.getTextbookId());
        return R.ok(result);
    }

    @ApiOperation(value = "根据审核记录ID查询详情（包含创建人和操作人姓名）")
    @GetMapping("/getReviewDetails/{reviewId}")
    public R getReviewDetails(@ApiParam(example = "1") @PathVariable Long reviewId) {
        TextbookReviewReturnParam result = textbookReviewMapper.selectByIdWithUserNames(reviewId);
        return R.ok(result);
    }

    @ApiOperation(value = "处理审核结果")
    @PostMapping("/processReviewResult")
    public R processReviewResult(@ApiParam(example = "1") @RequestParam Long reviewId,
                                 @ApiParam(example = "1") @RequestParam Integer auditResult,
                                 @ApiParam(example = "审核通过") @RequestParam String description) {
        textbookReviewService.processReviewResult(reviewId, auditResult, description);
        return R.commonReturn(200, "处理成功", "");
    }
}
