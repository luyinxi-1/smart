package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.TextbookNumberReturnParam;
import com.upc.modular.datastatistics.controller.param.TextbookNumberSearchParam;
import com.upc.modular.datastatistics.service.ITextbookStatisticsService;
import com.upc.modular.textbook.service.ITextbookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/textbook-statistics")
@Api(tags = "教材统计")
public class TextbookStatisticsController {
    @Autowired
    private ITextbookStatisticsService textbookStatisticsService;

    @ApiOperation("统计教材总数")
    @PostMapping("/countTextbookNumber")
    public R<Long> countTextbookNumber() {
        return R.ok(textbookStatisticsService.countTextbookNumber());
    }

    @ApiOperation("按时间统计教材新增数量")
    @PostMapping("/countTextbookNumberByTime")
    public R<List<TextbookNumberReturnParam>> countTextbookNumberByTime(@RequestBody TextbookNumberSearchParam param) {
        return R.ok(textbookStatisticsService.countTextbookNumberByTime(param));
    }
}
