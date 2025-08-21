package com.upc.modular.teacher.dto;

import com.upc.common.requestparam.PageBaseSearchParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TeacherLogPageSearchParam extends PageBaseSearchParam {
    @ApiModelProperty("教师ID")
    private Long teacherId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("修改内容")
    private String changeContent;

    @ApiModelProperty("操作路径")
    private String operationPath;
}
