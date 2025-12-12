package com.upc.modular.textbook.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.upc.common.responseparam.R;
import com.upc.common.responseparam.PageBaseReturnParam;
import com.upc.modular.textbook.param.TextbookRecordPageParam;
import com.upc.modular.textbook.service.ITextbookRecordService;
import com.upc.modular.textbook.param.TextbookRecordPageDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/textbook-record")
@Api(tags = "教材日志管理")
public class TextbookRecordController {

    @Autowired
    private ITextbookRecordService textbookRecordService;
    
    @PostMapping("/page")
    @ApiOperation("分页查看教材修改记录")
    public R<PageBaseReturnParam<TextbookRecordPageDto>> list(@RequestBody TextbookRecordPageParam param) {
        Page<TextbookRecordPageDto> page = textbookRecordService.pageRecords(param);
        return R.page(PageBaseReturnParam.ok(page));
    }
}