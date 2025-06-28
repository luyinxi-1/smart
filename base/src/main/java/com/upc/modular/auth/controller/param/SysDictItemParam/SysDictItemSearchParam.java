package com.upc.modular.auth.controller.param.SysDictItemParam;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SysDictItemSearchParam {

    @ApiModelProperty("字典项名称")
    @TableField("dict_item_name")
    private String dictItemName;

    @ApiModelProperty("字典唯一编码")
    private String dictTypeCode;

}
