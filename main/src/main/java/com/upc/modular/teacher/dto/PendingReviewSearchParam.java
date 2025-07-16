package com.upc.modular.teacher.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PendingReviewSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("题库名称（模糊查询）")
    private String bankName;

    @ApiModelProperty("学生姓名（模糊查询）")
    private String studentName;

    @ApiModelProperty("题库ID（精确查询）")
    private Long bankId;
}
