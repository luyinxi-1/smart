package com.upc.modular.datastatistics.controller.param;

import com.upc.modular.student.entity.Student;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.Count;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentStudyPathReturnParam", description = "学生学习路径返回参数")
public class StudentStudyPathReturnParam {
    @ApiModelProperty("阅读教材总数量")
    private Long textbookReadNum;

    @ApiModelProperty("书架教材数量")
    private Long favoriteTextbookNum;

    @ApiModelProperty("已完成阅读教材数")
    private Long completionNum;

    @ApiModelProperty("学生教材阅读总时长(小时)")
    private Long textbookReadingTime;

    @ApiModelProperty("学生教材阅读时长Top10")
    private List<StudentTextbookReadingTimeTopParam> studentTextbookReadingTimeTop;
}