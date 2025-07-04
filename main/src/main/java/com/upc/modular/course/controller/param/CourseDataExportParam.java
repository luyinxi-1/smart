package com.upc.modular.course.controller.param;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@ExcelIgnoreUnannotated
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
@Accessors(chain = true)
public class CourseDataExportParam {
//    @ApiModelProperty("教材名称")
//    @ExcelProperty("教材名称")
//    private String textbookName;

    @ApiModelProperty("教材id")
    @ExcelProperty("教材id")
    private Long textbookId;

    @ApiModelProperty("课程名称")
    @ExcelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("课程学分")
    @ExcelProperty("课程学分")
    private Integer credit;

    @ApiModelProperty("课程描述")
    @ExcelProperty("课程描述")
    private String description;

    @ApiModelProperty("状态")
    @ExcelProperty("状态")
    private String status;

}
