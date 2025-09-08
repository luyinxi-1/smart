package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.formula.functions.Count;

@Data
@Accessors(chain = true)
@ApiModel(value = "StudentStudyPathReturnParam", description = "学生学习路径返回参数")
public class StudentStudyPathReturnParam {
    @ApiModelProperty("阅读教材总数量")
    private Long textbookReadNum;

    @ApiModelProperty("书架教材数量")
    private Long favoriteTextbookNum;

    @ApiModelProperty("已完成")
    private Long completion;
}
