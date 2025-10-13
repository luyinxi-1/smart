package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.ChapterMasteryVO;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-statistics")
@Api(tags = "管理员数据统计")
public class AdminStatisticsController {

    @Autowired
    private ISystemStatisticsService systemStatisticsService;

    @ApiOperation("教材阅读时长排名")
    @PostMapping("/textbook-reading-rank")
    public R<List<Map<String, Object>>> getTextbookReadingRank(
            @ApiParam(value = "包含startTime和endTime的JSON对象", required = false) 
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            return R.ok(systemStatisticsService.getTextbookReadingRank(params));
        } catch (Exception e) {
            return R.fail("获取教材阅读时长排名失败: " + e.getMessage());
        }
    }

    @ApiOperation("类型阅读时长排名")
    @PostMapping("/textbook-type-reading-rank")
    public R<List<Map<String, Object>>> getTextbookTypeReadingRank(
            @ApiParam(value = "包含startTime和endTime的JSON对象", required = false) 
            @RequestBody(required = false) Map<String, Object> params) {
        try {
            return R.ok(systemStatisticsService.getTextbookTypeReadingRank(params));
        } catch (Exception e) {
            return R.fail("获取类型阅读时长排名失败: " + e.getMessage());
        }
    }
}