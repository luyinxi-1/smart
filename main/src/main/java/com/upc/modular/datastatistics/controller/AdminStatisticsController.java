package com.upc.modular.datastatistics.controller;

import com.upc.common.responseparam.R;
import com.upc.modular.datastatistics.controller.param.ChapterMasteryVO;
import com.upc.modular.datastatistics.controller.param.TextbookTypeReadingRankExportParam;
import com.upc.modular.datastatistics.service.ISystemStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @ApiOperation("导出类型阅读时长排名")
    @GetMapping("/export-textbook-type-reading-rank")
    public void exportTextbookTypeReadingRank(
            @ApiParam(value = "开始时间", required = false) @RequestParam(required = false) String startTime,
            @ApiParam(value = "结束时间", required = false) @RequestParam(required = false) String endTime,
            HttpServletResponse response) throws Exception {
        try {
            // 构造参数
            new java.util.HashMap<String, Object>() {{
                put("startTime", startTime);
                put("endTime", endTime);
            }};
            
            List<Map<String, Object>> rawData = systemStatisticsService.getTextbookTypeReadingRank(
                    new java.util.HashMap<String, Object>() {{
                        put("startTime", startTime);
                        put("endTime", endTime);
                    }}
            );
            
            // 转换为导出参数
            List<TextbookTypeReadingRankExportParam> exportData = new java.util.ArrayList<>();
            int rank = 1;
            for (Map<String, Object> item : rawData) {
                TextbookTypeReadingRankExportParam param = new TextbookTypeReadingRankExportParam();
                param.setTypeName((String) item.get("typeName"));
                param.setReadingDuration(((Number) item.get("readingDuration")).longValue());
                param.setRank(rank++);
                exportData.add(param);
            }
            
            // 设置响应头
            String fileName = "类型阅读时长排名.xlsx";
            String fallbackName = "textbook_type_reading_rank.xlsx";
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            
            // 兼容不同浏览器的文件名编码
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);
            
            // 导出Excel
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), TextbookTypeReadingRankExportParam.class)
                    .sheet("类型阅读时长排名")
                    .doWrite(exportData);
        } catch (Exception e) {
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            throw new RuntimeException("导出失败，请重试");
        }
    }
}