package com.upc.modular.questionbank.controller.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@Accessors(chain = true)
public class GradeSubjectiveRequest {

    @ApiModelProperty("student_exercises_content的主键ID")
    private Long contentId;

    @ApiModelProperty("教师给出的分数")
    private Double score;
}
