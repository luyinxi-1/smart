package com.upc.modular.textbook.controller;


import com.upc.common.responseparam.R;
import com.upc.modular.textbook.entity.Textbook;
import com.upc.modular.textbook.entity.TextbookReview;
import com.upc.modular.textbook.service.ITextbookReviewService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
