package com.upc.modular.auth.controller.param.SysDictTypeParam;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IdParam {
    @ApiModelProperty(value = "ID列表,角色传入realId")
    List<Long> idList = new ArrayList<>();
}
