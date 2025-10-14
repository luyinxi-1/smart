package com.upc.modular.student.controller.param.vo;

import com.upc.modular.student.entity.Student;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "学生分页返回对象")
public class StudentReturnVo extends Student {
    @ApiModelProperty("机构名称")
    private String institutionName;

    @ApiModelProperty("机构id")
    private Long institutionId;

    @ApiModelProperty("班级名称")
    private String className;
    
    @ApiModelProperty("年级")
    private Long grade;
}