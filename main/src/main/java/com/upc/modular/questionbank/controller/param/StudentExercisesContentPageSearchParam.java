package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StudentExercisesContentPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("学生id")
    private Long studentId;

    @ApiModelProperty("题目id")
    private Long teachingQuestion;

    @ApiModelProperty("答卷记录表id")
    private Long recordId;

    @ApiModelProperty("学生的作答内容")
    private String content;

    @ApiModelProperty("学生的作答结果")
    private String result;

    @ApiModelProperty("学生该题目的得分")
    private Double score;

    @ApiModelProperty("题目所属题库id")
    private Long teachingQuestionBankId;
}
