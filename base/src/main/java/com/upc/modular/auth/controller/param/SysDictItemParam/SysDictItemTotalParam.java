package com.upc.modular.auth.controller.param.SysDictItemParam;

import com.upc.modular.auth.entity.SysDictItem;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SysDictItemTotalParam {

    @ApiModelProperty("总数")
    private Integer totalNum;

    @ApiModelProperty("字段数据列表")
    private List<SysDictItem> sysDictDataList;
}
