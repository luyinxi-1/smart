package com.upc.modular.datastatistics.controller.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教师教材信息参数
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "TeacherTextbookInfoParam", description = "教师教材信息参数")
public class TeacherTextbookInfoParam {

    @ApiModelProperty("教材ID")
    private Long textbookId;

    @ApiModelProperty("教材名称")
    private String textbookName;

    @ApiModelProperty("教材类型")
    private Long textbookType;

    @ApiModelProperty("教材类型名称")
    private String textbookTypeName;

    @ApiModelProperty("教材出版社")
    private String textbookPublishingHouse;

    @ApiModelProperty("教材版本")
    private String textbookVersion;

    @ApiModelProperty("教材状态")
    private Integer releaseStatus;

    @ApiModelProperty("课程名称")
    private String courseName;

    @ApiModelProperty("班级名称")
    private String className;
}
