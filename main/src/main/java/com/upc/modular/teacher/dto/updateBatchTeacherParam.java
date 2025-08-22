package com.upc.modular.teacher.dto;

import com.upc.modular.teacher.entity.Teacher;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class updateBatchTeacherParam {
    @ApiModelProperty(value = "教师主键ID列表", required = true)
    private List<Long> ids;

    @ApiModelProperty(value = "要更新的账号状态", required = true)
    private Integer accountStatus;
}
