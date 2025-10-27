package com.upc.modular.questionbank.controller.param;

import com.baomidou.mybatisplus.annotation.TableField;
import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TeachingQuestionClassificationSearchParam extends PageBaseSearchParam {

    @ApiModelProperty("题目分类名称")
    @TableField("teaching_question_classification_name")
    private String teachingQuestionClassificationName;

    @ApiModelProperty("创建人姓名（教师名称模糊查询）")
    private String creatorName;
}
