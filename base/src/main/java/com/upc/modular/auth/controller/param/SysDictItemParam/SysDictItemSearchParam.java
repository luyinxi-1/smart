package com.upc.modular.auth.controller.param.SysDictItemParam;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysDictItemSearchParam {

    @ApiModelProperty("字典标签")
    private String name;

    @ApiModelProperty("字典类型")
    private String dictType;

}
