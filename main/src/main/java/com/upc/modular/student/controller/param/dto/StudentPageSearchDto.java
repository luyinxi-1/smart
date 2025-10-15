package com.upc.modular.student.controller.param.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

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
    
    @ApiModelProperty("组织id")
    private Long institutionId;
    
    @ApiModelProperty("年级")
    private Long grade;
    
    @ApiModelProperty("职务")
    private String position;
    
    @ApiModelProperty("账号状态")
    private Integer accountStatus;

    @ApiModelProperty("班级名称")
    private String className;

    @ApiModelProperty(value = "组织ID列表", hidden = true)
    private List<Long> institutionIdList;
}
