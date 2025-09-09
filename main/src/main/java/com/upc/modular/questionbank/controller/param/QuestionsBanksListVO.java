package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ApiModel(value = "题库题目信息(带类型描述)", description = "题库题目信息(带类型描述)")
public class QuestionsBanksListVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("题目id")
    private Long questionId;

    @ApiModelProperty("题库id")
    private Long bankId;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @ApiModelProperty("每道题目分值")
    private Double score;

    @ApiModelProperty("题目类型(数字)")
    private Integer questionType;

    @ApiModelProperty("题目类型(字符串描述)")
    private String questionTypeName;

    @ApiModelProperty("题目名称")
    private String questionContent;
    @ApiModelProperty("题目分类名称")
    private String questionClassificationName;

    public static String getQuestionTypeName(Integer type) {
        if (type == null) return "未知题型";

        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "问答题";
            default: return "未知题型";
        }
    }
}
