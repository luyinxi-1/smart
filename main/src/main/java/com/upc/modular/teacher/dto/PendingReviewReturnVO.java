package com.upc.modular.teacher.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PendingReviewReturnVO {

    @ApiModelProperty("答卷记录ID (record_id)")
    private Long recordId;

    @ApiModelProperty("题库ID")
    private Long bankId;

    @ApiModelProperty("题库名称")
    private String bankName;

    @ApiModelProperty("学生ID")
    private Long studentId;

    @ApiModelProperty("学生姓名")
    private String studentName;

    @ApiModelProperty("提交时间")
    private LocalDateTime submitTime;

    @ApiModelProperty("第几次作答")
    private Integer exerciseNum;
}
