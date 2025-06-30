package com.upc.modular.auth.controller.param.SysLogParam;

import com.upc.modular.auth.entity.SysLog;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysLogPageReturnParam extends SysLog {
    @ApiModelProperty("用户名称")
    private String student_name;
}
