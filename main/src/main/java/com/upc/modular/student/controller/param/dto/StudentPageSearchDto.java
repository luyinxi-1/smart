package com.upc.modular.student.controller.param.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "学生分页查询参数")
public class StudentPageSearchDto  extends PageBaseSearchParam {
    @ApiModelProperty("学号")
    private String identityId;

    @ApiModelProperty("姓名")
    private String name;

    @ApiModelProperty("性别")
    private String gender;

    @ApiModelProperty("学院")
    private String college;

    @ApiModelProperty("班级id")
    private Long classId;
}
