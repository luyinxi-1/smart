package com.upc.modular.datastatistics.controller.param;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "TextbookTypeReadingRankExportParam", description = "类型阅读排名导出参数")
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class TextbookTypeReadingRankExportParam {

    @ExcelProperty("类型名称")
    @ApiModelProperty("类型名称")
    private String typeName;

    @ExcelProperty("阅读时长(分钟)")
    @ApiModelProperty("阅读时长(分钟)")
    private Long readingDuration;

    @ExcelProperty("排名")
    @ApiModelProperty("排名")
    private Integer rank;
}