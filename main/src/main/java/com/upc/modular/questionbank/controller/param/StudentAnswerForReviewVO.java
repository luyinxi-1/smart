package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StudentAnswerForReviewVO {

    @ApiModelProperty("答题内容ID (content_id), 用于教师批改时定位到具体题目")
    private Long contentId;

    @ApiModelProperty("答卷记录ID (record_id)")
    private Long recordId;

    @ApiModelProperty("学生姓名")
    private String studentName;

    @ApiModelProperty("学生答案 (主观题)")
    private String studentAnswer;
}