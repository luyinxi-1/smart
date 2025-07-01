package com.upc.modular.teacher.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateUserResultVo {

    @ApiModelProperty(value = "是否全部成功")
    private boolean allSuccess;

    @ApiModelProperty(value = "总处理量")
    private int totalProcessed;

    @ApiModelProperty(value = "成功数量")
    private int successCount;

    @ApiModelProperty(value = "失败数量")
    private int failCount;

    @ApiModelProperty(value = "失败工号")
    private List<String> failedTeachers; // 可选，记录失败的 identityId
}
