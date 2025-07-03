package com.upc.modular.student.controller.param.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
@Data
public class GenerateUserResultVoStudent {
    @ApiModelProperty(value = "是否全部成功")
    private boolean allSuccess;

    @ApiModelProperty(value = "总处理量")
    private int totalProcessed;

    @ApiModelProperty(value = "成功数量")
    private int successCount;

    @ApiModelProperty(value = "失败数量")
    private int failCount;

    @ApiModelProperty(value = "失败工号")
    private List<String> failedStudents; // 可选，记录失败的 identityId
}
