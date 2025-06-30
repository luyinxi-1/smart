package com.upc.modular.auth.controller.param.SysDictTypeParam;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
public class IdParam {
    @ApiModelProperty(value = "删除ID列表")
    private List<Long> idList;


}
